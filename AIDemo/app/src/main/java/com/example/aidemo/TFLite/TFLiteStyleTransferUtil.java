package com.example.aidemo.TFLite;

import android.graphics.Bitmap;

import com.example.aidemo.Utils;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class TFLiteStyleTransferUtil {
    private static final String TAG = TFLiteStyleTransferUtil.class.getName();
    private Interpreter tflite;
    private TensorImage inputImageBuffer;
    private TensorBuffer outputImageBuffer, featureBuffer, stylebuffer;
    private static final int NUM_THREADS = 4;
    private STProgressCallback callback;

    /**
     * @param modelPath model path
     */
    public TFLiteStyleTransferUtil(String modelPath) throws Exception {

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
    // 设置风格提取网络
    private void setInputOutputDetails(Bitmap bitmap) {
            //抽取style风格的特征
            // 获取模型输入数据格式
            DataType imageDataType = tflite.getInputTensor(0).dataType();

            // 创建TensorImage，用于存放图像数据
            inputImageBuffer = new TensorImage(imageDataType);
            inputImageBuffer.load(bitmap);

            // 因为模型的输入shape是任意宽高的图片，即{-1,-1,-1,3}，但是在tflite java版中，我们需要指定输入数据的具体大小。
            // 所以在这里，我们要根据输入图片的宽高来设置模型的输入的shape
            int[] inputShape = {1, bitmap.getHeight(), bitmap.getWidth(), 3};
            tflite.resizeInput(tflite.getInputTensor(0).index(), inputShape);
            // 获取模型输出数据格式
            DataType outputDataType = tflite.getOutputTensor(0).dataType();
            // 同样的，要设置模型的输出shape,缩小8倍向上取整
            int[] output1Shape = {2, 1, 1, 512};
            // Creates the output tensor and its processor.
            stylebuffer = TensorBuffer.createFixedSize(output1Shape, outputDataType);

    }
    // 设置内容网络
//    private void setInputOutputDetails(TensorBuffer content, TensorBuffer style) {
//        //抽取style风格的特征
//        // 获取模型输入数据格式
//        DataType DataType = tflite.getInputTensor(0).dataType();
//        int[] contentShape = content.getShape();
//        featureBuffer = TensorBuffer.createFixedSize(contentShape, DataType);
//        featureBuffer.loadBuffer(content.getBuffer());
//        tflite.resizeInput(tflite.getInputTensor(0).index(), contentShape);
//
//        int[] styleShape = style.getShape();
//        stylebuffer = TensorBuffer.createFixedSize(styleShape, DataType);
//        stylebuffer.loadBuffer(style.getBuffer());
//
//        // 获取模型输出数据格式
//        DataType probabilityDataType = tflite.getOutputTensor(0).dataType();
//        // 同样的，要设置模型的输出shape
//        int[] probabilityShape = {1, contentShape[1]*8, contentShape[2]*8, 3};
//        // Creates the output tensor and its processor.
//        outputImageBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType);
//
//    }
    // 设置内容网络
    private void setInputOutputDetails(Bitmap bitmap, @NonNull ByteBuffer style) {
        // 获取模型输入数据格式
        DataType imageDataType = tflite.getInputTensor(0).dataType();

        // 创建TensorImage，用于存放图像数据
        inputImageBuffer = new TensorImage(imageDataType);
        inputImageBuffer.load(bitmap);

        // 因为模型的输入shape是任意宽高的图片，即{-1,-1,-1,3}，但是在tflite java版中，我们需要指定输入数据的具体大小。
        // 所以在这里，我们要根据输入图片的宽高来设置模型的输入的shape
        int[] inputShape = {1, bitmap.getHeight(), bitmap.getWidth(), 3};
        tflite.resizeInput(tflite.getInputTensor(0).index(), inputShape);

        int[] styleShape = tflite.getInputTensor(1).shape();
        stylebuffer = TensorBuffer.createFixedSize(styleShape, imageDataType);
        stylebuffer.loadBuffer(style);

        // 获取模型输出数据格式
        DataType probabilityDataType = tflite.getOutputTensor(0).dataType();
        // 同样的，要设置模型的输出shape
        int[] probabilityShape = {1, bitmap.getHeight(), bitmap.getWidth(), 3};
        // Creates the output tensor and its processor.
        outputImageBuffer = TensorBuffer.createFixedSize(probabilityShape, probabilityDataType);

    }

    /***
     * 推理函数:提取网络
     * @return
     * flag=2: 返回归一化后内容feature map
     * flag=1: 返回风格图片的均值与方差
     */
    public @NonNull ByteBuffer inference(Bitmap bitmap) {

    // 根据原图的小块图片设置模型输入
    setInputOutputDetails(bitmap);
    tflite.run(inputImageBuffer.getBuffer(),stylebuffer.getBuffer());
    ByteBuffer res = stylebuffer.getBuffer();
    return  res;
    }

    /***
     * 推理函数:合成网络
     */
//    public Bitmap inference(TensorBuffer content, TensorBuffer style) {
//        setInputOutputDetails(content, style);
//        // 执行模型的推理，得到小块图片t
//        Object[] inputs = new Object[]{featureBuffer.getBuffer(),stylebuffer.getBuffer()};
//        Map<Integer, Object> outputs = new HashMap();
//        outputs.put(0, outputImageBuffer.getBuffer());
//
//        tflite.runForMultipleInputsOutputs(inputs, outputs);
//
//        // 将高清图片数据从TensorBuffer转成float[]，以转成安卓常用的Bitmap类型
//        float[] results = outputImageBuffer.getFloatArray();
//        // 将图片从float[]转成Bitmap
//        int[] shape = outputImageBuffer.getShape();
//        Bitmap res = floatArrayToBitmap(results, shape[1], shape[2]);
//        return res;
//    }

    public Bitmap inference(Bitmap bitmap, @NonNull ByteBuffer style) {
        //是图片能够被8整除，方便计算
        bitmap = CutBitmap(bitmap);

        setInputOutputDetails(bitmap, style);
        // 执行模型的推理，得到小块图片t
        Object[] inputs = new Object[]{inputImageBuffer.getBuffer(),stylebuffer.getBuffer()};
        Map<Integer, Object> outputs = new HashMap();
        outputs.put(0, outputImageBuffer.getBuffer());

        tflite.runForMultipleInputsOutputs(inputs, outputs);

        // 将高清图片数据从TensorBuffer转成float[]，以转成安卓常用的Bitmap类型
        float[] results = outputImageBuffer.getFloatArray();
        // 将图片从float[]转成Bitmap
        int[] shape = outputImageBuffer.getShape();
        Bitmap res = floatArrayToBitmap(results, shape[1], shape[2]);
        return res;
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
    public void addSTProgressCallback(final TFLiteStyleTransferUtil.STProgressCallback callback) {
        this.callback = callback;
    }

    public interface STProgressCallback {
        public void callback(int progress);
    }

    public Bitmap CutBitmap(Bitmap bitmap){
        int dst = 512;
        Bitmap bmp = Bitmap.createScaledBitmap(bitmap, dst, dst, false);
        return bmp;
    }


}
