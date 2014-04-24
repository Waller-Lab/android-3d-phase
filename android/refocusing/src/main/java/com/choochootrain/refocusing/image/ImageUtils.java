package com.choochootrain.refocusing.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.choochootrain.refocusing.tasks.ComputeFocusTask;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Core;

//TODO cache images
public class ImageUtils {
    private static final String TAG = "ImageUtils";
    private static final String DATASET_PATH = "/sdcard/datasets/";
    //TODO refactor this
    private static final int DATASET_SIZE = 7;
    private static final int LED_DISTANCE = 4;
    private static final int F_CONDENSER = 60; //mm

    private static final double F1 = 16.5;
    private static final double F2 = 25.4 * 4;
    private static final double M = F2/F1;
    private static final double DX0 = 0.0053;
    private static final double DX = DX0/M; //spatial resolution

    public static Bitmap loadBitmap(String dataset, int row, int col) {
        String path = DATASET_PATH + dataset + "/image" + String.format("%d%d", row, col) + ".bmp";
        return BitmapFactory.decodeFile(path);
    }

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

    public static Bitmap computeFocus(String dataset, float z, ComputeFocusTask task) {
        Bitmap first = loadBitmap(dataset, 0, 0);
        int width = first.getWidth();
        int height = first.getHeight();

        Mat result = new Mat(height, width, CvType.CV_64FC4);
        Mat result8 = new Mat(height, width, CvType.CV_8UC4);
        Mat img;
        Mat img64 = new Mat(height, width, CvType.CV_64FC4);
        Mat shifted;
        for (int i = 0; i < DATASET_SIZE; i++) {
            int x = i - DATASET_SIZE / 2;
            for (int j = 0; j < DATASET_SIZE; j++) {
                int y = j - DATASET_SIZE / 2;

                //only use bright-field images
                if (Math.sqrt(x*x + y*y) < DATASET_SIZE / 2.0) {
                    img = toMat(loadBitmap(dataset, i, j));
                    img.convertTo(img64, CvType.CV_64FC4);

                    //compute and perform shift

                    int xDistance = x * LED_DISTANCE;
                    int yDistance = y * LED_DISTANCE;
                    double xShiftDistance = -z * xDistance / F_CONDENSER;
                    double yShiftDistance = -z * yDistance / F_CONDENSER;
                    int xShift = (int)(-xShiftDistance / DX + 0.5);
                    int yShift = (int)(-yShiftDistance / DX + 0.5);

                    //TODO use frequency domain scalar for better shifting
                    shifted = circularShift(img64, xShift, yShift);

                    //add to result
                    Core.add(result, shifted, result);
                }

                task.updateProgress((int)((i * DATASET_SIZE + j) / (DATASET_SIZE * DATASET_SIZE / 100.0)));
            }
        }

        Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(result.reshape(1));
        result.convertTo(result8, CvType.CV_8UC4, 255/minMaxLocResult.maxVal);

        return toBitmap(result8);
    }

    public static Mat circularShift(Mat mat, int x, int y) {
        int w = mat.cols();
        int h = mat.rows();
        Mat result = Mat.zeros(h, w, CvType.CV_64FC4);

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
