package com.example.aidemo.mnn;

import android.graphics.Bitmap;

public class NanoDet {

    static {
        System.loadLibrary("wfbmodel");
    }

    public static native void init(String name, String path, boolean useGPU);
    public static native BoxInfo[] detect(Bitmap bitmap, byte[] imageBytes, int width, int height, double threshold, double nms_threshold);
}
