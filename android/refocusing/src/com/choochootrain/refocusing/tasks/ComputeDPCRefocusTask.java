package com.choochootrain.refocusing.tasks;

import android.content.Context;
import android.util.Log;

import com.choochootrain.refocusing.datasets.Dataset;

public class ComputeDPCRefocusTask extends ImageProgressTask {

	Dataset mDataset;

	public ComputeDPCRefocusTask(Context context) {
        super(context);
        this.progressDialog.setMessage("Calculating Refocus Stack from DPC Data...");
    }
	
	@Override
	protected Void doInBackground(Dataset... params) {
		// TODO Auto-generated method stub
		mDataset = params[0];
		Log.d("DPC", "native function call");
		computeDPCRefocus(mDataset.DPC_ZMIN, mDataset.DPC_ZSTEP, mDataset.DPC_ZMAX, mDataset.DPC_FOCUS_DATASET_ROOT);
		Log.d("DPC", "done with native function call");
		return null;
	}
	
	/** Native Functions **/
	public native void computeDPCRefocus(float zMin, float zStep, float zMax, String datasetRoot);

}
