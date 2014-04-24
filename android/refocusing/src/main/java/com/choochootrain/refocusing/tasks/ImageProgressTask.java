package com.choochootrain.refocusing.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

public abstract class ImageProgressTask extends AsyncTask<Float, Integer, Bitmap> {
    protected Context context;
    protected ProgressDialog progressDialog;

    public ImageProgressTask(Context context) {
        this.context = context;
        this.progressDialog = new ProgressDialog(context);
        this.progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
    }

    @Override
    protected void onPreExecute() {
        progressDialog.show();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        progressDialog.setProgress(progress[0]);
        if (progress.length > 1)
            progressDialog.setSecondaryProgress(progress[1]);
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        if (progressDialog.isShowing())
            progressDialog.dismiss();
    }
}