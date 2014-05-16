package com.choochootrain.refocusing.activity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.SeekBar;
import android.widget.TextView;

import com.choochootrain.refocusing.R;
import com.choochootrain.refocusing.datasets.Dataset;
import com.choochootrain.refocusing.view.ZoomableImageView;

public class ZoomableImageActivity extends OpenCVActivity {
    private static final String TAG = "ZoomableImageActivity";
    private static final int SEEK_SIZE = (int)(Dataset.MAX_DEPTH * 2 / Dataset.DEPTH_INC);
    private static final float SEEK_RESOLUTION = Dataset.DEPTH_INC;

    private TextView imageInfo;
    private SeekBar focusDepth;
    private ZoomableImageView imageView;

    private String imageType;
    private boolean useSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        imageType = getIntent().getExtras().getString("type");
        useSlider = getIntent().getExtras().getBoolean("useSlider", true);

        if (useSlider)
            setContentView(R.layout.slider_image_view);
        else
            setContentView(R.layout.image_view);

        imageInfo = (TextView) findViewById(R.id.image_info);

        if (useSlider)
            imageInfo.setText(imageType + " at 0.0 " + Dataset.UNITS);
        else
            imageInfo.setText(imageType);

        imageView = (ZoomableImageView) findViewById(R.id.imageView);

        if (useSlider) {
            focusDepth = (SeekBar) findViewById(R.id.focusDepth);
            focusDepth.setEnabled(false);
            focusDepth.setMax(SEEK_SIZE);
            focusDepth.setProgress(SEEK_SIZE / 2);
            focusDepth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    float z = (progress - SEEK_SIZE / 2) * SEEK_RESOLUTION;
                    String file = Dataset.getResultImagePath(imageType, z);
                    Bitmap bmp = BitmapFactory.decodeFile(file);
                    if (bmp != null) {
                        imageView.setImage(bmp);
                        imageInfo.setText(imageType + " at " + z + " " + Dataset.UNITS);
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
        } else {
            Bitmap bmp = BitmapFactory.decodeFile(Dataset.getResultImagePath(imageType));
            imageView.setImage(bmp);
        }
    }

    @Override
    public void postOpenCVLoad() {
        super.postOpenCVLoad();
        if (useSlider)
            focusDepth.setEnabled(true);
    }
}
