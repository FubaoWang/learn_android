package com.example.aidemo.TFLite;

import android.graphics.Bitmap;
import com.example.aidemo.Utils;

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
        ArrayList<SplitBitmap> splitedBitmaps = Utils.splitBitmap(bitmap);
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
        Bitmap mergeBitmap = Utils.mergeBitmap(mergeBitmaps);
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


}
