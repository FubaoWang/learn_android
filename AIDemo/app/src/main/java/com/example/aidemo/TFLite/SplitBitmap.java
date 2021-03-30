package com.example.aidemo.TFLite;


import android.graphics.Bitmap;

import java.util.ArrayList;

/***
 * 这个类用来存放切分后的小块图片的信息
 */
public class SplitBitmap {
    public int row; // 当前小块图片相对原图处于哪一行
    public int column; // 当前小块图片相对原图处于哪一列
    public Bitmap bitmap; // 当前小块图片的位图
}

