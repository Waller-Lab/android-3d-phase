package com.choochootrain.refocusing.tasks;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.Toast;

import com.choochootrain.refocusing.ImageUtils;
import com.choochootrain.refocusing.MainActivity;

public class ComputeFocusTask extends AsyncTask<String, Integer, Bitmap> {
    private MainActivity context;
    private ImageUtils imageUtils;
    private float focusDepth;
    private ProgressDialog progressDialog;

    public ComputeFocusTask(MainActivity context, ImageUtils imageUtils, float focusDepth) {
        this.context = context;
        this.imageUtils = imageUtils;
        this.focusDepth = focusDepth;
        this.progressDialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        progressDialog.setMessage("Assembling refocused image...");
        progressDialog.show();
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        return imageUtils.computeFocus(focusDepth);
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
       progressDialog.setProgress(progress[0]);
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        if (progressDialog.isShowing())
            progressDialog.dismiss();

        Toast.makeText(context, "Refocused image computed", Toast.LENGTH_LONG).show();
        context.setImage(result);
    }
}