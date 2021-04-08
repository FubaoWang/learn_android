package com.example.aidemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.HandlerThread;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.aidemo.TFLite.TFLiteStyleTransferUtil;
import com.example.aidemo.databinding.FragmentStyleTransferBinding;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.File;
import java.nio.ByteBuffer;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link StyleTransferFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class StyleTransferFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public StyleTransferFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment StyleTransferFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static StyleTransferFragment newInstance(String param1, String param2) {
        StyleTransferFragment fragment = new StyleTransferFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    private TFLiteStyleTransferUtil tflitextract, tflitemerge;
    private FragmentStyleTransferBinding binding;
    private Handler handler;
    private HandlerThread handlerThread;
    private @NonNull ByteBuffer latent;
    private TensorBuffer feature;
    private String string;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        init();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_style_transfer, container, false);
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_style_transfer,container,false);
        binding.setLifecycleOwner(requireActivity());
        initlistenr();
        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(data == null){
            return;
        }
        Uri image_uri = data.getData();
        if (resultCode == Activity.RESULT_OK) {
            ShowImage(requestCode,image_uri);
            // 1返回风格特征，
            if(requestCode==1){
                binding.progressBar.setVisibility(View.VISIBLE);
                string = "提取图片style中。。。";
                binding.textView.setText(string);

                Bitmap stylebitmap = ((BitmapDrawable) binding.imageView1.getDrawable()).getBitmap();
                inference(tflitextract, stylebitmap,1);
            }
//            else{
//                Bitmap contentbitmap = ((BitmapDrawable) binding.imageView2.getDrawable()).getBitmap();
//                inference(tflitextract, contentbitmap,2);
//            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    void init(){
        // 加载style模型
        String styleModelPath = getActivity().getCacheDir().getAbsolutePath() + File.separator + "extractmodel_f16.tflite";
        Utils.copyFileFromAsset(getActivity(), "extractmodel_f16.tflite", styleModelPath);
        try {
            tflitextract = new TFLiteStyleTransferUtil(styleModelPath);
            Toast.makeText(getActivity(), "extract模型加载成功！", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getActivity(), "extract模型加载失败！", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            getActivity().finish();
        }
        // 加载content模型
        String contentModelPath = getActivity().getCacheDir().getAbsolutePath() + File.separator + "mergemodel_f16.tflite";
        Utils.copyFileFromAsset(getActivity(), "mergemodel_f16.tflite", contentModelPath);
        try {
            tflitemerge = new TFLiteStyleTransferUtil(contentModelPath);
            Toast.makeText(getActivity(), "merge模型加载成功！", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getActivity(), "merge模型加载失败！", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            getActivity().finish();
        }
        //        线程处理函数
        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }

    void initlistenr(){
         binding.progressBar.setVisibility(View.INVISIBLE);
         binding.button1.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                  OpenImage(1);
             }
         });
         binding.button2.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                  OpenImage(2);
             }
         });
         binding.button3.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 binding.progressBar.setVisibility(View.VISIBLE);
                 string = "合成图片中。。。";
                 binding.textView.setText(string);
                 Bitmap mBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
                 binding.imageView3.setImageBitmap(mBitmap);

                 Bitmap contentbitmap = ((BitmapDrawable) binding.imageView2.getDrawable()).getBitmap();
                 inference(tflitemerge,contentbitmap,latent);
             }
         });
         binding.button4.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 Bitmap stbitmap = ((BitmapDrawable) binding.imageView3.getDrawable()).getBitmap();
                 boolean flag = Utils.saveBitmap(getContext(),stbitmap);
                 if(flag){
                     Toast.makeText(getActivity(), "图片保存成功！", Toast.LENGTH_SHORT).show();
                 }else {
                     Toast.makeText(getActivity(), "图片保存失败！！！", Toast.LENGTH_SHORT).show();
                 }
             }
         });
    }
    // 判断不同按钮打开相册
    public void OpenImage(int a){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, a);
    }
    // 展示图片按照不同按钮
    public void ShowImage(int a, Uri uri){
        if(a==1){
            binding.imageView1.setImageURI(uri);
        }else{
            binding.imageView2.setImageURI(uri);
        }
    }

    private synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
        }
    }
    void inference(TFLiteStyleTransferUtil tflite, Bitmap bitmap,int flag) {
        runInBackground(new Runnable() {
            @Override
            public void run() {
                latent = tflite.inference(bitmap);
                Toast.makeText(getActivity(), "style提取成功！", Toast.LENGTH_SHORT).show();

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        binding.progressBar.setVisibility(View.INVISIBLE);
                        string = "提取图片style成功！！！";
                        binding.textView.setText(string);
                    }
                });
            }
        });
    }

    void inference(TFLiteStyleTransferUtil tflite, Bitmap content, @NonNull ByteBuffer style) {
        runInBackground(new Runnable() {
            @Override
            public void run() {
                Bitmap res = tflite.inference(content, style);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(res != null){
                            binding.progressBar.setVisibility(View.INVISIBLE);
                            string = "合成图片成功！！！";
                            binding.textView.setText(string);
                            binding.imageView3.setImageBitmap(res);
                        }
                    }
                });
            }
        });
    }

}