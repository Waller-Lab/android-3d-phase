package com.choochootrain.refocusing;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;
import org.opencv.highgui.Highgui.*;


public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    private ImageView imageView;
    private ImageUtils imageUtils;
    private int img;

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
        imageView = (ImageView) findViewById(R.id.imageView);
    }

    private void postOpenCVLoad() {
        imageUtils = new ImageUtils(this);

        Mat img1 = Highgui.imread("/sdcard/cellscope/samples/test.bmp", Highgui.CV_LOAD_IMAGE_GRAYSCALE);
        imageView.setImageBitmap(imageUtils.toBitmap(img1));
    }

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, this, mLoaderCallback);
    }

    protected void toggleImage() {
        this.img++;

        Bitmap bmp = imageUtils.loadBitmap(this.img % 2 == 0 ? R.drawable.test : R.drawable.test2, 500, 400);
        imageView.setImageBitmap(bmp);
        Log.d(TAG, bmp.getWidth() + "x" + bmp.getHeight());
    }

}
