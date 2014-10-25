package com.choochootrain.refocusing.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.TextView;

import com.choochootrain.refocusing.R;
import com.choochootrain.refocusing.datasets.Dataset;
import com.choochootrain.refocusing.view.ZoomableImageView;

public class ZoomableImageActivity extends OpenCVActivity {
    private static final String TAG = "ZoomableImageActivity";


    private TextView imageInfo;
    private SeekBar focusDepth;
    private ZoomableImageView imageView;
    public Dataset mDataset;

    private String imageType;
    private boolean useSlider;
    
    float zMin;
    float seekInc;
    float seekSize;
    float zMax;
    String outDir;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDataset =  (Dataset) getIntent().getSerializableExtra("dataset");
        imageType = getIntent().getExtras().getString("type");
        useSlider = getIntent().getExtras().getBoolean("useSlider", true);
        
        zMin = mDataset.ZMIN;
        seekInc = mDataset.ZINC;
        zMax = mDataset.ZMAX;
        outDir = mDataset.DATASET_PATH+"/Refocused/";
        seekSize = (zMax-zMin)/seekInc;
        
        Log.d(TAG,String.format("zMin is %f, zMax is %f, outdir is %s",zMin,zMax, outDir));
        
        if (useSlider)
            setContentView(R.layout.slider_image_view);
        else
            setContentView(R.layout.image_view);

        imageInfo = (TextView) findViewById(R.id.image_info);

        if (useSlider)
            imageInfo.setText(imageType + " at 0.0 " + Dataset.UNITS);
        else
            imageInfo.setText(imageType);

        imageView = (ZoomableImageView) findViewById(R.id.imageView);

        if (useSlider) {
            focusDepth = (SeekBar) findViewById(R.id.focusDepth);
            focusDepth.setEnabled(false);
            focusDepth.setMax((int)seekSize);
            focusDepth.setProgress((int)(seekSize / 2));
            focusDepth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    float z = (progress - seekSize / 2) * seekInc;
                    
                    String file = "";
                    if (imageType.equals("refocus"))
                    	file = String.format("%s%srefocused_(%d).png", outDir, mDataset.DATASET_HEADER,(int)(z-zMin));
                    else if (imageType.equals("dpc_tb"))
                    	file = String.format("%s%sdpc_tb_(%d).png", outDir, mDataset.DATASET_HEADER,(int)(z-zMin));
                    else if(imageType.equals("dpc_lr"))
                    	file= String.format("%s%sdpc_lr_(%d).png", outDir, mDataset.DATASET_HEADER,(int)(z-zMin));
                    else
                    	Log.d(TAG,"ERROR - Incorrect dataset type!");
                    Log.d(TAG,file);
                    if (file != null) {
                        imageView.setImage(file);
                        imageInfo.setText(imageType + " at " + z + " " + Dataset.UNITS);
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                    //do nothing
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    //do nothing
                }
            });
        } else {
            String file = mDataset.getResultImagePath(imageType);
            imageView.setImage(file);
        }
    }

    @Override
    public void postOpenCVLoad() {
        super.postOpenCVLoad();
        if (useSlider)
            focusDepth.setEnabled(true);
    }
}
