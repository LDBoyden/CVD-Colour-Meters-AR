package com.example.developer.cvm_ar;


import android.provider.ContactsContract;
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

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import static android.R.attr.width;
import static android.R.attr.x;
import static android.R.attr.y;


public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private static String TAG = "MainActivity";
    JavaCameraView camStream; //object of the surface view containing the camera feed "vidfeed"
    Mat mRgba, mRgb , mAcrom, mEdge, mHsv , mBgr; //global variables are horrific
    private Scalar mColHSV;
    TextView SqCol;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Example of a call to a native method
        /*  TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
        TextView Suc = (TextView) findViewById(R.id.successText);
        Suc.setText("TestCase: OpenCV Install Success");
        */

        camStream = (JavaCameraView) findViewById(R.id.vidFeed); //assigning the surface view to the camera object
        camStream.setVisibility(SurfaceView.VISIBLE);
        camStream.setCvCameraViewListener(this);

        SqCol = (TextView) findViewById(R.id.TvColOut);
        SqCol.setText("Hue: " + mColHSV.val[0] + " Sat: " + mColHSV.val[1] + " Val: " + mColHSV.val[2]);
    }

    BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch(status){
                case BaseLoaderCallback.SUCCESS:{
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
    protected void onPause(){
        super.onPause();
        if (camStream != null){
            camStream.disableView();
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if (camStream != null){
            camStream.disableView();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        if (OpenCVLoader.initDebug()){
            Log.i(TAG, "OpenCV Loaded Successfully");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }else{
            Log.i(TAG, "OpenCV Not Loaded");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0,this, mLoaderCallback);
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();


    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height,width, CvType.CV_8SC4); // defines matrice as being the size of the screen with colour channels as 4
        mRgb = new Mat(height,width, CvType.CV_8SC4);
        mAcrom = new Mat(height,width, CvType.CV_8SC1); //defines colour channels as 1
        mEdge = new Mat(height,width, CvType.CV_8SC1); //defines entire screen as field to detect and 1 channel.
        mHsv = new Mat(height,width, CvType.CV_8SC3);
        mBgr = new Mat(height,width, CvType.CV_8SC3);

        mColHSV = new Scalar(255);

    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba(); //renders frames from video in colour {R,G,B,A} each pixel?.
        mRgb = mRgba;

        double mSizeX = mRgba.size().height;
        double mSizeY = mRgba.size().width;

        Point Dsq1 = new Point(mSizeY/10*6,mSizeX/10*4);
        Point Dsq2 = new Point(mSizeY/10*4,mSizeX/10*6);

        Imgproc.cvtColor(mRgb,mBgr,Imgproc.COLOR_RGBA2BGR);
        Imgproc.cvtColor(mBgr,mHsv,Imgproc.COLOR_BGR2HSV);
        //Imgproc.cvtColor(mHsv,mRgba,Imgproc.COLOR_BGR2RGBA);

        Imgproc.rectangle(mRgba,Dsq1,Dsq2,new Scalar(100,25,100),6);
        Rect DetSQ = new Rect();

        //mHsv = Core.sumElems(detection points?);
        //ColView(mSizeX,mSizeY);

        DetSQ.x = (int)mSizeY/2;
        DetSQ.y = (int)mSizeX/2;

        DetSQ.width = (int)mSizeX/10*2;
        DetSQ.height = (int)mSizeY/10*2;

        Mat HSVDetSQ = new Mat();
        Mat RGBADetSQ = mRgba.submat(DetSQ);
        Imgproc.cvtColor(RGBADetSQ,HSVDetSQ,Imgproc.COLOR_RGB2HSV_FULL);

        mColHSV = Core.sumElems(RGBADetSQ);
        int DetSQPx = DetSQ.height * DetSQ.height;
        for(int i = 0; i < mColHSV.val.length; i++){
            mColHSV.val[i] /= DetSQPx;
        }

        return mRgba;  // return value should be output value
    }

    /*
    public void ColView(double ScrnX, double ScrnY){
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 1, 10, 1);

        LinearLayout AVGCol = new LinearLayout(this);
        AVGCol.setOrientation(LinearLayout.HORIZONTAL);
        AVGCol.setLayoutParams(layoutParams);

        TextView value = new TextView(this);
        value.setText("Test");
        value.setTextSize(23);
        value.setGravity(Gravity.CENTER);

        AVGCol.addView(value);
    }
    */

}
