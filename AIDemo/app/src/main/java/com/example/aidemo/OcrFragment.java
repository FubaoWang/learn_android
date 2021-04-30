package com.example.aidemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.example.aidemo.databinding.FragmentOcrBinding;
import com.example.aidemo.ocr.DebugDialog;
import com.example.aidemo.ocr.OcrEngine;
import com.example.aidemo.ocr.OcrResult;
import com.example.aidemo.ocr.TextDialog;

import java.io.FileInputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Math.max;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OcrFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OcrFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private FragmentOcrBinding binding;
    private AtomicBoolean detectPhoto = new AtomicBoolean(true);
    private int numThreed = 4;
    private long startTime = 0;
    private long endTime = 0;
    private OcrResult ocrResult;
    private Bitmap selectedImg;
    ExecutorService detectService = Executors.newSingleThreadExecutor();

    public OcrFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OcrFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static OcrFragment newInstance(String param1, String param2) {
        OcrFragment fragment = new OcrFragment();
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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_ocr, container, false);
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_ocr, container, false);
        binding.setLifecycleOwner(requireActivity());
        init();
        Toast.makeText(getActivity(), "模型加载成功！", Toast.LENGTH_SHORT).show();
        initlistener();
        return binding.getRoot();
    }

    private void init(){
        binding.progressBar.setVisibility(View.GONE);
        OcrEngine.init(getActivity().getAssets(), numThreed);
    };

    private void initlistener(){
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()){
                    case R.id.button8:
                        detectPhoto.set(true);
                        Intent intent = new Intent(Intent.ACTION_PICK);
                        intent.setType("image/*");
                        startActivityForResult(intent, 1);
                        break;
                    case R.id.button9:
                        detectPhoto.set(false);
                        break;
                    case R.id.button10:
                        binding.ocrimageView.setImageDrawable(null);
                        binding.textView7.setText("Please wait ...");
                        break;
                    case R.id.button11:
                        if(selectedImg == null){
                            Toast.makeText(getActivity(), "请选择一张图片", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        int maxSideLen = max(selectedImg.getWidth(),selectedImg.getHeight());
                        v.post(new Runnable() {
                            @Override
                            public void run() {
                                binding.ocrimageView.setImageDrawable(null);
                            }
                        });
                        detect(selectedImg,maxSideLen);
                        break;
                    case R.id.button12:
                        if(ocrResult == null) {return;}
                        TextDialog myDialog = TextDialog.getInstance();
                        myDialog.setContent(ocrResult.getStrRes());
                        myDialog.show(getChildFragmentManager(),"myDialog");
                        break;
                    case R.id.button13:
                        if(ocrResult == null && ocrResult.getTextBlocks()==null) {return;}
                        DebugDialog debugDialog = DebugDialog.getInstance();
                        debugDialog.setResult(ocrResult);
                        debugDialog.show(getChildFragmentManager(),"myDialog");
                        break;
                }
            }
        };

        binding.button8.setOnClickListener(listener);
        binding.button9.setOnClickListener(listener);
        binding.button10.setOnClickListener(listener);
        binding.button11.setOnClickListener(listener);
        binding.button12.setOnClickListener(listener);
        binding.button13.setOnClickListener(listener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 1) {
                if (data == null) {
                    Log.w("OCR onActivityResult", "user photo data is null");
                    return;
                }
                try {
                    FileInputStream fis = new FileInputStream(Utils.getPathFromURI(getContext(), data.getData()));
                    selectedImg = BitmapFactory.decodeStream(fis);
                    binding.ocrimageView.setImageBitmap(selectedImg);
                }catch (Exception e){
                    return;
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void detect(Bitmap img, int reSize){

        if(detectService == null){
            return;
        }
        detectService.execute(new Runnable() {
            @Override
            public void run() {
                getView().post(new Runnable() {
                    @Override
                    public void run() {
                        binding.progressBar.setVisibility(View.VISIBLE);
                    }
                });
                Bitmap boxImg = Bitmap.createBitmap(img.getWidth(), img.getHeight(),
                        Bitmap.Config.ARGB_8888);
                startTime = System.currentTimeMillis();
                ocrResult  = OcrEngine.detect(img, boxImg, reSize);
                endTime  = System.currentTimeMillis();
                getView().post(new Runnable() {
                    @Override
                    public void run() {
                        binding.progressBar.setVisibility(View.GONE);
                        showResultOnUI();
                    }
                });
            }
        });
//        Bitmap boxImg = Bitmap.createBitmap(img.getWidth(), img.getHeight(),
//                        Bitmap.Config.ARGB_8888);
//        startTime = System.currentTimeMillis();
//        ocrResult  = OcrEngine.detect(img, boxImg, reSize);
//        endTime  = System.currentTimeMillis();
//        showResultOnUI();
    }

    private void showResultOnUI(){
        long time = endTime - startTime;
        int numblocks = ocrResult.getTextBlocks().size();
        binding.textView7.setText(String.format("%s\n框检测耗时: %.3f ms\n框分割耗时: %.3f ms\n总耗时: %.3f ms\n检测框总数: %d",
                "OCR:", (float) ocrResult.getDbNetTime(), (float) ocrResult.getDetectTime(),(float) time, numblocks));
        binding.ocrimageView.setImageBitmap(ocrResult.getBoxImg());
    }
}