package com.example.developer.cvm_ar;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

public class Meter {

    private static String TAG = "MeterClass";

    public void DrawDetectionSq(Mat mRgba ,int topLeftX, int topLeftY, int rectWidth, int rectHeight) {

        // find the ellipse coordinates and draw the ellipse
        int centreX = topLeftX + (rectWidth/2);
        int centreY = topLeftY + (rectHeight/2);
        Point ellipseCentre = new Point(centreX, centreY);
        int ellipseRadius = Math.min(rectWidth, rectHeight)/2;


        // Line Calculations By David Flatla.
        Scalar mColHSV = GetHSVColourAt(mRgba,centreX, centreY);
        double rads = (mColHSV.val[0] / 128.0) * Math.PI;   // 127.5
        double satRadius = ellipseRadius * (mColHSV.val[1]/256.0);
        int x = (int) Math.round(satRadius * Math.cos(rads));
        int y = (int) Math.round(satRadius * Math.sin(rads) * -1.0);
        Point lineEnd = new Point(centreX+x, centreY+y);

        int metLumi = 255 - (int)mColHSV.val[2]; // used to make the meter bright when dark and dark when bright

        Imgproc.ellipse(mRgba,ellipseCentre,new Size(ellipseRadius,ellipseRadius),0,0,360,new Scalar(metLumi,metLumi,metLumi),3);
        Imgproc.line(mRgba, ellipseCentre, lineEnd, new Scalar(metLumi,metLumi,metLumi), 3);

//        String hue = "" + mColHSV.val[0];
//        Imgproc.putText(mRgba, hue, ellipseCentre, 1, 2.0, new Scalar(200,100,100), 3);
    }

    public Scalar GetHSVColourAt(Mat mRgba, int centreX, int centreY) {
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
