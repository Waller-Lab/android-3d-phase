package com.choochootrain.refocusing.tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

import com.choochootrain.refocusing.datasets.Dataset;
import com.choochootrain.refocusing.utils.ImageUtils;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ComputeRefocusTask extends ImageProgressTask{
    private static final String TAG = "ComputeRefocusTask";
    public ComputeRefocusTask(Context context) {
        super(context);
        this.progressDialog.setMessage("Assembling refocused images...");
    }
    Dataset mDataset = null;
    double tanh_lit[];
    double tanv_lit[];
    byte[][] fileByteList;
    
    @Override
    protected Void doInBackground(Dataset... params) {
    	mDataset = params[0];
        float zMin = mDataset.ZMIN;
        float zInc = mDataset.ZINC;
        float zMax = mDataset.ZMAX;
        String outDir = mDataset.DATASET_PATH+"/Refocused/";
        File outFile = new File(outDir);
        outFile.mkdirs();
        
        // Build rotation matrix
        double globalRotation = Math.PI/4;
        double rotationTransform[][] = new double[3][3];
        rotationTransform[0][0] =  Math.cos(globalRotation);
        rotationTransform[0][1] = -1*Math.sin(globalRotation);
        rotationTransform[0][2] = 0.0;
        rotationTransform[1][0] = Math.sin(globalRotation);
        rotationTransform[1][1] = Math.cos(globalRotation);
        rotationTransform[1][2] = 0.0;
        rotationTransform[2][0] = 0.0;
        rotationTransform[2][1] = 0.0;
        rotationTransform[2][2] = 1.0;

        double rotatedCoordinates[][] = new double[mDataset.domeCoordinates.length][3];
        double domeCoordinates[][] = new double[mDataset.domeCoordinates.length][3];
        
        // Convert the coordinates to floats
        for (int i=0; i<mDataset.domeCoordinates.length; i++)
        {
        	domeCoordinates[i][0] = (double)mDataset.domeCoordinates[i][0];
        	domeCoordinates[i][1] = (double)mDataset.domeCoordinates[i][1];
        	domeCoordinates[i][2] = (double)mDataset.domeCoordinates[i][2];
        }
        
        rotatedCoordinates = ImageUtils.multiplyArray(domeCoordinates, rotationTransform);
        
        tanh_lit = new double[mDataset.domeCoordinates.length];
        tanv_lit = new double[mDataset.domeCoordinates.length];
        
        for (int i=0; i<mDataset.domeCoordinates.length; i++)
        {
        	tanh_lit[i] = rotatedCoordinates[i][0]/rotatedCoordinates[i][2];
        	tanv_lit[i] = rotatedCoordinates[i][1]/rotatedCoordinates[i][2];
        }
        
        //TODO - Load as many images into ram as possible
        //TODO - load compressed images rather than mats
        fileByteList = new byte[mDataset.fileCount][(int) mDataset.fileList[0].length()*2];
        
        for (int idx = 0; idx < mDataset.fileCount; idx++) {
        	File file = mDataset.fileList[idx];
    	    int size = (int) file.length();
    	    byte[] bytes = new byte[size];
    	    try {
    	        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
    	        buf.read(bytes, 0, bytes.length);
    	        buf.close();
    	    } catch (FileNotFoundException e) {
    	        // TODO Auto-generated catch block
    	        e.printStackTrace();
    	    } catch (IOException e) {
    	        // TODO Auto-generated catch block
    	        e.printStackTrace();
    	    }
    	    fileByteList[idx] = bytes;
    	
        }

        for (float z = zMin; z <= zMax; z += zInc) {
            float progress = (z - zMin) / (zMax - zMin);
            onProgressUpdate(-1,(int)(progress * 100));
            Bitmap[] results = computeFocus(z);
            String refocused_fName = String.format("%s%srefocused_(%d).png", outDir, mDataset.DATASET_HEADER,(int)(z-zMin));
            String dpc_tb_fName = String.format("%s%sdpc_tb_(%d).png", outDir, mDataset.DATASET_HEADER,(int)(z-zMin));
            String dpc_lr_fName = String.format("%s%sdpc_lr_(%d).png", outDir, mDataset.DATASET_HEADER,(int)(z-zMin));
            
            File refocusedBmp = new File(refocused_fName);
            File dpc_tb_Bmp = new File(dpc_tb_fName);
            File dpc_lr_Bmp = new File(dpc_lr_fName);
            
            try {
                FileOutputStream fos = new FileOutputStream(refocusedBmp);
                results[0].compress(Bitmap.CompressFormat.PNG, 100, fos);
            } catch (FileNotFoundException e) {
                return null;
            }
            try {
                FileOutputStream fos = new FileOutputStream(dpc_tb_Bmp);
                results[1].compress(Bitmap.CompressFormat.PNG, 100, fos);
            } catch (FileNotFoundException e) {
                return null;
            }
            try {
                FileOutputStream fos = new FileOutputStream(dpc_lr_Bmp);
                results[2].compress(Bitmap.CompressFormat.PNG, 100, fos);
            } catch (FileNotFoundException e) {
                return null;
            }
        }
        updateFileStructure(outDir);
        return null;
    }

    private Bitmap[] computeFocus(float z) {
        int width = mDataset.WIDTH-2*mDataset.XCROP;
        int height = mDataset.HEIGHT-2*mDataset.YCROP;

        Mat result = new Mat(height, width, CvType.CV_32FC4);
        Mat result8 = new Mat(height, width, CvType.CV_8UC4);
        
        Mat dpc_result_tb = new Mat(height, width, CvType.CV_32FC4);
        Mat dpc_result_tb8 = new Mat(height, width, CvType.CV_8UC4);
        
        Mat dpc_result_lr = new Mat(height, width, CvType.CV_32FC4);
        Mat dpc_result_lr8 = new Mat(height, width, CvType.CV_8UC4);
        
        Mat img;
        Mat img32 = new Mat(height, width, CvType.CV_32FC4);
        Mat shifted;
        
        for (int idx = 0; idx < mDataset.fileCount; idx++) {
	        
            //img = ImageUtils.toMat(BitmapFactory.decodeFile(mDataset.fileList[idx].toString()));
	        img = ImageUtils.toMat(BitmapFactory.decodeByteArray(fileByteList[idx], 0, fileByteList[idx].length));
            img = img.submat( mDataset.YCROP, mDataset.HEIGHT-mDataset.YCROP,mDataset.XCROP, mDataset.WIDTH-mDataset.XCROP);
            img.convertTo(img32, result.type());
            
            // Grab actual hole number from filename
            String fName = mDataset.fileList[idx].toString();
            String hNum =fName.substring(fName.indexOf("_scanning_")+10,fName.indexOf(".jpeg"));
            int holeNum = Integer.parseInt(hNum);
            //Log.d(TAG,String.format("BF Scan Header is: %s", hNum));

            // Calculate these based on array coordinates
            int xShift = (int) Math.round(z*tanh_lit[holeNum]);
            int yShift = (int) Math.round(z*tanv_lit[holeNum]);

            shifted = ImageUtils.circularShift(img32, yShift, xShift);

            //add to result
            //Log.d(TAG,String.format("result size: %dx%d , shifted size: %dx%d",result.width(),result.height(),shifted.width(),shifted.height()));
            Core.add(result, shifted, result);
            
            if (tanh_lit[holeNum] <= 0) //add LHS
                Core.add(dpc_result_lr, shifted, dpc_result_lr);
            /*
            else //subtract RHS
                Core.subtract(dpc_result_lr, shifted, dpc_result_lr);
                */
            
            if (tanv_lit[holeNum] <= 0) //add Top
                Core.add(dpc_result_tb, shifted, dpc_result_tb);
            /*
            else //subtract Bottom
                Core.subtract(dpc_result_tb, shifted, dpc_result_tb);
                */

            float progress = ((float)((idx+1) / (float)mDataset.fileCount));
            onProgressUpdate((int)(progress * 100),-1);
            Log.d(TAG,String.format("progress: %f", progress));
        }

        Core.MinMaxLocResult minMaxLocResult = Core.minMaxLoc(result.reshape(1));
        result.convertTo(result8, CvType.CV_8UC4, 255/minMaxLocResult.maxVal);
        
        minMaxLocResult = Core.minMaxLoc(dpc_result_lr.reshape(1));
        dpc_result_lr8.convertTo(dpc_result_lr8, CvType.CV_8UC4, 255/minMaxLocResult.maxVal);
        
        minMaxLocResult = Core.minMaxLoc(dpc_result_tb.reshape(1));
        dpc_result_tb8.convertTo(dpc_result_tb8, CvType.CV_8UC4, 255/minMaxLocResult.maxVal);
        
        Bitmap[] outputBitmaps = new Bitmap[3];
        outputBitmaps[0] = ImageUtils.toBitmap(result8);
        outputBitmaps[1] = ImageUtils.toBitmap(dpc_result_lr8);
        outputBitmaps[2] = ImageUtils.toBitmap(dpc_result_tb8);

        return outputBitmaps;
    }
    
    public void updateFileStructure(String currPath) { 
    	  File f = new File(currPath);
    	  File[] fileList = f.listFiles();
    	  ArrayList<String> arrayFiles = new ArrayList<String>();
    	     if (!(fileList.length == 0))
    	     {
    	            for (int i=0; i<fileList.length; i++) 
    	                arrayFiles.add(currPath+"/"+fileList[i].getName());
    	     }
    	     
         String[] fileListString = new String[arrayFiles.size()];
         fileListString = arrayFiles.toArray(fileListString);
         MediaScannerConnection.scanFile(context,
                  fileListString, null,
                  new MediaScannerConnection.OnScanCompletedListener() {
                      public void onScanCompleted(String path, Uri uri) {
                          //Log.i("TAG", "Finished scanning " + path);
                      }
                  });
    	}
}