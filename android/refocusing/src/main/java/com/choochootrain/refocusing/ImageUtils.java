package com.choochootrain.refocusing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.InputStream;
import java.net.URL;

//TODO cache images
public class ImageUtils {
    private static final String DATASET_PATH = "/sdcard/datasets/";
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
        return loadBitmap(0, 0);
    }

}
