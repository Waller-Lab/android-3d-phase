package com.choochootrain.refocusing.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.choochootrain.refocusing.datasets.Dataset;
import com.choochootrain.refocusing.utils.ImageUtils;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class ComputeDPCTask extends ImageProgressTask {
    public ComputeDPCTask(Context context) {
        super(context);
        this.progressDialog.setMessage("Assembling DPC image...");
    }
    Dataset mDataset = null;

    @Override
    protected Void doInBackground(Dataset... params) {
    	mDataset = params[0];
        float zMin = mDataset.ZMIN;
        float zInc = mDataset.ZINC;
        float zMax = mDataset.ZMAX;

        for (float z = zMin; z <= zMax; z += zInc) {
            float progress = (z - zMin) / (zMax - zMin);
            onProgressUpdate((int)(progress * 100), -1);

            Bitmap result = computeDPC(z);
            File resultBmp = new File(mDataset.getResultImagePath("dpc", z));
            try {
                FileOutputStream fos = new FileOutputStream(resultBmp);
                result.compress(Bitmap.CompressFormat.PNG, 100, fos);
            } catch (FileNotFoundException e) {
                return null;
            }
        }

        return null;
    }

    private Bitmap computeDPC(float z) {
        Bitmap first = BitmapFactory.decodeFile(mDataset.getRawImagePath(0, 0));
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
                    img = ImageUtils.toMat(BitmapFactory.decodeFile(mDataset.getRawImagePath(i, j)));
                    img.convertTo(img64, CvType.CV_64FC4);

                    //compute and perform shift

                    int xDistance = x * Dataset.LED_DISTANCE;
                    int yDistance = y * Dataset.LED_DISTANCE;
                    double xShiftDistance = -z * xDistance / Dataset.F_CONDENSER;
                    double yShiftDistance = -z * yDistance / Dataset.F_CONDENSER;
                    int xShift = (int)(-xShiftDistance / Dataset.DX + 0.5);
                    int yShift = (int)(-yShiftDistance / Dataset.DX + 0.5);

                    shifted = ImageUtils.circularShift(img64, xShift, yShift);

                    if (x <= 0) //add LHS
                        Core.add(result, shifted, result);
                    else //subtract RHS
                        Core.subtract(result, shifted, result);
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
