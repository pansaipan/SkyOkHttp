package com.skygeoinfo.skyokhttp.cell;

import okhttp3.Call;
import okhttp3.Response;

/**
 * 作    者：pansai
 * 创建日期：17/3/23 下午4:47
 * 回调抽象类
 */
public abstract  class AbsCallback <T>{
    /** 请求网络开始前，UI线程 */
    public void onBefore() {

    }
    /**
     * 解析响应数据*/
    public abstract T convertSuccess(Response response) throws  Exception;
    /**请求失败 */
    public abstract void onError(Call call, Exception e);

    /**请求成功回调*/
    public  abstract void onResponse(T response);


    /** 请求网络结束后，UI线程 */
    public void onAfter() {

    }

    /**
     * 进度回调
     * @param currentSize 当前上传/下载的字节数
     * @param totalSize 总共需要上传/下载的字节数
     * @param progress  当前上传/下载的进度
     * @param networkSpeed 当前上传/下载的速度 字节/秒
     */
    public void initProgress(long currentSize, long totalSize, float progress, long networkSpeed) {

    }
}
