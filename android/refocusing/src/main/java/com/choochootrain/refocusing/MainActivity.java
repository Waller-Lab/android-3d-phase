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
    private int img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.imageView);
        InputStream is = this.getResources().openRawResource(R.drawable.test);
        Bitmap bitmap = BitmapFactory.decodeStream(is);
        imageView.setImageBitmap(bitmap);

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
        Toast.makeText(this, "Loading...", Toast.LENGTH_SHORT).show();
        this.img++;
        InputStream is;
        if (this.img % 2 == 0)
            is = this.getResources().openRawResource(R.drawable.test);
        else
            is = this.getResources().openRawResource(R.drawable.test2);
        Bitmap bmp = BitmapFactory.decodeStream(is);
        imageView.setImageBitmap(bmp);
        Log.d(TAG, bmp.getWidth() + "x" + bmp.getHeight());
        Toast.makeText(this, "Done.", Toast.LENGTH_SHORT).show();
    }

}
