package com.choochootrain.refocusing;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.choochootrain.refocusing.tasks.ComputeFocusTask;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;


public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";
    private static final String DATASET = "10-1-13";

    private EditText focusDepth;
    private Button computeButton;
    private ImageView imageView;
    private ImageUtils imageUtils;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native libraries after(!) OpenCV initialization
                    postOpenCVLoad();
                } break;
                default: {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

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
                new ComputeFocusTask(MainActivity.this, imageUtils, focus).execute(focusDepth.getText().toString());
            }
        });

        imageView = (ImageView) findViewById(R.id.imageView);
    }

    private void postOpenCVLoad() {
        imageUtils = new ImageUtils(this, DATASET);
        computeButton.setEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, this, mLoaderCallback);
    }

    public void setImage(Bitmap bmp) {
        imageView.setImageBitmap(bmp);
    }
}
