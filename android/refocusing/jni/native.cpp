#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/contrib/contrib.hpp>
#include <android/log.h>
#include <vector>

using namespace std;
using namespace cv;

extern "C" {
int* meshgrid_x(int width);
int* meshgrid_y(int height);
JNIEXPORT jdoubleArray JNICALL Java_com_choochootrain_refocusing_tasks_ComputePhaseHeightMap_computePhaseImage(JNIEnv* env, jobject, jlong addrInput);

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

}
