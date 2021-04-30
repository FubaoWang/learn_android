package com.example.aidemo.ocr;

import android.app.Dialog;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;


public abstract class BaseDialog extends DialogFragment {

    private static final String TAG = BaseDialog.class.getName();
    private int mGravity = Gravity.CENTER; //对话框的位置
    private boolean mCanceledOnTouchOutside = true;//是否触摸外部关闭
    private boolean mCanceledBack = true; //是否返回键关闭
    private int mAnimStyle = 0; //显示动画

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        //修复DialogFragment内存泄漏
        if(this.getShowsDialog()){
            this.setShowsDialog(false);
        }
        super.onActivityCreated(savedInstanceState);
        this.setShowsDialog(true);
        View tmpView = this.getView();
        Dialog dialog = this.getDialog();
        if (tmpView != null) {
            boolean var3 = tmpView.getParent() == null;
            if (!var3) {
                Log.e(TAG, "DialogFragment can not be attached to a container view");
            }
            dialog.setContentView(tmpView);
        }
        dialog.setOwnerActivity(getActivity());

        if (savedInstanceState != null) {
            Bundle dialogState = savedInstanceState.getBundle("android:savedDialogState");
            if (dialogState != null) {
                dialog.onRestoreInstanceState(dialogState);
            }
        }
    }

    public final void setGravity(int gravity) {
        this.mGravity = gravity;
    }

    public final void setCanceledBack(boolean canceledBack) {
        this.mCanceledBack = canceledBack;
    }

    public final void setCanceledOnTouchOutside(boolean canceledOnTouchOutsidek) {
        this.mCanceledOnTouchOutside = canceledOnTouchOutsidek;
    }

    public final void setAnimStyle(int animStyle) {
        this.mAnimStyle = animStyle;
    }

    @Override
    public void onStart() {
        Dialog baseDialog = this.getDialog();
        if (baseDialog != null) {
            baseDialog.setCanceledOnTouchOutside(this.mCanceledOnTouchOutside);
            baseDialog.setCancelable(this.mCanceledBack);
            this.setDialogGravity(baseDialog);
        }
        // 全屏显示Dialog，重新测绘宽高
        DisplayMetrics dm = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        getDialog().getWindow().setAttributes((WindowManager.LayoutParams) params);
        getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                                          (int)((double)dm.heightPixels * 0.9D));
        super.onStart();
    }

    private final void setDialogGravity(Dialog dialog) {
        Window dialogWindow = dialog.getWindow();
        if (dialogWindow != null) {
            WindowManager.LayoutParams wlp = dialogWindow.getAttributes();
            wlp.gravity = this.mGravity;
            wlp.width = -1;
            wlp.height = -2;
            dialogWindow.setAttributes(wlp);
            if (this.mAnimStyle != 0) {
                dialogWindow.setWindowAnimations(this.mAnimStyle);
            }

        }
    }
}
