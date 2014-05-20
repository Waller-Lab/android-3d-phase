package com.choochootrain.refocusing.activity;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.choochootrain.refocusing.R;
import com.choochootrain.refocusing.datasets.Dataset;
import com.choochootrain.refocusing.tasks.ComputeDPCTask;
import com.choochootrain.refocusing.tasks.ComputeDarkfieldTask;
import com.choochootrain.refocusing.tasks.ComputeRefocusTask;

public class MainActivity extends OpenCVActivity {
    private static final String TAG = "MainActivity";

    private Button computeRefocus;
    private Button viewRefocus;
    private Button computeDPC;
    private Button viewDPC;
    private Button computeDarkfield;
    private Button viewDarkfield;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        computeRefocus = (Button) findViewById(R.id.computeRefocus);
        computeRefocus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ComputeRefocusTask(MainActivity.this).execute(-Dataset.MAX_DEPTH, Dataset.DEPTH_INC, Dataset.MAX_DEPTH);
            }
        });
        viewRefocus = (Button) findViewById(R.id.viewRefocus);
        viewRefocus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.startViewActivity("refocus", true);
            }
        });

        computeDPC = (Button) findViewById(R.id.computeDPC);
        computeDPC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ComputeDPCTask(MainActivity.this).execute(-Dataset.MAX_DEPTH, Dataset.DEPTH_INC, Dataset.MAX_DEPTH);
            }
        });
        viewDPC = (Button) findViewById(R.id.viewDPC);
        viewDPC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.startViewActivity("dpc", true);
            }
        });

        computeDarkfield = (Button) findViewById(R.id.computeDarkfield);
        computeDarkfield.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ComputeDarkfieldTask(MainActivity.this).execute();
            }
        });
        viewDarkfield = (Button) findViewById(R.id.viewDarkfield);
        viewDarkfield.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.startViewActivity("darkfield", false);
            }
        });
    }

    //ensure app is in portrait orientation
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    //fire intent to start activity with proper configuration for type
    protected void startViewActivity(String type, boolean useSlider) {
        Intent intent = new Intent(this, ZoomableImageActivity.class);
        intent.putExtra("type", type);
        intent.putExtra("useSlider", useSlider);
        startActivity(intent);
    }
}
