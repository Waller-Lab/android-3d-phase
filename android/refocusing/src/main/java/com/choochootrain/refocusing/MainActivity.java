package com.choochootrain.refocusing;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.choochootrain.refocusing.datasets.Dataset;
import com.choochootrain.refocusing.opencv.OpenCVActivity;
import com.choochootrain.refocusing.tasks.ComputeDPCTask;
import com.choochootrain.refocusing.tasks.ComputeDarkfieldTask;
import com.choochootrain.refocusing.tasks.ComputeFocusTask;
import com.choochootrain.refocusing.view.ZoomableImageView;

public class MainActivity extends OpenCVActivity {
    private static final String TAG = "MainActivity";
    private static final int SEEK_SIZE = (int)(Dataset.MAX_DEPTH * 2 / Dataset.DEPTH_INC);
    private static final float SEEK_RESOLUTION = Dataset.DEPTH_INC;

    private TextView imageInfo;
    private Button computeButton;
    private SeekBar focusDepth;
    private ZoomableImageView imageView;

    private String imageType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageInfo = (TextView) findViewById(R.id.image_info);
        imageInfo.setText("Refocused at 0.0 " + Dataset.UNITS);
        //TODO refactor image view handling
        imageType = "result";

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
                String file = Dataset.getResultImagePath(imageType, z);
                Bitmap bmp = BitmapFactory.decodeFile(file);
                if (bmp != null) {
                    imageView.setImage(bmp);
                    imageInfo.setText("Refocused at " + z + " " + Dataset.UNITS);
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
        computeButton.setEnabled(true);
        focusDepth.setEnabled(true);
        Toast.makeText(this, "OpenCV initialized successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id) {
            case R.id.refocus:
                new ComputeFocusTask(MainActivity.this).execute(-Dataset.MAX_DEPTH, Dataset.DEPTH_INC, Dataset.MAX_DEPTH);
                imageType = "result";
                break;
            case R.id.darkfield:
                new ComputeDarkfieldTask(MainActivity.this).execute();
                break;
            case R.id.phase_contrast:
                new ComputeDPCTask(MainActivity.this).execute(-Dataset.MAX_DEPTH, Dataset.DEPTH_INC, Dataset.MAX_DEPTH);
                imageType = "dpc";
                break;
        }

        return true;
    }
}
