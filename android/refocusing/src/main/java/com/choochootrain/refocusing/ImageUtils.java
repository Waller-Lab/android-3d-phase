package com.choochootrain.refocusing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

//TODO cache images
public class ImageUtils {
    private static final String DATASET_PATH = "/sdcard/datasets/";
    //TODO refactor this
    private static final int DATASET_SIZE = 7;

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

    public Bitmap computeFocus(float depth) {
        Bitmap first = loadBitmap(0, 0);
        int width = first.getWidth();
        int height = first.getHeight();

        Mat result = new Mat(height, width, CvType.CV_8UC1);
        Mat img;
        for (int i = 0; i < DATASET_SIZE; i++) {
            int x = i - DATASET_SIZE / 2;
            for (int j = 0; j < DATASET_SIZE; j++) {
                int y = j - DATASET_SIZE / 2;

                //only use bright-field images
                if (Math.sqrt(x*x + y*y) < DATASET_SIZE / 2.0) {
                    img = toMat(loadBitmap(i, j));

                    //compute and perform shift

                    //add to result
                }
            }
        }

        return toBitmap(result);
    }

}
