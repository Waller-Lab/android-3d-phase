package com.choochootrain.refocusing;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.InputStream;

public class ImageUtils {
    private Context context;

    public ImageUtils(Context context) {
        this.context = context;
    }

    public Bitmap loadBitmap(int id) {
        InputStream is = this.context.getResources().openRawResource(id);
        return BitmapFactory.decodeStream(is);
    }

    //TODO cache images
    public Bitmap loadBitmap(int id, int width, int height) {
        Bitmap bmp = loadBitmap(id);
        return Bitmap.createScaledBitmap(bmp, width, height, false);
    }
}
