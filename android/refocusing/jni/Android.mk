# See file:///home/zkphil/develop/NVPACK/android-ndk-r9d/docs/ANDROID-MK.html
LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include /home/zkphil/develop/NVPACK/OpenCV-2.4.8.2-Tegra-sdk/sdk/native/jni/OpenCV-tegra3.mk

LOCAL_MODULE    := nativeProcessing
LOCAL_SRC_FILES := native.cpp
LOCAL_LDLIBS +=  -llog -ldl

include $(BUILD_SHARED_LIBRARY)