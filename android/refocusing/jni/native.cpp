#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/contrib/contrib.hpp>
#include <android/log.h>
#include <vector>
#include <iostream>
#include <string>
#include <stdio.h>
#include <dirent.h>
#include "RefocusingCoordinates.h"

#define FILENAME_LENGTH 32
#define FILE_HOLENUM_DIGITS 3

using namespace std;
using namespace cv;

class R_image{
  
  public:
        cv::Mat Image;
        int led_num;
        float tan_x;
        float tan_y;
};

extern "C" {
int* meshgrid_x(int width);
int* meshgrid_y(int height);
int loadImages(string datasetRoot, vector<R_image> *images);
void circularShift(Mat img, Mat result, int x, int y);
void computeFocusDPC(vector<R_image> iStack, int fileCount, float z, int width, int height, int xcrop, int ycrop, Mat* results);

JNIEXPORT void JNICALL Java_com_choochootrain_refocusing_tasks_ComputeDPCRefocusTask_computeDPCRefocus(JNIEnv* env, jobject, jfloat zMin, jfloat zStep, 
                                                                                                       jfloat zMax, jstring datasetRoot);
JNIEXPORT jdoubleArray JNICALL Java_com_choochootrain_refocusing_tasks_ComputePhaseHeightMap_computePhaseImage(JNIEnv* env, jobject, jlong addrInput);

JNIEXPORT void JNICALL Java_com_choochootrain_refocusing_tasks_ComputeDPCRefocusTask_computeDPCRefocus(JNIEnv* env, jobject, jfloat zMin, jfloat zStep,
                                                                                                       jfloat zMax, jstring jDatasetRoot){
    __android_log_write(ANDROID_LOG_INFO, "DPC", "entered native method");
    // perhaps convert jfloats to floats?
    const char *datasetRoot = (env)->GetStringUTFChars(jDatasetRoot, 0); 

    vector<R_image> * imageStack;
    imageStack = new vector<R_image>;
    int16_t imgCount = loadImages(datasetRoot,imageStack);
    Mat results[3];

    for (float zDist = zMin; zDist <= zMax; zDist += zStep)
    {
        computeFocusDPC(*imageStack, imgCount, zDist, imageStack->at(0).Image.cols, imageStack->at(0).Image.rows, 0, 0, results);
       
        char bfFilename[FILENAME_LENGTH];
        char dpcLRFilename[FILENAME_LENGTH];
        char dpcTBFilename[FILENAME_LENGTH];
        snprintf(bfFilename,sizeof(bfFilename), "/sdcard/Pictures/Refocused/BF_%3f.png",zDist);
        snprintf(dpcLRFilename,sizeof(dpcLRFilename), "/sdcard/Pictures/Refocused/DPCLR_%.2f.png",zDist);
        snprintf(dpcTBFilename,sizeof(dpcTBFilename), "/sdcard/Pictures/Refocused/DPCTB_%.2f.png",zDist);
       
        imwrite(bfFilename, results[0]);
        imwrite(dpcLRFilename, results[1]);
        imwrite(dpcTBFilename, results[2]);
    }
}

JNIEXPORT jdoubleArray JNICALL Java_com_choochootrain_refocusing_tasks_ComputePhaseHeightMap_computePhaseImage(JNIEnv* env, jobject, jlong addrInput){
    Mat& I = *(Mat*)addrInput;
    
    vector<Mat> rgb;
    split(I, rgb);

    double k = 10;
    double delta_z = 10;
    double epsilon = 10000000000; // probably not a great idea
    
    Mat red, green, blue;
    rgb[0].convertTo(blue, CV_64F);
    rgb[1].convertTo(green, CV_64F);
    rgb[2].convertTo(red, CV_64F);
    
    double offset = 10;
    
    Mat G = (k/(green + offset)).mul((blue - red)*(1.0/delta_z)); 
     
    Mat padded;                            //expand input image to optimal size
    int m = getOptimalDFTSize( G.rows );
    int n = getOptimalDFTSize( G.cols ); // on the border add zero values
    copyMakeBorder(G, padded, 0, m - G.rows, 0, n - G.cols, BORDER_CONSTANT, Scalar::all(0));

    Mat planes[] = {Mat_<double>(padded), Mat::zeros(padded.size(), CV_64F)};
    Mat complexG;
    merge(planes, 2, complexG);         // Add to the expanded another plane with zeros

    dft(complexG, complexG);            // this way the result may fit in the source matrix

    // compute the magnitude and switch to logarithmic scale
    // => log(1 + sqrt(Re(DFT(I))^2 + Im(DFT(I))^2))
    split(complexG, planes);                   // planes[0] = Re(DFT(I), planes[1] = Im(DFT(I))
    
    int * mesh_x = meshgrid_x(planes[0].cols);
    int * mesh_y = meshgrid_y(planes[0].rows);
    double pi_squared = pow(M_PI, 2);

    for(int y = 0; y < planes[0].rows; y++){
        for(int x = 0; x < planes[0].cols; x++){
            double denom = -4*pi_squared*(pow(mesh_x[x], 2) + pow(mesh_y[y], 2)) + epsilon;
            planes[0].at<double>(y,x) /= denom;
            planes[1].at<double>(y,x) /= denom;
        }
    }

    merge(planes, 2, complexG);
    dft(complexG, I, DFT_INVERSE|DFT_REAL_OUTPUT); 
    
    double min, max;
    minMaxLoc(I, &min, &max);

    normalize(I, I, 0, 1, CV_MINMAX);
    I.convertTo(I, CV_8U, 255.0);
    I = 255 - I;

    jdoubleArray minMax;
    minMax = env->NewDoubleArray(2);
    
    jdouble tempMinMax[2];
    tempMinMax[0] = min;
    tempMinMax[1] = max;

    env->SetDoubleArrayRegion(minMax, 0, 2, tempMinMax);
    return minMax;
}

int* meshgrid_x(int width){
    int * mesh_x = new int[width];
    for(int i = 0; i < width; i++){
        if(i <= (width-1)/2){
            mesh_x[i] = i;
        } else {
            mesh_x[i] = i - width;
        }
    }
    return mesh_x; 
}

int* meshgrid_y(int height){
    int * mesh_y = new int[height];
    for(int i = 0; i < height; i++){
        if(i <= (height-1)/2){
            mesh_y[i] = -1*i;
        } else {
            mesh_y[i] = height - i;
        }
    }
    return mesh_y; 
} 

void circularShift(Mat img, Mat result, int x, int y){
    int w = img.cols;
    int h  = img.rows;

    int shiftR = x % w;
    int shiftD = y % h;
    
    if (shiftR < 0)
        shiftR += w;
    
    if (shiftD < 0)
        shiftD += h;

    cv::Rect gate1(0, 0, w-shiftR, h-shiftD);
    cv::Rect out1(shiftR, shiftD, w-shiftR, h-shiftD);
    
    cv::Rect gate2(w-shiftR, 0, shiftR, h-shiftD); //rect(x, y, width, height)
    cv::Rect out2(0, shiftD, shiftR, h-shiftD);

    cv::Rect gate3(0, h-shiftD, w-shiftR, shiftD);
    cv::Rect out3(shiftR, 0, w-shiftR, shiftD);

    cv::Rect gate4(w-shiftR, h-shiftD, shiftR, shiftD);
    cv::Rect out4(0, 0, shiftR, shiftD);
       
    cv::Mat shift1 = img ( gate1 );
    cv::Mat shift2 = img ( gate2 );
    cv::Mat shift3 = img ( gate3 );
    cv::Mat shift4 = img ( gate4 );
   
//   if(shiftD != 0 && shiftR != 0)

    shift1.copyTo(cv::Mat(result, out1));
    if(shiftR != 0)
        shift2.copyTo(cv::Mat(result, out2));
    if(shiftD != 0)
        shift3.copyTo(cv::Mat(result, out3));
    if(shiftD != 0 && shiftR != 0)
        shift4.copyTo(cv::Mat(result, out4));

    //result.convertTo(result,img.type());
}

int loadImages(string datasetRoot, vector<R_image> *images) {

    DIR *dir;
    struct dirent *ent;
    if ((dir = opendir (datasetRoot.c_str())) != NULL) {
      
      int num_images = 0;
      while ((ent = readdir (dir)) != NULL) {
        //add to list
        string fileName = ent->d_name;
        string filePrefix = "_scanning_";
        if (fileName.compare(".") != 0 && fileName.compare("..") != 0)
        {
           string holeNum = fileName.substr(fileName.find(filePrefix)+filePrefix.length(),FILE_HOLENUM_DIGITS);
           //cout << "Filename is: " << fileName << endl;
         //  cout << "Filename is: " << fileName << ". HoleNumber is: " << holeNum << endl;
        R_image currentImage;
        currentImage.led_num = atoi(holeNum.c_str());
        //currentImage.Image = imread(datasetRoot + "/" + fileName, CV_8UC1);
        currentImage.Image = imread(datasetRoot + "/" + fileName, -1);//apparently - loads with a?

        currentImage.tan_x = -domeCoordinates[currentImage.led_num][0] / domeCoordinates[currentImage.led_num][2];
        currentImage.tan_y = domeCoordinates[currentImage.led_num][1] / domeCoordinates[currentImage.led_num][2];
        (*images).push_back(currentImage);
        num_images ++;
        }
      }
      closedir (dir);
      return num_images;

    } else {
      /* could not open directory */
      perror ("");
      return EXIT_FAILURE;
    }
}

void computeFocusDPC(vector<R_image> iStack, int fileCount, float z, int width, int height, int xcrop, int ycrop, Mat* results) {
    int newWidth = width;// - 2*xcrop;
    int newHeight = height;// - 2*ycrop;

    cv::Mat bf_result = cv::Mat(newHeight, newWidth, CV_16UC3, double(0));
     cv::Mat dpc_result_tb = cv::Mat(newHeight, newWidth, CV_16SC1,double(0));
     cv::Mat dpc_result_lr = cv::Mat(newHeight, newWidth, CV_16SC1,double(0));
     
    cv::Mat bf_result8 = cv::Mat(newHeight, newWidth, CV_8UC3);
    cv::Mat dpc_result_tb8 = cv::Mat(newHeight, newWidth, CV_8UC1);
    cv::Mat dpc_result_lr8 = cv::Mat(newHeight, newWidth, CV_8UC1);
    
    cv::Mat img;
     cv::Mat img16;
    cv::Mat shifted = cv::Mat(iStack[0].Image.rows, iStack[0].Image.cols, CV_16UC3,double(0));
    vector<Mat> channels(3);
    for (int idx = 0; idx < fileCount; idx++)
        {
         // Load image, convert to 16 bit grayscale image
         img = iStack[idx].Image;

         // Get home number
         int holeNum = iStack[idx].led_num;

         // Calculate shift based on array coordinates and desired z-distance
         int xShift = (int) round(z*iStack[idx].tan_x);
         int yShift = (int) round(z*iStack[idx].tan_y);

         // Shift the Image in x and y
            circularShift(img, shifted, yShift, xShift);
            
            // Add Brightfield image
            cv::add(bf_result, shifted, bf_result);
            
            // Convert shifted to b/w for DPC
            split(shifted, channels);
            channels[1].convertTo(channels[1],dpc_result_lr.type());
            
            if (find(leftList, leftList + 30, holeNum) != leftList + 30)
             cv::add(dpc_result_lr, channels[1], dpc_result_lr);
         else
             cv::subtract(dpc_result_lr, channels[1], dpc_result_lr);

         if (find(topList, topList + 30, holeNum) != topList + 30)
             cv::add(dpc_result_tb, channels[1], dpc_result_tb);
         else
             cv::subtract(dpc_result_tb, channels[1], dpc_result_tb);

         //float progress = 100*((idx+1) / (float)fileCount);
         //cout << progress << endl;
        }
        
        // Scale the values to 8-bit images
        double min_1, max_1, min_2, max_2, min_3, max_3;
        
        cv::minMaxLoc(bf_result, &min_1, &max_1);
         bf_result.convertTo(bf_result8, CV_8UC4, 255/(max_1 - min_1), - min_1 * 255.0/(max_1 - min_1));
       
        cv::minMaxLoc(dpc_result_lr.reshape(1), &min_2, &max_2);
        dpc_result_lr.convertTo(dpc_result_lr8, CV_8UC4, 255/(max_2 - min_2), -min_2 * 255.0/(max_2 - min_2));
        
        cv::minMaxLoc(dpc_result_tb.reshape(1), &min_3, &max_3);
        dpc_result_tb.convertTo(dpc_result_tb8, CV_8UC4, 255/(max_3 - min_3), -min_3 * 255.0/(max_3 - min_3));
        
        results[0] = bf_result8;
        results[1] = dpc_result_lr8;
        results[2] = dpc_result_tb8;

}

}
