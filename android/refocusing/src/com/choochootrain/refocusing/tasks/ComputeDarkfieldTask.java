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

public class ComputeDarkfieldTask extends ImageProgressTask {
    public ComputeDarkfieldTask(Context context) {
        super(context);
        this.progressDialog.setMessage("Assembling darkfield image...");
    }
    Dataset mDataset;

    @Override
    protected Void doInBackground(Dataset... params) {
        Bitmap result = computeDarkfield();

        File resultBmp = new File(mDataset.getResultImagePath("darkfield"));
        try {
            FileOutputStream fos = new FileOutputStream(resultBmp);
            result.compress(Bitmap.CompressFormat.PNG, 100, fos);
        } catch (FileNotFoundException e) {
            return null;
        }

        return null;
    }

    private Bitmap computeDarkfield() {
        Bitmap first = BitmapFactory.decodeFile(mDataset.getRawImagePath(0, 0));
        int width = first.getWidth();
        int height = first.getHeight();

        Mat result = new Mat(height, width, CvType.CV_64FC4);
        Mat result8 = new Mat(height, width, CvType.CV_8UC4);
        Mat img;
        Mat img64 = new Mat(height, width, CvType.CV_64FC4);
        for (int i = 0; i < Dataset.SIZE; i++) {
            int x = i - Dataset.SIZE / 2;
            for (int j = 0; j < Dataset.SIZE; j++) {
                int y = j - Dataset.SIZE / 2;

                //only use dark-field images
                if (Math.sqrt(x*x + y*y) >= Dataset.SIZE / 2.0) {
                    img = ImageUtils.toMat(BitmapFactory.decodeFile(mDataset.getRawImagePath(i, j)));
                    img.convertTo(img64, CvType.CV_64FC4);

                    //add to result
                    Core.add(result, img64, result);
                }

                float progress = ((float)(i * Dataset.SIZE + j)) / (Dataset.SIZE * Dataset.SIZE);
                onProgressUpdate((int)(progress * 100), -1);
            }
        }

        Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(result.reshape(1));
        result.convertTo(result8, CvType.CV_8UC4, 255/minMaxLocResult.maxVal);

        return ImageUtils.toBitmap(result8);
    }
}
