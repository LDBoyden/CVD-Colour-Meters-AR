package com.example.developer.cvm_ar;


import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;


public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private static String TAG = "MainActivity";
    JavaCameraView camStream; //object of the surface view containing the camera feed "vidfeed"
    Mat mRgba, mAcrom, mEdge, mHsv; //global variables are horrific

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
        mAcrom = new Mat(height,width, CvType.CV_8SC1); //defines colour channels as 1
        mEdge = new Mat(height,width, CvType.CV_8SC1); //defines entire screen as field to detect and 1 channel.
        mHsv = new Mat(height,width, CvType.CV_64FC2);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba(); //renders frames from video in colour {R,G,B,A} each pixel?.


        Imgproc.cvtColor(mRgba,mAcrom,Imgproc.COLOR_RGB2GRAY); //image processing simple rgb to gray
        Imgproc.Canny(mAcrom,mEdge,10,30); // simple edge detection inputmat,outputmat,gradient detection vertical, gradient horizontal.
        Imgproc.cvtColor(mRgba,mHsv,Imgproc.COLOR_RGB2HSV_FULL);
        //Method(input material, output material, process parameters)
        //Core.split(); could be needed for splitting colour channels.
        //Imgproc.calcHist();
        

        return mHsv;  // return value should be output value
    }
}
