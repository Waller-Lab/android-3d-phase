package com.choochootrain.refocusing;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.choochootrain.refocusing.image.ImageUtils;
import com.choochootrain.refocusing.opencv.OpenCVActivity;
import com.choochootrain.refocusing.tasks.ComputeFocusTask;

public class MainActivity extends OpenCVActivity {
    private static final String TAG = "MainActivity";
    private static final String DATASET = "10-1-13";

    private EditText focusDepth;
    private Button computeButton;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        focusDepth = (EditText) findViewById(R.id.focusDepth);

        computeButton = (Button) findViewById(R.id.computeButton);
        computeButton.setEnabled(false);
        computeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float focus = Float.parseFloat(focusDepth.getText().toString());
                new ComputeFocusTask(MainActivity.this, DATASET, focus).execute(focusDepth.getText().toString());
            }
        });

        imageView = (ImageView) findViewById(R.id.imageView);
    }

    @Override
    public void postOpenCVLoad() {
        computeButton.setEnabled(true);
    }

    public void setImage(Bitmap bmp) {
        imageView.setImageBitmap(bmp);
    }
}
