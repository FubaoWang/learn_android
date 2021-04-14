package com.example.aidemo;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import com.example.aidemo.databinding.ActivitySurfaceBinding;

public class SurfaceActivity extends AppCompatActivity {
    public static int FACE_LANDMARK = 1;
    public static int SIMPLE_POSE = 2;


    public static int USE_MODEL = FACE_LANDMARK;
    public static boolean USE_GPU = false;

    private ActionBar actionbar;
    private ActivitySurfaceBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_surface);
        setActionbar();
        hideControls();
    }
    // 隐藏一些不用的设置
    private void hideControls(){
        binding.nmsSeekBar.setEnabled(false);
        binding.thresholdSeekBar.setEnabled(false);
        binding.txtNMS.setVisibility(View.GONE);
        binding.txtThreshold.setVisibility(View.GONE);
        binding.nmsSeekBar.setVisibility(View.GONE);
        binding.thresholdSeekBar.setVisibility(View.GONE);
        binding.valTxtView.setVisibility(View.GONE);
        binding.sbVideoSpeed.setVisibility(View.GONE);
        binding.sbVideo.setVisibility(View.GONE);
    }

    // 设置顶部工具栏
    private void setActionbar(){
        actionbar = getSupportActionBar();
        if(USE_MODEL==FACE_LANDMARK){
            actionbar.setTitle(R.string.button5);
        }else{
            actionbar.setTitle(R.string.button6);
        }
        actionbar.setDisplayHomeAsUpEnabled(true);
    }
    // 拦截确定按钮
    private void DialogBuilder(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.dialog_tile);
        builder.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SurfaceActivity.this.finish();
            }
        });
        builder.setNegativeButton(R.string.dialog_cancle, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:// 点击返回图标事件
                DialogBuilder();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        DialogBuilder();
    }
}