package com.example.aidemo.ocr;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.aidemo.R;
import com.example.aidemo.Utils;

import org.jetbrains.annotations.NotNull;

public class TextDialog extends BaseDialog{

    private String content = "";
    private String title = "";
    private Button negativeBtn;
    private Button positiveBtn;
    private EditText contentEdit;

    public static final TextDialog getInstance() {
        TextDialog dialog = new TextDialog();
        dialog.setCanceledBack(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setGravity(Gravity.CENTER);
        dialog.setAnimStyle(R.style.diag_top_down_up_animation);
        return dialog;
    }

    @NotNull
    public final TextDialog setTitle(@NotNull String title) {
        this.title = title;
        return this;
    }

    @NotNull
    public final TextDialog setContent(@NotNull String textContent) {
        this.content = textContent;
        return this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_text_dialog, container, false);
        negativeBtn = view.findViewById(R.id.negativeBtn);
        positiveBtn = view.findViewById(R.id.positiveBtn);
        contentEdit = view.findViewById(R.id.contentEdit);
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
        contentEdit.setText(this.content);
    }

    View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.negativeBtn) {
                dismiss();
            } else if (v.getId() == R.id.positiveBtn) {
                Utils.ToClipboard(getContext(),content);
            }
        }
    };


}