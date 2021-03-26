package com.example.aidemo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.aidemo.mnn.BoxInfo;
import com.example.aidemo.mnn.NanoDet;
import com.example.aidemo.databinding.FragmentObjectDetectionBinding;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ObjectDetectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ObjectDetectionFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private Bitmap mutableBitmap;
    private int width;
    private int height;
    private FragmentObjectDetectionBinding binding;
    private double threshold = 0.4, nms_threshold = 0.6;

    public ObjectDetectionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ObjectDetectionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ObjectDetectionFragment newInstance(String param1, String param2) {
        ObjectDetectionFragment fragment = new ObjectDetectionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        init();
        Log.i("OBD", "onCreate: init success");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_object_detection, container, false);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_object_detection, container, false);
        binding.setLifecycleOwner(requireActivity());

        binding.photobutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 打开相册
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        binding.NMSseekBar.setProgress((int) (nms_threshold * 100));
        binding.THDseekBar.setProgress((int) (nms_threshold * 100));
        final String format = "THR: %.2f, NMS: %.2f";
        binding.textView3.setText(String.format(Locale.ENGLISH, format, threshold, nms_threshold));
        binding.NMSseekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                nms_threshold = progress / 100.f;
                binding.textView3.setText(String.format(Locale.ENGLISH, format, threshold, nms_threshold));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        binding.THDseekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                threshold = progress / 100.f;
                binding.textView3.setText(String.format(Locale.ENGLISH, format, threshold, nms_threshold));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                if (data == null) {
                    Log.w("ObjectDetection onActivityResult", "user photo data is null");
                    return;
                }
                try {
                    runByPhoto(data);
                }catch(Exception e){
                    Toast.makeText(getActivity(), "Photo is null", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    void init(){
//        模型初始化
        String ObjectDetectionModelPath = getActivity().getCacheDir().getAbsolutePath() + File.separator + "nanodet_320.mnn";
        Utils.copyFileFromAsset(getActivity(), "nanodet_320.mnn", ObjectDetectionModelPath);
        try {
            NanoDet.init("nanodet_320.mnn", ObjectDetectionModelPath, false);
            Toast.makeText(getActivity(), "模型加载成功！", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getActivity(), "模型加载失败！", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            getActivity().finish();
        }

    }

    void runByPhoto(@Nullable Intent data) throws FileNotFoundException {
        final Bitmap image = getPicture(getContext(),data);
        if (image == null) {
            Toast.makeText(getActivity(), "Photo is null", Toast.LENGTH_SHORT).show();
            return;
        }
        long start = System.currentTimeMillis();
        mutableBitmap = image.copy(Bitmap.Config.ARGB_8888, true);
        width = image.getWidth();
        height = image.getHeight();
        mutableBitmap = detectAndDraw(mutableBitmap);
        long end = System.currentTimeMillis();

        String show_text = "尺寸：" +  height + "x" + width +
                           "时间：" + (end - start) + "ms";
        binding.textView4.setText(show_text);
        binding.imageView2.setImageBitmap(mutableBitmap);

    }

    // 根据相册的Uri获取图片的路径
    Bitmap getPicture(@Nullable Context context, @Nullable Intent data) throws FileNotFoundException {
        Uri uri = data.getData();
        String image_path;
        Bitmap bitmap = null;
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) {
            image_path = uri.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            image_path = cursor.getString(idx);
            cursor.close();
        }
        try{
            FileInputStream fis = new FileInputStream(image_path);
            bitmap = BitmapFactory.decodeStream(fis);
        }catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    Bitmap detectAndDraw(Bitmap bitmap) {
        byte[] imageDataBytes = bitampToByteArray(bitmap);

        BoxInfo[] result = null;
        result = NanoDet.detect(bitmap, imageDataBytes, bitmap.getWidth(), bitmap.getHeight(), threshold, nms_threshold);

        if (result == null) {
            mutableBitmap = bitmap;
            return bitmap;
        }
        mutableBitmap = drawBoxRects(bitmap, result);
        return mutableBitmap;
    }

    Bitmap drawBoxRects(Bitmap mutableBitmap, BoxInfo[] results) {
        if (results == null || results.length <= 0) {
            return mutableBitmap;
        }
        Canvas canvas = new Canvas(mutableBitmap);
        final Paint boxPaint = new Paint();
        boxPaint.setAlpha(200);
        boxPaint.setStyle(Paint.Style.STROKE);
        boxPaint.setStrokeWidth(4 * mutableBitmap.getWidth() / 800.0f);
        boxPaint.setTextSize(30 * mutableBitmap.getWidth() / 800.0f);
        for (BoxInfo box : results) {
            boxPaint.setColor(box.getColor());
            boxPaint.setStyle(Paint.Style.FILL);
            canvas.drawText(box.getLabel() + String.format(Locale.CHINESE, " %.3f", box.getScore()), box.x0 + 3, box.y0 + 30 * mutableBitmap.getWidth() / 1000.0f, boxPaint);
            boxPaint.setStyle(Paint.Style.STROKE);
            canvas.drawRect(box.getRect(), boxPaint);
        }
        return mutableBitmap;
    }

    byte[] bitampToByteArray(Bitmap bitmap) {
        int bytes = bitmap.getByteCount();
        ByteBuffer buf = ByteBuffer.allocate(bytes);
        bitmap.copyPixelsToBuffer(buf);
        return buf.array();
    }
}