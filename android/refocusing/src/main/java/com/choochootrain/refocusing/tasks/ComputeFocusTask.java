package com.choochootrain.refocusing.tasks;

import android.graphics.Bitmap;
import android.widget.Toast;

import com.choochootrain.refocusing.datasets.Dataset;
import com.choochootrain.refocusing.image.ImageUtils;
import com.choochootrain.refocusing.MainActivity;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class ComputeFocusTask extends ImageProgressTask {
    private MainActivity mainActivity;

    public ComputeFocusTask(MainActivity mainActivity) {
        super(mainActivity);
        this.mainActivity = mainActivity;
        this.progressDialog.setMessage("Assembling refocused image...");
    }

    @Override
    protected Bitmap doInBackground(Float... params) {
        float z = params[0];
        Bitmap first = Dataset.loadBitmap(0, 0);
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
                    img = ImageUtils.toMat(Dataset.loadBitmap(i, j));
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
                onProgressUpdate((int)(progress * 100));
            }
        }

        Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(result.reshape(1));
        result.convertTo(result8, CvType.CV_8UC4, 255/minMaxLocResult.maxVal);

        return ImageUtils.toBitmap(result8);
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        super.onPostExecute(result);

        Toast.makeText(context, "Refocused image computed", Toast.LENGTH_LONG).show();
        mainActivity.setImage(result);
    }
}