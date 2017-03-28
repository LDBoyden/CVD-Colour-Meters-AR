package com.example.developer.cvm_ar;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import android.view.SurfaceView;
import android.widget.TextView;

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


public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private static String TAG = "MainActivity";
    JavaCameraView camStream; //object of the surface view containing the camera feed "vidfeed"
    public Mat mRgba, mRgb, mHsv; //global variables are horrific

    TextView SqCol;
    String ColOut = "???";
    Handler UiAccess = new Handler();

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

        SqCol = (TextView) findViewById(R.id.TvColOut);


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
        mHsv = new Mat(height, width, CvType.CV_8UC3);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba(); //renders frames from video in colour {R,G,B,A} each pixel?.
        mRgb = mRgba;

        for(int i = 0; i < 11; i++) {
            for (int j = 0; j < 5; j++) {
                DrawDetectionSq(i, j);
            }
        }

        return mRgba;  // return value should be output value
    }


    public void DrawDetectionSq(int col, int row) {
        double mSizeX = mRgba.size().height;
        double mSizeY = mRgba.size().width;

        Point Dsq1 = new Point(mSizeY/10*row , mSizeX/5*col);
        Point Dsq2 = new Point(mSizeY/10*col , mSizeX/5*row);
        Point DelC = new Point(mSizeY/10*(col+0.5), mSizeX/5*(row+0.5));

        int radius = 60;
        Size DelA = new Size(radius,radius);

        Imgproc.rectangle(mRgba, Dsq1, Dsq2, new Scalar(100,25,100),3);
        Imgproc.ellipse(mRgba,DelC,DelA,0,0,360,new Scalar(100,25,100),3);

        //Line Calculations By David Flatla.
        Scalar mColHSV = ColourDetectionSq(mSizeX,mSizeY);
        double degs = mColHSV.val[0] * 360.0 / 255.0;
        double rads = degs / 2.0 * Math.PI;
        int y = (int) Math.round(radius * Math.sin(rads));
        int x = (int) Math.round(radius * Math.cos(rads));
        Point lineEnd = new Point(y+DelC.y, x+DelC.x);
        Imgproc.line(mRgba, DelC, lineEnd, new Scalar(200,100,100), 3);
    }

    public Scalar ColourDetectionSq(double x, double y){
        Scalar mColHSV;

        Rect DetSQ = new Rect();
        DetSQ.x = (int) y / 2;
        DetSQ.y = (int) x / 2;

        DetSQ.width = (int) x / 10 * 2;
        DetSQ.height = (int) y / 10 * 2;

        Mat RGBADetSQ = mRgba.submat(DetSQ);

        Imgproc.cvtColor(RGBADetSQ, RGBADetSQ, Imgproc.COLOR_RGBA2RGB);
        Imgproc.cvtColor(RGBADetSQ, RGBADetSQ, Imgproc.COLOR_RGB2HSV_FULL);


        mColHSV = new Scalar(RGBADetSQ.get(0, 0));

        mColHSV.val[1] = mColHSV.val[1]/2.55;
        mColHSV.val[2] = mColHSV.val[2]/2.55;

        return mColHSV;
    }

    public void ChText(Scalar ScVal){

        ColOut = "HSV color: (" + (int)ScVal.val[0] + ", " +(int)ScVal.val[1] + ", " + (int)ScVal.val[2] + ")";

        Runnable UpdateUI = new Runnable() {
            @Override
            public void run() {
                {
                    SqCol.setText(ColOut);
                }
            }
        };

        UiAccess.post(UpdateUI);

    }


}