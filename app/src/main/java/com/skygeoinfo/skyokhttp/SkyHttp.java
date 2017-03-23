package com.skygeoinfo.skyokhttp;

import android.os.Handler;
import android.os.Looper;

import com.skygeoinfo.skyokhttp.request.GetRequest;
import com.skygeoinfo.skyokhttp.request.PostRequest;

import okhttp3.Call;
import okhttp3.OkHttpClient;

/**
 * 作    者：pansai
 * 创建日期：17/3/23 下午4:45
 */
public class SkyHttp {

    public static final int DEFAULT_MILLISECONDS = 60000;       //默认的超时时间
    public static int REFRESH_TIME = 100;                       //回调刷新时间（单位ms）
    private  static  SkyHttp mInstance;
    private OkHttpClient mOkHttpClient;
    private Handler mDelivery;//返回主线程

    private SkyHttp() {
        mOkHttpClient = new OkHttpClient();
        mDelivery = new Handler(Looper.getMainLooper());
    }


    public static SkyHttp getInstance() {
        if (mInstance == null) {
            synchronized (SkyHttp.class) {
                if (mInstance == null) {
                    mInstance = new SkyHttp();
                }
            }
        }
        return mInstance;
    }

    /**
     *
     * @return
     */
    public OkHttpClient getOkHttpClient(){

        return  mOkHttpClient;
    }
    public Handler getDelivery(){
        return mDelivery;
    }


    /**
     * get 请求*/
    public  static GetRequest get(String url){
        return  new GetRequest(url);
    }
    /** post请求 */
    public static PostRequest post(String url){
        return  new PostRequest(url);
    }

    public void cancelTag(Object tag)
    {
        for (Call call : mOkHttpClient.dispatcher().queuedCalls())
        {
            if (tag.equals(call.request().tag()))
            {
                call.cancel();
            }
        }
        for (Call call : mOkHttpClient.dispatcher().runningCalls())
        {
            if (tag.equals(call.request().tag()))
            {
                call.cancel();
            }
        }
    }
}
