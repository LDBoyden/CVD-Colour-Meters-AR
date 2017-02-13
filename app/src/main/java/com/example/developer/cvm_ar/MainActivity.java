package com.example.developer.cvm_ar;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
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
import org.opencv.core.MatOfRect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;



public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private static String TAG = "MainActivity";
    JavaCameraView camStream; //object of the surface view containing the camera feed "vidfeed"
    Mat mRgba, mRgb , mAcrom, mEdge, mHsv , mBgr; //global variables are horrific


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

        //Imgproc.cvtColor(mRgba,mAcrom,Imgproc.COLOR_RGB2GRAY); //image processing simple rgb to gray
        //Imgproc.Canny(mAcrom,mEdge,10,30); // simple edge detection inputmat,outputmat,gradient detection vertical, gradient horizontal.

        Imgproc.cvtColor(mRgb,mBgr,Imgproc.COLOR_RGBA2BGR);
        Imgproc.cvtColor(mBgr,mHsv,Imgproc.COLOR_BGR2HSV);
        //Imgproc.cvtColor(mHsv,mRgba,Imgproc.COLOR_BGR2RGBA);

        //Imgproc.cvtColor(mHsv,mRgba,Imgproc.COLOR_HSV2RGB_FULL);
        //Method(input material, output material, process parameters)
        //Core.split(); could be needed for splitting colour channels.
        //Imgproc.calcHist();


        //System.out.println(mHsv);
        //Imgproc.rectangle(mRgba,new Point(mSizeX/100*10,mSizeY/100*10),new Point(mSizeX/100*50,mSizeY/100*80),new Scalar(0,0,0),6);

        Imgproc.line(mRgba,new Point(mSizeX/100*90,mSizeY/100*10),new Point(mSizeX/100*10,mSizeY/100*10),new Scalar(0,0,0),6);
        Imgproc.line(mRgba,new Point(mSizeX/100*80,mSizeY/100*20),new Point(mSizeX/100*20,mSizeY/100*20),new Scalar(0,0,0),6);
        Imgproc.line(mRgba,new Point(mSizeX/100*70,mSizeY/100*30),new Point(mSizeX/100*30,mSizeY/100*30),new Scalar(0,0,0),6);
        Imgproc.line(mRgba,new Point(mSizeX/100*60,mSizeY/100*40),new Point(mSizeX/100*40,mSizeY/100*40),new Scalar(0,0,0),6);
        Imgproc.line(mRgba,new Point(mSizeX/100*10,mSizeY/100*50),new Point(mSizeX/100*170,mSizeY/100*50),new Scalar(0,0,0),6);
        // screen width is 180 screen height is 60
        return mRgba;  // return value should be output value
    }
}
