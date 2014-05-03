package com.choochootrain.refocusing.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.choochootrain.refocusing.datasets.Dataset;
import com.choochootrain.refocusing.image.ImageUtils;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class ComputeFocusTask extends ImageProgressTask {
    public ComputeFocusTask(Context context) {
        super(context);
        this.progressDialog.setMessage("Assembling refocused images...");
    }

    @Override
    protected Void doInBackground(Float... params) {
        float zMin = params[0];
        float zInc = params[1];
        float zMax = params[2];

        for (float z = zMin; z <= zMax; z += zInc) {
            float progress = (z - zMin) / (zMax - zMin);
            onProgressUpdate((int)(progress * 100), -1);

            Bitmap result = computeFocus(z);
            File resultBmp = new File(Dataset.getResultImagePath(z));
            try {
                FileOutputStream fos = new FileOutputStream(resultBmp);
                result.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } catch (FileNotFoundException e) {
                return null;
            }
        }

        return null;
    }

    private Bitmap computeFocus(float z) {
        Bitmap first = BitmapFactory.decodeFile(Dataset.getRawImagePath(0, 0));
        int width = first.getWidth();
        int height = first.getHeight();

        Mat result = new Mat(height, width, CvType.CV_64FC4);
        Mat result8 = new Mat(height, width, CvType.CV_8UC4);
        Mat img;
        Mat img64 = new Mat(height, width, CvType.CV_64FC4);
        Mat shifted;
        for (int i = 0; i < Dataset.SIZE; i++) {
            int x = i - Dataset.SIZE / 2;
            for (int j = 0; j < Dataset.SIZE; j++) {
                int y = j - Dataset.SIZE / 2;

                //only use bright-field images
                if (Math.sqrt(x*x + y*y) < Dataset.SIZE / 2.0) {
                    img = ImageUtils.toMat(BitmapFactory.decodeFile(Dataset.getRawImagePath(i, j)));
                    img.convertTo(img64, CvType.CV_64FC4);

                    //compute and perform shift

                    int xDistance = x * Dataset.LED_DISTANCE;
                    int yDistance = y * Dataset.LED_DISTANCE;
                    double xShiftDistance = -z * xDistance / Dataset.F_CONDENSER;
                    double yShiftDistance = -z * yDistance / Dataset.F_CONDENSER;
                    int xShift = (int)(-xShiftDistance / Dataset.DX + 0.5);
                    int yShift = (int)(-yShiftDistance / Dataset.DX + 0.5);

                    //TODO use frequency domain scalar for better shifting
                    shifted = ImageUtils.circularShift(img64, xShift, yShift);

                    //add to result
                    Core.add(result, shifted, result);
                }

                float progress = ((float)(i * Dataset.SIZE + j)) / (Dataset.SIZE * Dataset.SIZE);
                onProgressUpdate(-1, (int)(progress * 100));
            }
        }

        Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(result.reshape(1));
        result.convertTo(result8, CvType.CV_8UC4, 255/minMaxLocResult.maxVal);

        return ImageUtils.toBitmap(result8);
    }
}