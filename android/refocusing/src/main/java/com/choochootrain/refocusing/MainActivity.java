package com.choochootrain.refocusing;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;


public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

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
                float depth = Float.parseFloat(focusDepth.getText().toString());
                Bitmap result = imageUtils.computeFocus("10-1-13", depth);
                imageView.setImageBitmap(result);
            }
        });

        imageView = (ImageView) findViewById(R.id.imageView);
    }

    private void postOpenCVLoad() {
        imageUtils = new ImageUtils(this);
        computeButton.setEnabled(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, this, mLoaderCallback);
    }
}
