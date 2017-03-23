package com.skygeoinfo.skyokhttp.cell;

import android.app.Activity;
import android.app.ProgressDialog;
import android.view.Window;

/**
 * 作    者：pansai
 * 创建日期：17/3/23 下午4:55
 */
public abstract class DialogCallback extends StringCallback {
    private ProgressDialog dialog;


    private void initDialog(Activity activity) {
        dialog = new ProgressDialog(activity);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("请求网络中...");
    }

    public DialogCallback(Activity activity) {
        super();
        initDialog(activity);
    }

    @Override
    public void onBefore() {
        super.onBefore();
        //网络请求前
        if (dialog!=null&&!dialog.isShowing()){
            dialog.show();
        }
    }

    @Override
    public void onAfter() {
        super.onAfter();
        //网络请求结束
        if (dialog!=null&&dialog.isShowing()){
            dialog.dismiss();
        }
    }
}
