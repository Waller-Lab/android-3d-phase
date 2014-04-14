#!/bin/sh
# copy dataset onto sdcard of android device
adb push $1 /sdcard/datasets/$1
