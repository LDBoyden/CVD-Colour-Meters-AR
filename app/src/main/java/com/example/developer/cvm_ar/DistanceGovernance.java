package com.example.developer.cvm_ar;

import android.view.GestureDetector;
import android.view.MotionEvent;


public class DistanceGovernance extends MainActivity implements GestureDetector.OnGestureListener{

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
