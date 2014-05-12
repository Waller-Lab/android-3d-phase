package com.choochootrain.refocusing.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.choochootrain.refocusing.R;
import com.choochootrain.refocusing.datasets.Dataset;
import com.choochootrain.refocusing.opencv.OpenCVActivity;
import com.choochootrain.refocusing.view.ZoomableImageView;

public class ZoomableImageActivity extends OpenCVActivity {
    private static final String TAG = "ZoomableImageActivity";
    private static final int SEEK_SIZE = (int)(Dataset.MAX_DEPTH * 2 / Dataset.DEPTH_INC);
    private static final float SEEK_RESOLUTION = Dataset.DEPTH_INC;

    private TextView imageInfo;
    private SeekBar focusDepth;
    private ZoomableImageView imageView;

    private String imageType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.slider_image_view);

        imageInfo = (TextView) findViewById(R.id.image_info);
        imageInfo.setText("Refocused at 0.0 " + Dataset.UNITS);
        //TODO refactor image view handling
        imageType = "result";

        //TODO handle images with no slider
        focusDepth = (SeekBar) findViewById(R.id.focusDepth);
        focusDepth.setEnabled(false);
        focusDepth.setMax(SEEK_SIZE);
        focusDepth.setProgress(SEEK_SIZE/2);
        focusDepth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float z = (progress - SEEK_SIZE/2) * SEEK_RESOLUTION;
                String file = Dataset.getResultImagePath(imageType, z);
                Bitmap bmp = BitmapFactory.decodeFile(file);
                if (bmp != null) {
                    imageView.setImage(bmp);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //do nothing
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //do nothing
            }
        });

        imageView = (ZoomableImageView) findViewById(R.id.imageView);
    }

    @Override
    public void postOpenCVLoad() {
        super.postOpenCVLoad();
        focusDepth.setEnabled(true);
    }
}
