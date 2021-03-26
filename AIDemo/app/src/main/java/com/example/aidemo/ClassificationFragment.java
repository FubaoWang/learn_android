package com.example.aidemo;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.aidemo.TFLite.TFLiteClassificationUtil;
import com.example.aidemo.databinding.FragmentClassificationBinding;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ClassificationFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ClassificationFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ClassificationFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ClassificationFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ClassificationFragment newInstance(String param1, String param2) {
        ClassificationFragment fragment = new ClassificationFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    TFLiteClassificationUtil tfLiteClassificationUtil;
    ArrayList<String> classNames;
    FragmentClassificationBinding binding;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        init();
        Log.i("CLS", "onCreate: init success");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_classification, container, false);

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_classification, container, false);
        binding.setLifecycleOwner(requireActivity());
        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 打开相册
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });
        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String image_path;
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                if (data == null) {
                    Log.w("Classification onActivityResult", "user photo data is null");
                    return;
                }
                Uri image_uri = data.getData();
                image_path = getPathFromURI(getContext(), image_uri);
                try {
                    // 预测图像
                    FileInputStream fis = new FileInputStream(image_path);
                    Bitmap bitmap = BitmapFactory.decodeStream(fis);
                    binding.imageview.setImageBitmap(bitmap);
                    long start = System.currentTimeMillis();
                    float[] result = tfLiteClassificationUtil.predictImage(bitmap);
                    long end = System.currentTimeMillis();
                    String show_text = "预测结果标签：" + (int) result[0] +
                            "\n名称：" +  classNames.get((int) result[0]) +
                            "\n概率：" + result[1] +
                            "\n时间：" + (end - start) + "ms";
                    binding.textView.setText(show_text);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    void init(){
        // 加载模型和标签
        classNames = Utils.ReadListFromFile(getActivity().getAssets(), "label_list.txt");
        String classificationModelPath = getActivity().getCacheDir().getAbsolutePath() + File.separator + "mobilenetv2_float16.tflite";
        Utils.copyFileFromAsset(getActivity(), "mobilenetv2_float16.tflite", classificationModelPath);
        try {
            tfLiteClassificationUtil = new TFLiteClassificationUtil(classificationModelPath);
            Toast.makeText(getActivity(), "模型加载成功！", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getActivity(), "模型加载失败！", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            getActivity().finish();
        }
    }
    // 根据相册的Uri获取图片的路径
    String getPathFromURI(Context context, Uri uri) {
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


}