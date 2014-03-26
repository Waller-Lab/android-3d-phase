package com.choochootrain.refocusing;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.InputStream;


public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    private ImageView imageView;
    private ImageUtils imageUtils;
    private int img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageUtils = new ImageUtils(this);

        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setImageBitmap(imageUtils.loadBitmap(R.drawable.test));

        imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP)
                    MainActivity.this.toggleImage();
                return true;
            }
        });
    }

    protected void toggleImage() {
        this.img++;

        Bitmap bmp = imageUtils.loadBitmap(this.img % 2 == 0 ? R.drawable.test : R.drawable.test2);
        imageView.setImageBitmap(bmp);
        Log.d(TAG, bmp.getWidth() + "x" + bmp.getHeight());
    }

}
