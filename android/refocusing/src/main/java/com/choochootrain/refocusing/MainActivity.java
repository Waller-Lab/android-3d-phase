package com.choochootrain.refocusing;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

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
                new ComputeFocusTask(MainActivity.this).execute(focusDepth.getText().toString());
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

    private class ComputeFocusTask extends AsyncTask<String, Integer, Bitmap> {
        private Context context;
        private ProgressDialog progressDialog;

        public ComputeFocusTask(Context context) {
            this.context = context;
            this.progressDialog = new ProgressDialog(context);
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage("Loading images...");
            progressDialog.show();
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            return imageUtils.computeFocus(Float.parseFloat(focusDepth.getText().toString()));
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
           progressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            if (progressDialog.isShowing())
                progressDialog.dismiss();

            Toast.makeText(MainActivity.this, "Refocused image computed", Toast.LENGTH_LONG).show();
            MainActivity.this.imageView.setImageBitmap(result);
        }
    }
}
