package com.example.aidemo.ocr;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.aidemo.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class DebugDialog extends BaseDialog {

    private String title = "";
    private Button negativeBtn;
    private Button positiveBtn;
    private RecyclerView recyclerView;
    private DebugItemView debugItemView = new DebugItemView();

    private ArrayList<TextBlock> textBlocks = new ArrayList<TextBlock>();

    public static final DebugDialog getInstance() {
        DebugDialog dialog = new DebugDialog();
        dialog.setCanceledBack(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setGravity(Gravity.CENTER);
        dialog.setAnimStyle(R.style.diag_top_down_up_animation);
        return dialog;
    }

    @NotNull
    public final DebugDialog setTitle(@NotNull String title) {
        this.title = title;
        return this;
    }

    @NotNull
    public final DebugDialog setResult(@NotNull OcrResult ocrResult) {
        this.textBlocks.clear();
        this.textBlocks.addAll(ocrResult.getTextBlocks());
        return this;
    }



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_debug_dialog, container, false);
        negativeBtn = view.findViewById(R.id.negativeBtn);
        positiveBtn = view.findViewById(R.id.positiveBtn);
        recyclerView = view.findViewById(R.id.debugRV);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        initViews();
        super.onActivityCreated(savedInstanceState);
    }

    private final void initViews(){
        negativeBtn.setOnClickListener(listener);
        positiveBtn.setOnClickListener(listener);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(debugItemView);
        debugItemView.setTextBlocks(this.textBlocks);
        debugItemView.notifyDataSetChanged();
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dismiss();
        }
    };


}