package com.choochootrain.refocusing.tasks;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.Toast;

import com.choochootrain.refocusing.image.ImageUtils;
import com.choochootrain.refocusing.MainActivity;

public class ComputeFocusTask extends AsyncTask<Float, Integer, Bitmap> {
    private MainActivity context;
    private String dataset;
    private ProgressDialog progressDialog;

    public ComputeFocusTask(MainActivity context, String dataset) {
        this.context = context;
        this.dataset = dataset;
        this.progressDialog = new ProgressDialog(context);
    }

    @Override
    protected void onPreExecute() {
        progressDialog.setMessage("Assembling refocused image...");
        progressDialog.show();
    }

    @Override
    protected Bitmap doInBackground(Float... params) {
        return ImageUtils.computeFocus(dataset, params[0]);
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