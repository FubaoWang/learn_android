package com.example.aidemo.ncnn;

import android.content.res.AssetManager;
import android.graphics.Bitmap;

public class FaceLandmark {
    static {
        System.loadLibrary("wfbmodel");
    }

    public static native void init(AssetManager manager, boolean useGPU);
    public static native FaceKeyPoint[] detect(Bitmap bitmap);
}
