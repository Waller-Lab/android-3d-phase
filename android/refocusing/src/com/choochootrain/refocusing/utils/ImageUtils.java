package com.choochootrain.refocusing.utils;

import android.graphics.Bitmap;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;

public class ImageUtils {
    private static final String TAG = "ImageUtils";

    public static Mat toMat(Bitmap bmp) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bmp, mat);
        return mat;
    }

    public static Bitmap toBitmap(Mat mat) {
        Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bmp);
        return bmp;
    }
    


    public static double[][] multiplyArray(double m1[][],double m2[][]){
      int m1rows = m1.length;
      int m1cols = m1[0].length;
      int m2rows = m2.length;
      int m2cols = m2[0].length;
      if (m1cols != m2rows)
         throw new IllegalArgumentException("matrices  don't match: "+ m1cols + " != " + m2rows);
      else
      {
         double[][] result = new double[m1rows][m2cols];
         for (int i=0; i< m1rows; i++){
            for (int j=0; j< m2cols; j++){
               for (int k=0; k< m1cols; k++){
                  result[i][j] += m1[i][k] * m2[k][j];
               }
            }
         }
   	  return result;
      }
   }

    public static Mat circularShift(Mat mat, int x, int y) {
        int w = mat.cols();
        int h = mat.rows();
        Mat result = Mat.zeros(h, w, CvType.CV_32FC4);

        int shiftR = x % w;
        int shiftD = y % h;
        //java modulus gives negative results for negative numbers
        if (shiftR < 0)
            shiftR += w;
        if (shiftD < 0)
            shiftD += h;

        /* extract 4 submatrices
                      |---| shiftR
             ______________
            |         |   |
            |    1    | 2 |
            |_________|___|  ___ shiftD
            |         |   |   |
            |    3    | 4 |   |
            |         |   |   |
            |_________|___|  _|_
         */
        Mat shift1 = mat.submat(0, h-shiftD, 0, w-shiftR);
        Mat shift2 = mat.submat(0, h-shiftD, w-shiftR, w);
        Mat shift3 = mat.submat(h-shiftD, h, 0, w-shiftR);
        Mat shift4 = mat.submat(h-shiftD, h, w-shiftR, w);

        /* and rearrange
             ______________
            |   |         |
            | 4 |    3    |
            |   |         |
            |___|_________|
            |   |         |
            | 2 |    1    |
            |___|_________|
         */
        shift1.copyTo(new Mat(result, new Rect(shiftR, shiftD, w-shiftR, h-shiftD)));
        shift2.copyTo(new Mat(result, new Rect(0, shiftD, shiftR, h-shiftD)));
        shift3.copyTo(new Mat(result, new Rect(shiftR, 0, w-shiftR, shiftD)));
        shift4.copyTo(new Mat(result, new Rect(0, 0, shiftR, shiftD)));

        return result;
    }
    
    

}
