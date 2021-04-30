package com.example.aidemo.ocr;

import android.graphics.Bitmap;

import java.util.ArrayList;

public class OcrResult {

    public double getDbNetTime() {
        return dbNetTime;
    }

    public void setDbNetTime(Double dbNetTime) {
        this.dbNetTime = dbNetTime;
    }

    public ArrayList<TextBlock> getTextBlocks() {
        return textBlocks;
    }

    public void setTextBlocks(ArrayList<TextBlock> textBlocks) {
        this.textBlocks = textBlocks;
    }

    public Bitmap getBoxImg() {
        return boxImg;
    }

    public void setBoxImg(Bitmap boxImg) {
        this.boxImg = boxImg;
    }

    public double getDetectTime() {
        return detectTime;
    }

    public void setDetectTime(Double detectTime) {
        this.detectTime = detectTime;
    }

    public String getStrRes() {
        return strRes;
    }

    public void setStrRes(String strRes) {
        this.strRes = strRes;
    }

    public OcrResult(double dbNetTime, ArrayList<TextBlock> textBlocks, Bitmap boxImg, double detectTime, String strRes) {
        this.dbNetTime = dbNetTime;
        this.textBlocks = textBlocks;
        this.boxImg = boxImg;
        this.detectTime = detectTime;
        this.strRes = strRes;
    }

    private double dbNetTime;
    private ArrayList<TextBlock> textBlocks;
    private Bitmap boxImg;
    private double detectTime;
    private String strRes;
}
