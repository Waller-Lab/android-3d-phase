package com.choochootrain.refocusing;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Toast;

import com.choochootrain.refocusing.datasets.Dataset;
import com.choochootrain.refocusing.opencv.OpenCVActivity;
import com.choochootrain.refocusing.tasks.ComputeFocusTask;

public class MainActivity extends OpenCVActivity {
    private static final String TAG = "MainActivity";
    private static final int SEEK_SIZE = (int)(Dataset.MAX_DEPTH * 2 / Dataset.DEPTH_INC);
    private static final float SEEK_RESOLUTION = Dataset.DEPTH_INC;

    private Button computeButton;
    private SeekBar focusDepth;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        computeButton = (Button) findViewById(R.id.computeButton);
        computeButton.setEnabled(false);
        computeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ComputeFocusTask(MainActivity.this).execute(-Dataset.MAX_DEPTH, Dataset.DEPTH_INC, Dataset.MAX_DEPTH);
            }
        });

        focusDepth = (SeekBar) findViewById(R.id.focusDepth);
        focusDepth.setEnabled(false);
        focusDepth.setMax(SEEK_SIZE);
        focusDepth.setProgress(SEEK_SIZE/2);
        focusDepth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float z = (progress - SEEK_SIZE/2) * SEEK_RESOLUTION;
                String file = Dataset.getResultImagePath(z);
                Bitmap bmp = BitmapFactory.decodeFile(file);
                if (bmp != null)
                    imageView.setImageBitmap(bmp);
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

        imageView = (ImageView) findViewById(R.id.imageView);
    }

    @Override
    public void postOpenCVLoad() {
        computeButton.setEnabled(true);
        focusDepth.setEnabled(true);
        Toast.makeText(this, "OpenCV initialized successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }
}
