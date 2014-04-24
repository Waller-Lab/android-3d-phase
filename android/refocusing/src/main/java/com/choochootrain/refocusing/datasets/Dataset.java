package com.choochootrain.refocusing.datasets;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

// Constants for current image dataset
public class Dataset {
    public static final int SIZE = 7;
    public static final int LED_DISTANCE = 4;
    public static final int F_CONDENSER = 60; //mm
    public static final double F1 = 16.5;
    public static final double F2 = 25.4 * 4;
    public static final double M = F2/F1;
    public static final double DX0 = 0.0053;
    public static final double DX = DX0/M; //spatial resolution

    private static final String DATASET_PATH = "/sdcard/datasets/";
    private static final String DATASET = "10-1-13";

    public static Bitmap loadBitmap(int row, int col) {
        String path = DATASET_PATH + DATASET + "/image" + String.format("%d%d", row, col) + ".bmp";
        return BitmapFactory.decodeFile(path);
    }
}
