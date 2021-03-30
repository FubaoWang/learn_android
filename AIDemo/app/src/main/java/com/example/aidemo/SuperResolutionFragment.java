package com.example.aidemo;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.aidemo.TFLite.SplitBitmap;
import com.example.aidemo.TFLite.TFLiteSRUtil;
import com.example.aidemo.databinding.FragmentSuperResolutionBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SuperResolutionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SuperResolutionFragment extends Fragment {
    private static final String TAG = SuperResolutionFragment.class.getName();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SuperResolutionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SuperResolutionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SuperResolutionFragment newInstance(String param1, String param2) {
        SuperResolutionFragment fragment = new SuperResolutionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    private TFLiteSRUtil tfLiteSRUtil;
    private FragmentSuperResolutionBinding binding;
    private Handler handler;
    private HandlerThread handlerThread;
    private Bitmap srbitmap;

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
//        return inflater.inflate(R.layout.fragment_super_resolution, container, false);
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_super_resolution,container,false);
        binding.setLifecycleOwner(requireActivity());
        initlistenr();
        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data == null) {
            return;
        }
        try {
            FileInputStream fis = new FileInputStream(getPathFromURI(getContext(), data.getData()));
            Bitmap bitmap = BitmapFactory.decodeStream(fis);
            srinference(bitmap);
        }catch (Exception e){
            return;
        }

    }

    void init(){
        // 加载模型
        String classificationModelPath = getActivity().getCacheDir().getAbsolutePath() + File.separator + "srganx4.tflite";
        Utils.copyFileFromAsset(getActivity(), "srganx4.tflite", classificationModelPath);
        try {
            tfLiteSRUtil = new TFLiteSRUtil(classificationModelPath);
            Toast.makeText(getActivity(), "模型加载成功！", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getActivity(), "模型加载失败！", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            getActivity().finish();
        }
//        线程处理函数
        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());

        //progressbar回调函数
        tfLiteSRUtil.addSRProgressCallback(new TFLiteSRUtil.SRProgressCallback() {
            @Override
            public void callback(int progress) {
                binding.progressBar.setProgress(progress,true);
            }
        });

    }

    private synchronized void runInBackground(final Runnable r) {
        if (handler != null) {
            handler.post(r);
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

    void initlistenr(){
        binding.button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 打开相册
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });
        binding.button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetView();
                InputStream inputStream = null;
                try {
                    inputStream = getActivity().getAssets().open("Moth.png");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                srinference(bitmap);
            }
        });
        binding.button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean flag = saveBitmap(srbitmap);
                if(flag){
                    Toast.makeText(getActivity(), "图片保存成功！", Toast.LENGTH_SHORT).show();
                }else {
                    Toast.makeText(getActivity(), "图片保存失败！！！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    void resetView() {
        Bitmap mBitmap = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888);
        binding.imageView1.setImageBitmap(mBitmap);
        binding.imageView2.setImageBitmap(mBitmap);
        binding.progressBar.setProgress(0, true);
    }
    void srinference(Bitmap bitmap){
//        Bitmap srbitmap = tfLiteSRUtil.inference(bitmap);
//        binding.imageView1.setImageBitmap(bitmap);
//        binding.imageView2.setImageBitmap(srbitmap);

        runInBackground(new Runnable() {
            @Override
            public void run() {
                srbitmap = tfLiteSRUtil.inference(bitmap);
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(bitmap != null){
                            binding.imageView1.setImageBitmap(bitmap);
                            binding.imageView2.setImageBitmap(srbitmap);
                        }
                    }
                });
            }
        });
    }

    private boolean saveBitmap(Bitmap bitmap) {
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
        getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        return ret;
    }

}