package com.example.aidemo.ocr;

import android.content.res.AssetManager;
import android.graphics.Bitmap;

public class OcrEngine {
    static {
        System.loadLibrary("wfbmodel");
    }
    public static int padding = 50;
    public static float boxScoreThresh = 0.6f;
    public static float boxThresh  = 0.3f;
    public static float unClipRatio = 2.0f;
    public static boolean doAngle = true;
    public static boolean mostAngle = true;

    public static OcrResult detect(Bitmap input, Bitmap output, int maxSideLen){
        return detect(
                input, output, padding, maxSideLen,
                boxScoreThresh, boxThresh,
                unClipRatio, doAngle, mostAngle
               );
    }

//    public static native void init(String name, String path);
    public static native void init(AssetManager assetManager, int numThread);
    public static native OcrResult detect(Bitmap input, Bitmap output, int padding, int maxSidelen,
                                          float boxScoreThresh, float boxThresh,
                                          float unClipRatio, boolean doAngle, boolean mostAngle);
}
