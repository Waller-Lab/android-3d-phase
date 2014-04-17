package com.choochootrain.refocusing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Core;

//TODO cache images
public class ImageUtils {
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


    private Context context;
    private String dataset;

    public ImageUtils(Context context, String dataset) {
         this.context = context;
    }

    public Bitmap loadBitmap(int row, int col) {
        String path = DATASET_PATH + dataset + "/image" + String.format("%d%d", row, col) + ".bmp";
        return BitmapFactory.decodeFile(path);
    }

    public Mat toMat(Bitmap bmp) {
        Mat mat = new Mat();
        Utils.bitmapToMat(bmp, mat);
        return mat;
    }

    public Bitmap toBitmap(Mat mat) {
        Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bmp);
        return bmp;
    }

    public Bitmap computeFocus(float z) {
        Bitmap first = loadBitmap(0, 0);
        int width = first.getWidth();
        int height = first.getHeight();

        Mat result = new Mat(height, width, CvType.CV_8UC1);
        Mat img;
        Mat shifted;
        for (int i = 0; i < DATASET_SIZE; i++) {
            int x = i - DATASET_SIZE / 2;
            for (int j = 0; j < DATASET_SIZE; j++) {
                int y = j - DATASET_SIZE / 2;

                //only use bright-field images
                if (Math.sqrt(x*x + y*y) < DATASET_SIZE / 2.0) {
                    img = toMat(loadBitmap(i, j));

                    //compute and perform shift

                    int xDistance = x * LED_DISTANCE;
                    int yDistance = y * LED_DISTANCE;
                    double xShiftDistance = -z * xDistance / F_CONDENSER;
                    double yShiftDistance = -z * yDistance / F_CONDENSER;
                    int xShift = (int)(-xShiftDistance / DX + 0.5);
                    int yShift = (int)(-yShiftDistance / DX + 0.5);

                    //TODO use frequency domain scalar for better shifting
                    shifted = circularShift(img, xShift, yShift);

                    //add to result
                    Core.add(result, shifted, result);
                }
            }
        }

        Core.normalize(result, result);

        return toBitmap(result);
    }

    private Mat circularShift(Mat mat, int x, int y) {
        int w = mat.width();
        int h = mat.height();
        Mat result = Mat.zeros(w, h, CvType.CV_8UC1);
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int i2 = (i + x) % w;
                int j2 = (j + y) % h;
                result.put(i2, j2, mat.get(i, j));
            }
        }

        return result;
    }

}
