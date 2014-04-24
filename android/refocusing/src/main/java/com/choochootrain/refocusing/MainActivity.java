package com.choochootrain.refocusing;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.choochootrain.refocusing.datasets.Dataset;
import com.choochootrain.refocusing.opencv.OpenCVActivity;
import com.choochootrain.refocusing.tasks.ComputeFocusTask;

public class MainActivity extends OpenCVActivity {
    private static final String TAG = "MainActivity";

    private EditText focusDepth;
    private Button computeButton;
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

        imageView = (ImageView) findViewById(R.id.imageView);
    }

    @Override
    public void postOpenCVLoad() {
        computeButton.setEnabled(true);
        Toast.makeText(this, "OpenCV initialized successfully", Toast.LENGTH_SHORT).show();
    }

    public void setImage(Bitmap bmp) {
        imageView.setImageBitmap(bmp);
    }
}
