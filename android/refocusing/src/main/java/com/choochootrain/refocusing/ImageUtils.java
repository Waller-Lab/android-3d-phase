package com.choochootrain.refocusing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.InputStream;

//TODO cache images
public class ImageUtils {
    private Context context;

    public ImageUtils(Context context) {
        this.context = context;
    }

    public Bitmap loadBitmap(int id) {
        InputStream is = this.context.getResources().openRawResource(id);
        return BitmapFactory.decodeStream(is);
    }

    public Bitmap loadBitmap(int id, int width, int height) {
        Bitmap bmp = loadBitmap(id);
        return Bitmap.createScaledBitmap(bmp, width, height, false);
    }

    public Bitmap toBitmap(Mat mat) {
        Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, bmp);
        return bmp;
    }

    public Bitmap computeFocus(String dataset, float depth) {
        return null;
    }
}
