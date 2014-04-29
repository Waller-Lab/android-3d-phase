package com.choochootrain.refocusing.datasets;

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

    public static final float MAX_DEPTH = 0.1f;
    public static final float DEPTH_INC = 0.01f;

    public static final String DATASET_PATH = "/sdcard/datasets/";
    public static final String DATASET = "10-1-13";

    public static String getRawImagePath(int row, int col) {
        return DATASET_PATH + DATASET + "/image" + String.format("%d%d", row, col) + ".bmp";
    }

    public static String getResultImagePath(float depth) {
        return DATASET_PATH + DATASET + "/result" + String.format("%f", depth) + ".png";
    }
}
