package com.example.developer.cvm_ar;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import com.google.vr.ndk.base.GvrLayout;
import com.google.vr.sdk.base.GvrActivity;
import com.google.vr.sdk.base.GvrView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class MainActivity extends GvrActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private static String TAG = "MainActivity";
    JavaCameraView camStream; //object of the surface view containing the camera feed "vidfeed"

    public Mat mRgba; //global variables are horrific

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        /*  TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
        */

        camStream = (JavaCameraView) findViewById(R.id.vidFeed); //assigning the surface view to the camera object
        camStream.setVisibility(SurfaceView.VISIBLE);
        camStream.setCvCameraViewListener(this);

    }

    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case BaseLoaderCallback.SUCCESS: {
                    camStream.enableView();
                    break;
                }
                default: {
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        if (camStream != null) {
            camStream.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (camStream != null) {
            camStream.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            Log.i(TAG, "OpenCV Loaded Successfully");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        } else {
            Log.i(TAG, "OpenCV Not Loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, mLoaderCallback);
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4); // defines matrix as being the size of the screen with colour channels as 4
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba(); //renders frames from video in colour {R,G,B,A} each pixel?.

        int numCols = 10;
        int numRows = 5;
        int rectWidth = mRgba.width()/numCols;
        int rectHeight = mRgba.height()/numRows;

        for(int i = 0; i < numCols; i++) {
            int topLeftX = rectWidth * i;
            for (int j = 0; j < numRows; j++) {
                int topLeftY = rectHeight * j;
                DrawDetectionSq(topLeftX, topLeftY, rectWidth, rectHeight);
            }
        }
        return mRgba;  // return value should be output value
    }

    public void DrawDetectionSq(int topLeftX, int topLeftY, int rectWidth, int rectHeight) {

        // find the ellipse coordinates and draw the ellipse
        int centreX = topLeftX + (rectWidth/2);
        int centreY = topLeftY + (rectHeight/2);
        Point ellipseCentre = new Point(centreX, centreY);
        int ellipseRadius = Math.min(rectWidth, rectHeight)/2;


        // Line Calculations By David Flatla.
        Scalar mColHSV = GetHSVColourAt(centreX, centreY);
        double rads = (mColHSV.val[0] / 128.0) * Math.PI;   // 127.5
        double satRadius = ellipseRadius * (mColHSV.val[1]/256.0);
        int x = (int) Math.round(satRadius * Math.cos(rads));
        int y = (int) Math.round(satRadius * Math.sin(rads) * -1.0);
        Point lineEnd = new Point(centreX+x, centreY+y);

        int metLumi = 255 - (int)mColHSV.val[2]; // used to make the meter bright when dark and dark when bright

        Imgproc.ellipse(mRgba,ellipseCentre,new Size(ellipseRadius,ellipseRadius),0,0,360,new Scalar(metLumi,metLumi,metLumi),3);
        Imgproc.line(mRgba, ellipseCentre, lineEnd, new Scalar(200,100,100), 3);

//        String hue = "" + mColHSV.val[0];
//        Imgproc.putText(mRgba, hue, ellipseCentre, 1, 2.0, new Scalar(200,100,100), 3);
    }

    public Scalar GetHSVColourAt(int centreX, int centreY) {
        // define the detection square (1x1 centred at above coordinates)
        Rect DetSQ = new Rect();
        DetSQ.x = centreX;
        DetSQ.y = centreY;
        DetSQ.width = 1;
        DetSQ.height = 1;

        // get the RGBA colours (1) in the detection square
        Mat RGBADetSQ = mRgba.submat(DetSQ);
        Imgproc.cvtColor(RGBADetSQ, RGBADetSQ, Imgproc.COLOR_RGBA2RGB);
        Imgproc.cvtColor(RGBADetSQ, RGBADetSQ, Imgproc.COLOR_RGB2HSV_FULL);

        // sample the central (only) colour
        return new Scalar(RGBADetSQ.get(0, 0));
    }
}