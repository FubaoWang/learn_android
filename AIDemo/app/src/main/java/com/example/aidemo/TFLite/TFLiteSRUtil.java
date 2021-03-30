package com.example.aidemo.TFLite;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.gpu.GpuDelegate;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.util.ArrayList;


public class TFLiteSRUtil {
    private static final String TAG = TFLiteSRUtil.class.getName();
    private Interpreter tflite;
    private TensorImage inputImageBuffer;
    private  TensorBuffer outputImageBuffer;
    private static final int NUM_THREADS = 4;
    private static final float[] IMAGE_MEAN = new float[]{127.5f, 127.5f, 127.5f};
    private static final float[] IMAGE_STD = new float[]{127.5f, 127.5f, 127.5f};
    private ImageProcessor imageProcessor;
    private int scale = 4;
    private SRProgressCallback callback;
    /**
     * @param modelPath model path
     */
    public TFLiteSRUtil(String modelPath) throws Exception {

//        initPaint();
        File file = new File(modelPath);
        if (!file.exists()) {
            throw new Exception("model file is not exists!");
        }

        try {
            Interpreter.Options options = new Interpreter.Options();
            // 使用多线程预测
            options.setNumThreads(NUM_THREADS);
            // 使用Android自带的API或者GPU加速
//            NnApiDelegate delegate = new NnApiDelegate();
//            GpuDelegate delegate = new GpuDelegate();
//            options.addDelegate(delegate);
            options.setUseXNNPACK(true);
            tflite = new Interpreter(file, options);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("load model fail!");
        }
    }

    private void setInputOutputDetails(Bitmap bitmap) {
        // 获取模型输入数据格式
        DataType imageDataType = tflite.getInputTensor(0).dataType();
//        Log.e(TAG, "imageDataType:" + imageDataType.toString());

        // 创建TensorImage，用于存放图像数据
        inputImageBuffer = new TensorImage(imageDataType);
        inputImageBuffer.load(bitmap);

        // 因为模型的输入shape是任意宽高的图片，即{-1,-1,-1,3}，但是在tflite java版中，我们需要指定输入数据的具体大小。
        // 所以在这里，我们要根据输入图片的宽高来设置模型的输入的shape
        int[] inputShape = {1, bitmap.getHeight(), bitmap.getWidth(), 3};
        tflite.resizeInput(tflite.getInputTensor(0).index(), inputShape);
//        Log.e(TAG, "inputShape:" + bitmap.getByteCount());
//        for (int i : inputShape) {
//            Log.e(TAG, i + "");
//        }

        // 获取模型输出数据格式
        DataType probabilityDataType = tflite.getOutputTensor(0).dataType();
//        Log.e(TAG, "probabilityDataType:" + probabilityDataType.toString());

        // 同样的，要设置模型的输出shape，因为我们用的模型的功能是在原图的基础上，放大scale倍，所以这里要乘以scale
        int[] probabilityShape = {1, bitmap.getWidth() * scale, bitmap.getHeight() * scale, 3};//tfLite.getOutputTensor(0).shapeSignature();

        // Creates the output tensor and its processor.
        outputImageBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType);
    }

    /***
     * 推理函数，将图片数据输送给模型并且得到输出结果，然后将输出结果转为Bitmap格式
     * @param bitmap
     * @return
     */
    public Bitmap inference(Bitmap bitmap) {
        float progress = 0;
        ArrayList<SplitBitmap> splitedBitmaps = splitBitmap(bitmap);
        ArrayList<SplitBitmap> mergeBitmaps = new ArrayList<SplitBitmap>();
        float total = splitedBitmaps.size() + 10; // 因为后面还有合并操作，所以分母设置的稍微大一点点
        int curIndex = 0;
        for (SplitBitmap sb : splitedBitmaps) {
            callback.callback(Math.round(progress));
             // 根据原图的小块图片设置模型输入
            setInputOutputDetails(sb.bitmap);
            // 执行模型的推理，得到小块图片sr后的高清图片
            tflite.run(inputImageBuffer.getBuffer(), outputImageBuffer.getBuffer());
            // 将高清图片数据从TensorBuffer转成float[]，以转成安卓常用的Bitmap类型
            float[] results = outputImageBuffer.getFloatArray();
            // 将图片从float[]转成Bitmap
            Bitmap res = floatArrayToBitmap(results, sb.bitmap.getWidth() * scale, sb.bitmap.getHeight() * scale);
            SplitBitmap srsb = new SplitBitmap();
            srsb.column = sb.column;
            srsb.row = sb.row;
            srsb.bitmap = res;
            mergeBitmaps.add(srsb);
            progress = (curIndex++ / total) * 100;

       }
        Bitmap mergeBitmap = mergeBitmap(mergeBitmaps);
        callback.callback(100);
        return mergeBitmap;
    }


    /***
     * 模型的输出结果是float型的数据，需要转成int型
     * @param data
     * @return
     */
    private int floatToInt(float data) {
        int tmp = Math.round(data);
        if (tmp < 0){
            tmp = 0;
        }else if (tmp > 255) {
            tmp = 255;
        }
//        Log.e(TAG, tmp + " " + data);
        return tmp;
    }

    /***
     * 模型的输出得到的是一个float数据，这个数组就是sr后的高清图片信息，我们要将它转成Bitmap格式才好在安卓上使用
     * @param data 图片数据
     * @param width 图片宽度
     * @param height 图片高度
     * @return 返回图片的位图
     */
    private Bitmap floatArrayToBitmap(float[] data, int width, int height) {
        int[] intdata = new int[width * height];
        // 因为我们用的Bitmap是ARGB的格式，而data是RGB的格式，所以要经过转换，A指的是透明度
        for (int i = 0; i < width * height; i++) {
            int R = floatToInt(data[3 * i]);
            int G = floatToInt(data[3 * i + 1]);
            int B = floatToInt(data[3 * i + 2]);

            intdata[i] = (0xff << 24) | (R << 16) | (G << 8) | (B << 0);

//            Log.e(TAG, intdata[i]+"");
        }
        //得到位图
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(intdata, 0, width, 0, 0, width, height);
        return bitmap;
    }


//    进度条
    public void addSRProgressCallback(final SRProgressCallback callback) {
        this.callback = callback;
    }

    public interface SRProgressCallback {
        public void callback(int progress);
    }


    /***
     * 将原图切分成众多小块图片，根据原图的宽高和cropBitmapSize来决定分成多少小块
     * @param bitmap 待拆分的位图
     * @return 返回切割后的小块位图列表
     */
    public ArrayList<SplitBitmap> splitBitmap(Bitmap bitmap) {
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
    public Bitmap mergeBitmap(ArrayList<SplitBitmap> splitedBitmaps) {
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
