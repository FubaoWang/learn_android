package com.example.aidemo;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;

import com.example.aidemo.TFLite.SplitBitmap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Utils {
    private static final String TAG = Utils.class.getName();


    // 获取最优的预览图片大小
    public static Size chooseOptimalSize(final Size[] choices, final int width, final int height) {
        final Size desiredSize = new Size(width, height);

        // Collect the supported resolutions that are at least as big as the preview Surface
        boolean exactSizeFound = false;
        float desiredAspectRatio = width * 1.0f / height; //in landscape perspective
        float bestAspectRatio = 0;
        final List<Size> bigEnough = new ArrayList<Size>();
        for (final Size option : choices) {
            if (option.equals(desiredSize)) {
                // Set the size but don't return yet so that remaining sizes will still be logged.
                exactSizeFound = true;
                break;
            }

            float aspectRatio = option.getWidth() * 1.0f / option.getHeight();
            if (aspectRatio > desiredAspectRatio) continue; //smaller than screen
            //try to find the best aspect ratio which fits in screen
            if (aspectRatio > bestAspectRatio) {
                if (option.getHeight() >= height && option.getWidth() >= width) {
                    bigEnough.clear();
                    bigEnough.add(option);
                    bestAspectRatio = aspectRatio;
                }
            } else if (aspectRatio == bestAspectRatio) {
                if (option.getHeight() >= height && option.getWidth() >= width) {
                    bigEnough.add(option);
                }
            }
        }
        if (exactSizeFound) {
            return desiredSize;
        }

        if (bigEnough.size() > 0) {
            final Size chosenSize = Collections.min(bigEnough, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(
                            (long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
                }
            });
            return chosenSize;
        } else {
            return choices[0];
        }
    }

    /**
     * copy model file to local
     *
     * @param context     activity context
     * @param assets_path model in assets path
     * @param new_path    copy to new path
     */
    public static void copyFileFromAsset(Context context, String assets_path, String new_path) {
        File father_path = new File(new File(new_path).getParent());
         if (!father_path.exists()) {
            father_path.mkdirs();
        }
        try {
            File new_file = new File(new_path);
            InputStream is_temp = context.getAssets().open(assets_path);
            if (new_file.exists() && new_file.isFile()) {
                if (contrastFileMD5(new_file, is_temp)) {
                    Log.d(TAG, new_path + " is exists!");
                    return;
                } else {
                    Log.d(TAG, "delete old model file!");
                    new_file.delete();
                }
            }
            InputStream is = context.getAssets().open(assets_path);
            FileOutputStream fos = new FileOutputStream(new_file);
            byte[] buffer = new byte[1024];
            int byteCount;
            while ((byteCount = is.read(buffer)) != -1) {
                fos.write(buffer, 0, byteCount);
            }
            fos.flush();
            is.close();
            fos.close();
            Log.d(TAG, "the model file is copied");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //get bin file's md5 string
    private static boolean contrastFileMD5(File new_file, InputStream assets_file) {
        MessageDigest new_file_digest, assets_file_digest;
        int len;
        try {
            byte[] buffer = new byte[1024];
            new_file_digest = MessageDigest.getInstance("MD5");
            FileInputStream in = new FileInputStream(new_file);
            while ((len = in.read(buffer, 0, 1024)) != -1) {
                new_file_digest.update(buffer, 0, len);
            }

            assets_file_digest = MessageDigest.getInstance("MD5");
            while ((len = assets_file.read(buffer, 0, 1024)) != -1) {
                assets_file_digest.update(buffer, 0, len);
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        String new_file_md5 = new BigInteger(1, new_file_digest.digest()).toString(16);
        String assets_file_md5 = new BigInteger(1, assets_file_digest.digest()).toString(16);
        Log.d("new_file_md5", new_file_md5);
        Log.d("assets_file_md5", assets_file_md5);
        return new_file_md5.equals(assets_file_md5);
    }

    public static ArrayList<String> ReadListFromFile(AssetManager assetManager, String filePath) {
        ArrayList<String> list = new ArrayList<String>();
        BufferedReader reader = null;
        InputStream istr = null;
        try {
            reader = new BufferedReader(
                    new InputStreamReader(assetManager.open(filePath)));
            String line;
            while ((line = reader.readLine()) != null) {
                list.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    // 根据相册的Uri获取图片的路径
    public static String getPathFromURI(Context context, Uri uri) {
        String result;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            result = uri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            result = cursor.getString(idx);
            cursor.close();
        }
        return result;
    }

    public static Bitmap URItoBitmap(Context context, Uri uri){
        Bitmap bitmap=null;
        try {
            FileInputStream fis = new FileInputStream(Utils.getPathFromURI(context, uri));
            bitmap = BitmapFactory.decodeStream(fis);
        }catch (Exception e){
            Log.e(TAG, "URItoBitmap: 失败！");
        }
        return bitmap;
    }
    // 保存图片到相册
    public static boolean saveBitmap(Context context, Bitmap bitmap) {
        boolean ret = false;
        if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            return ret;
        }
        final File rootDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsoluteFile();
        if (!rootDir.exists()){
            if (!rootDir.mkdirs()) {
                Log.e(TAG, "saveBitmap: Make dir failed");
            }
        }
        String filename = SystemClock.uptimeMillis() + ".png";
        File file = new File(rootDir, filename);
        try {
            final FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            ret = true;
        } catch (final Exception e) {
            Log.e(TAG, "saveBitmap: Exception!");
        }
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        return ret;
    }

    /***
     * 将原图切分成众多小块图片，根据原图的宽高和cropBitmapSize来决定分成多少小块
     * @param bitmap 待拆分的位图
     * @return 返回切割后的小块位图列表
     */
    public static ArrayList<SplitBitmap> splitBitmap(Bitmap bitmap) {
        int cropBitmapSize = 80;
        // 获取原图的宽高
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        // 原图宽高除以cropBitmapSize，得到应该将图片的宽和高分成几部分
        float splitFW = (float)width / cropBitmapSize;
        float splitFH = (float)height / cropBitmapSize;
        int splitW = (int)(splitFW);
        int splitH = (int)(splitFH);
        // 用来存放切割以后的小块图片的信息
        ArrayList<SplitBitmap> splitedBitmaps = new ArrayList<SplitBitmap>();

        //对图片进行切割
        if (splitFW < 1.2 && splitFH < 1.2) {
            // 直接计算整张图
            SplitBitmap sb = new SplitBitmap();
            sb.row = 0;
            sb.column = 0;
            sb.bitmap = bitmap;
            splitedBitmaps.add(sb);
        } else if (splitFW < 1.2 && splitFH > 1) {
            // 仅在高度上拆分
            for (int i = 0; i < splitH; i++) {
                SplitBitmap sb = new SplitBitmap();
                sb.row = i;
                sb.column = 0;
                if (i == splitH - 1) {
                    sb.bitmap = Bitmap.createBitmap(bitmap, 0, i * cropBitmapSize, cropBitmapSize, height - i * cropBitmapSize, null, false);
                }else {
                    sb.bitmap = Bitmap.createBitmap(bitmap, 0, i * cropBitmapSize, cropBitmapSize, cropBitmapSize, null, false);
                }
                splitedBitmaps.add(sb);
            }
        } else if (splitFW > 1 && splitFH < 1.2) {
            // 仅在宽度上拆分
            for (int i = 0; i < splitW; i++) {
                SplitBitmap sb = new SplitBitmap();
                sb.row = 0;
                sb.column = i;
                if (i == splitW - 1) {
                    sb.bitmap = Bitmap.createBitmap(bitmap, i * cropBitmapSize, 0, cropBitmapSize, width - i * cropBitmapSize, null, false);
                }else {
                    sb.bitmap = Bitmap.createBitmap(bitmap, i * cropBitmapSize, 0, cropBitmapSize, cropBitmapSize, null, false);
                }

                splitedBitmaps.add(sb);
            }
        } else {
            // 在高度和宽度上都拆分
            for (int i = 0; i < splitH; i++) {
                for (int j = 0; j < splitW; j++) {
                    int lastH = cropBitmapSize;
                    int lastW = cropBitmapSize;
                    // 最后一行的高度
                    if (i == splitH - 1) {
                        lastH = height - i * cropBitmapSize;
//                        Log.e(TAG, "lastH:" +lastH);
                    }
                    // 最后一列的宽度
                    if (j == splitW - 1) {
                        lastW = width - j * cropBitmapSize;
//                        Log.e(TAG, "lastW:" +lastW);
                    }
//                    Log.e(TAG, "lastH:" + lastH + " lastW:" + lastW +
//                            " bitmapH:" + bitmap.getHeight() + " bitmapW:" + bitmap.getWidth() +
//                            " i * cropBitmapSize:" + i * cropBitmapSize + " j * cropBitmapSize:" + j * cropBitmapSize +
//                            " i:" + i + " j:" + j
//                    );

                    SplitBitmap sb = new SplitBitmap();
                    // 记录当前小块图片所处的行列
                    sb.row = i;
                    sb.column = j;
                    // 获取当前小块的位图
                    sb.bitmap = Bitmap.createBitmap(bitmap, j * cropBitmapSize, i * cropBitmapSize, lastW, lastH, null, false);
                    splitedBitmaps.add(sb);
                }
            }
        }
        return splitedBitmaps;
    }


//    private  final Paint boxPaint = new Paint();
    /***
     * 初始化画笔，用来调试切分图片和合并图片的
     */
//    private  void initPaint() {
//        boxPaint.setColor(Color.RED);
//        boxPaint.setStyle(Paint.Style.STROKE);
//        boxPaint.setStrokeWidth(2.0f);
//        boxPaint.setStrokeCap(Paint.Cap.ROUND);
//        boxPaint.setStrokeJoin(Paint.Join.ROUND);
//        boxPaint.setStrokeMiter(100);
//    }

    /***
     * 合并小块位图列表为一个大的位图
     * @param splitedBitmaps 待合并的小块位图列表
     * @return 返回合并后的大的位图
     */
    public static Bitmap mergeBitmap(ArrayList<SplitBitmap> splitedBitmaps) {
        int mergeBitmapWidth = 0;
        int mergeBitmapHeight = 0;
        // 遍历位图列表，根据行和列的信息，计算出合并后的位图的宽高
        for (SplitBitmap sb : splitedBitmaps) {
//            Log.e(TAG, "sb.column:" + sb.column + " sb.row:" + sb.row + " sb.bitmap.getHeight():" + sb.bitmap.getHeight() + " sb.bitmap.getWidth():" + sb.bitmap.getWidth());
            if (sb.row == 0) {
                mergeBitmapWidth += sb.bitmap.getWidth();
            }
            if (sb.column == 0) {
                mergeBitmapHeight += sb.bitmap.getHeight();
            }
        }

        Log.e(TAG, "splitedBitmaps: " + splitedBitmaps.size() + " mergeBitmapWidth:" + mergeBitmapWidth + " mergeBitmapHeight:" + mergeBitmapHeight);
        // 根据宽高创建合并后的空位图
        Bitmap mBitmap = Bitmap.createBitmap(mergeBitmapWidth, mergeBitmapHeight, Bitmap.Config.ARGB_8888);

        // 创建画布，我们将在画布上拼接新的大位图
        Canvas canvas = new Canvas(mBitmap);

        // 计算位图列表的长度
        int splitedBitmapsSize = splitedBitmaps.size();

        //lastRowSB记录上一行的第一列的数据，主要用来判断当前行是否最后一行，因为最后一行之前的所有行的高度都是一致的
        SplitBitmap lastColumn0SB = null;

        for (int i = 0; i < splitedBitmapsSize; i++) {
            // 获取当前小块信息
            SplitBitmap sb = splitedBitmaps.get(i);
            // 根据当前小块所处的行列和宽高计算小块应处于大位图中的位置
            int left = sb.column * sb.bitmap.getWidth();
            int top = sb.row * sb.bitmap.getHeight();
            int right = left + sb.bitmap.getWidth();
            int bottom = top + sb.bitmap.getHeight();

            // 最后一列
            // 根据计算下一个小块位图的列数是否为0判断当前小块是否是最后一列
            if (i != 0 && i < splitedBitmapsSize - 1 && splitedBitmaps.get(i + 1).column == 0) {
                // 因为最后一列的宽度不确定，所以，要根据上一小块的宽高来计算当前小块在大位图中的起始位置
                SplitBitmap lastBitmap = splitedBitmaps.get(i - 1);
                left = sb.column * lastBitmap.bitmap.getWidth();
                top = sb.row * lastBitmap.bitmap.getHeight();
                right = left + sb.bitmap.getWidth();
                bottom = top + sb.bitmap.getHeight();
            }

            //最后一行
            // 根据对比上一行中的高度来计算当前行是否最后一行，因为最后一行前的所有行的高度都是一致的
            if (i != 0 && i < splitedBitmapsSize && lastColumn0SB != null && splitedBitmaps.get(i).bitmap.getHeight() != lastColumn0SB.bitmap.getHeight()) {
//                Log.e(TAG, "---------------");
                // 如果最后一行的高度和之前行的高度不一致，那么就要根据上一行中的高度来重新计算当前行的起始位置
                SplitBitmap lastColumnBitmap = lastColumn0SB;
                left = sb.column * lastColumnBitmap.bitmap.getWidth();
                top = sb.row * lastColumnBitmap.bitmap.getHeight();
                right = left + sb.bitmap.getWidth();
                bottom = top + sb.bitmap.getHeight();
            } else if (sb.column == 0) {
                // 记录上一行的第一个列的小块信息
                lastColumn0SB = sb;
            }

            // 这个是当前小块的信息
            Rect srcRect = new Rect(0, 0, sb.bitmap.getWidth(), sb.bitmap.getHeight());
            // 这个是当前小块应该在大图中的位置信息
            Rect destRect = new Rect(left, top, right, bottom);
            // 将当前小块画到大图中
            canvas.drawBitmap(sb.bitmap, srcRect, destRect, null);
            // 这个是为了调试而画的框
//            canvas.drawRect(destRect, boxPaint);
//            Log.e(TAG,"I:" + i + " col:" + sb.column + " row:" + sb.row + " width:" + sb.bitmap.getWidth() + " height:" + sb.bitmap.getHeight());
        }

        return mBitmap;
    }
}
