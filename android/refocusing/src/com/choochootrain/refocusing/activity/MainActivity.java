package com.choochootrain.refocusing.activity;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.choochootrain.refocusing.R;
import com.choochootrain.refocusing.datasets.Dataset;
import com.choochootrain.refocusing.tasks.ComputeDPCTask;
import com.choochootrain.refocusing.tasks.ComputeDarkfieldTask;
import com.choochootrain.refocusing.tasks.ComputeRefocusTask;
import com.choochootrain.refocusing.utils.ImageUtils;


public class MainActivity extends OpenCVActivity {
    private static final String TAG = "RefocusingMain";

    private Button computeRefocus;
    private Button viewRefocus;
    private Button computeDPC;
    private Button viewDPC;
    private Button computeDarkfield;
    private Button viewDarkfield;
    
    private static final int ACTIVITY_CHOOSE_FILE = 3;
    public Dataset mDataset =  new Dataset();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Ask user to select a file using any file browser
        Intent chooseFile;
        Intent intent;
        chooseFile = new Intent(Intent.ACTION_GET_CONTENT);
        chooseFile.setType("file/*");
        intent = Intent.createChooser(chooseFile, "Choose a file inside the directory to use");
        startActivityForResult(intent, ACTIVITY_CHOOSE_FILE);

        computeRefocus = (Button) findViewById(R.id.computeRefocus);
        computeRefocus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ComputeRefocusTask(MainActivity.this).execute(mDataset);
            }
        });
        viewRefocus = (Button) findViewById(R.id.viewRefocus);
        viewRefocus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.startViewActivity("refocus", true, mDataset);
            }
        });

        computeDPC = (Button) findViewById(R.id.computeDPC);
        computeDPC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new ComputeDPCTask(MainActivity.this).execute(mDataset);
            }
        });
        viewDPC = (Button) findViewById(R.id.viewDPC);
        viewDPC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.startViewActivity("dpc", true, mDataset);
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
                MainActivity.this.startViewActivity("darkfield", false, mDataset);
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
    protected void startViewActivity(String type, boolean useSlider, Dataset mDataset) {
        Intent intent = new Intent(this, ZoomableImageActivity.class);
        //intent.putExtra("type", mDataset);
        intent.putExtra("type", type);
        intent.putExtra("useSlider", useSlider);
        startActivity(intent);
    }
    
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;
        if(requestCode == ACTIVITY_CHOOSE_FILE)
        {
              Uri uri = data.getData();
              String FilePath = getRealPathFromURI(uri);
              Log.d(TAG,String.format("FilePath is: %s", FilePath));
              // Extract directory:
              mDataset.DATASET_PATH = FilePath.substring(0,FilePath.lastIndexOf("/"));
              Log.d(TAG,String.format("Path is: %s", mDataset.DATASET_PATH));
              File fileList = new File(mDataset.DATASET_PATH);
              
              mDataset.fileList = fileList.listFiles(new FileFilter() {
                  @Override
                  public boolean accept(File pathname) {
                      String name = pathname.getName().toLowerCase();
                      return name.endsWith(".jpeg") && pathname.isFile();
                  }
              });
              /*
              for (int i = 0; i<mDataset.fileList.length; i++)
              {
            	  Log.d(TAG,mDataset.fileList[i].toString());
              }
              */
              
              mDataset.fileCount = mDataset.fileList.length;
              
              // Define the dataset type
              if (FilePath.contains("Brightfield_Scan"))
              {
              	  mDataset.DATASET_TYPE = "brightfield";
                  mDataset.DATASET_HEADER = FilePath.substring(FilePath.lastIndexOf(File.separator)+1,FilePath.lastIndexOf("_scanning_"))+"_scanning_";
                  Log.d(TAG,String.format("BF Scan Header is: %s", mDataset.DATASET_HEADER));
              }
              
          	  else if (FilePath.contains("multimode"))
          	  {
          		  mDataset.DATASET_TYPE = "multimode";
                  mDataset.DATASET_HEADER = "milti_";
                  Log.d(TAG,String.format("Header is: %s", mDataset.DATASET_HEADER));
          	  }
              
          	  else if (FilePath.contains("Full_Scan"))
          	  {
          		  mDataset.DATASET_TYPE = "full_scan";
                  mDataset.DATASET_HEADER = FilePath.substring(FilePath.lastIndexOf(File.separator)+1,FilePath.lastIndexOf("_scanning_"));
                  Log.d(TAG,String.format("Full Scan Header is: %s", mDataset.DATASET_HEADER));
          	  }
              
              // Name the Dataset after the directory
              mDataset.DATASET_NAME = mDataset.DATASET_PATH.substring(0,mDataset.DATASET_PATH.lastIndexOf(File.separator));
        }
    }

	public String getRealPathFromURI(Uri contentUri) {
	    String [] proj      = {MediaStore.Images.Media.DATA};
	    Cursor cursor       = getContentResolver().query( contentUri, proj, null, null,null); 
	    if (cursor == null) return null; 
	    int column_index    = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
	    cursor.moveToFirst();
	    return cursor.getString(column_index);
}
}
