package com.choochootrain.refocusing.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Core.MinMaxLocResult;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.highgui.Highgui;

import prefuse.util.ColorMap;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.choochootrain.refocusing.datasets.Dataset;

public class ComputePhaseHeightMap extends ImageProgressTask {

	Dataset mDataset;

	public ComputePhaseHeightMap(Context context) {
        super(context);
        this.progressDialog.setMessage("Constructing heightmap image...");
    }
	
	@Override
	protected Void doInBackground(Dataset... params) {
		// TODO Auto-generated method stub
		mDataset = params[0];
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inPreferredConfig = Bitmap.Config.ARGB_8888;
		Log.d("filepath", mDataset.getTIEInputImagePath());
		Log.d("filepath", mDataset.getTIEResultImagePath());
		
		Bitmap bitmap = BitmapFactory.decodeFile(mDataset.getTIEInputImagePath(), options);
		Mat result = getPhaseImage(bitmap, mDataset);
		
		File outputDir = new File(mDataset.TIE_RESULT_DIR);
		if(!outputDir.exists()){
			outputDir.mkdir();
		}
		//writeToFile(result, mDataset.getTIEResultImagePath());
		Highgui.imwrite(mDataset.getTIEResultImagePath(), result);
		return null;
	}
	
	private Mat getPhaseImage(Bitmap in, Dataset mDataset){
		Mat inMat = new Mat(in.getHeight(), in.getWidth(), CvType.CV_8UC4);
		Utils.bitmapToMat(in, inMat);
		
		double[] minMax = computePhaseImage(inMat.getNativeObjAddr());
		
		File minMaxFile = new File(mDataset.getTIEInfoFilePath());
		FileWriter writer = null;
		
		try{
			writer  = new FileWriter(minMaxFile);
			String data = String.format("%s:%f , %s:%f","min", minMax[0], "max", minMax[1]);
			writer.write(data);
		} catch (IOException e) {
			Log.d("file write", e.toString());
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		inMat.convertTo(inMat, CvType.CV_8UC4);
		

		
		/**Bitmap.Config conf = Bitmap.Config.ARGB_8888;
		Bitmap out = Bitmap.createBitmap(inMat.height(), inMat.width(), conf);
		Utils.matToBitmap(inMat, out);
		return out;**/
		return inMat;
	}
	
	
	private int[] rawToGreyscale(double[] in){
		double min = Double.POSITIVE_INFINITY;
		double max = Double.NEGATIVE_INFINITY;
		for(int i = 0; i < in.length; i++){
			if(in[i] > max){
				max = in[i];
			} else if(in[i] < min){
				min = in[i];
			}
		}
		
		int[] colorArray = new int[256];
		for(int i = 0; i < 256; i++){
			int color = 255-i;
			colorArray[i] = (color << 16) + (color << 8) + color;
		}
		ColorMap map = new ColorMap(colorArray, min,max);
		
		int[] out = new int[in.length];
		for(int i = 0; i < out.length; i++ ){
			out[i] = (0xFF << 24) + map.getColor(in[i]);
		}
		return out;
	}
	
	/***** utility/helper functions *****/
	
	private int[] meshGridX(int width){
		int[] mesh_x = new int[width];
		for(int i = 0; i < width; i++){
			if(i <= (width-1)/2){
				mesh_x[i] = i;
			} else {
				mesh_x[i] = i - width;
			}
		}
		return mesh_x;
	}
	
	private int[] meshGridY(int height){
		int[] mesh_y = new int[height];
		for(int i = 0; i < height; i++){
			if(i <= (height-1)/2){
				mesh_y[i] = -1*i;
			} else {
				mesh_y[i] = height - i;
			}
		}
		return mesh_y;
	}
	
	private double mean(double[] in){
		double sum = 0;
		for(int i = 0; i < in.length; i++){
			sum += in[i];
		}
		return sum/in.length;
	}
	
	private void writeToFile(Bitmap bmp, String path){
		FileOutputStream out = null;
		try {
		    out = new FileOutputStream(path);
		    bmp.compress(Bitmap.CompressFormat.PNG, 90, out);
		} catch (Exception e) {
		    e.printStackTrace();
		} finally {
		    try {
		        if (out != null) {
		            out.close();
		        }
		    } catch (IOException e) {
		        e.printStackTrace();
		    }
		}
	}
	
	/** Native Functions **/
	public native double[] computePhaseImage(long inputAddr);

}
