package com.example.developer.cvm_ar;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.SurfaceView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;


public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, GestureDetector.OnGestureListener{

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private static String TAG = "MainActivity";
    JavaCameraView camStream; //object of the surface view containing the camera feed "vidfeed"
    public Mat mRgba; //global variables are horrific the camera output frame by frame as an object
    public GestureDetector mGestDet; //for detecting swipes
    public int mColNum, mRowNum;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        int requestCode = 200;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, requestCode);
        }

        camStream = (JavaCameraView) findViewById(R.id.vidFeed); //assigning the surface view to the camera object
        camStream.setVisibility(SurfaceView.VISIBLE);
        camStream.setCvCameraViewListener(this);

        mGestDet = new GestureDetector(this, this);

        mColNum = 10;
        mRowNum = 5;

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
        mRgba = inputFrame.rgba(); //renders frames from video in colour {R,G,B,A} each pixel?.#

        int numCols = mColNum;
        int numRows = mRowNum;

        int rectWidth = mRgba.width()/numCols;
        int rectHeight = mRgba.height()/numRows;
        Meter meter = new Meter();

        for(int i = 0; i < numCols; i++) {
            int topLeftX = rectWidth * i;
            for (int j = 0; j < numRows; j++) {
                int topLeftY = rectHeight * j;
                meter.DrawDetectionSq(mRgba, topLeftX, topLeftY, rectWidth, rectHeight);
            }
        }
        return mRgba;  // return value should be output value
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        return mGestDet.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        int min = 1;
        int maxCol = 20;
        int maxRow = 10;
        int currentNumCol = mColNum;
        int currentNumRow = mRowNum;

        if((e1.getY() > e2.getY()) && (currentNumRow != maxRow)){
            mRowNum = currentNumRow +1;
            //swipe down
        }

        if((e1.getY() < e2.getY()) && (currentNumRow != min)){
            mRowNum = currentNumRow -1;
            //swipe up
        }

        if((e1.getX() < e2.getX()) && (currentNumCol != maxCol)){
            mColNum = currentNumCol +1;
            //swipe right
        }

        if((e1.getX() > e2.getX()) && (currentNumCol != min)){
            mColNum = currentNumCol -1;
            //swipe left
        }
        return true;
    }
}