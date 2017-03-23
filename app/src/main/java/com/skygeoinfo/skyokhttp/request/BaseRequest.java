package com.skygeoinfo.skyokhttp.request;


import com.skygeoinfo.skyokhttp.SkyHttp;
import com.skygeoinfo.skyokhttp.cell.AbsCallback;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 作    者：pansai
 * 创建日期：17/3/23 下午4:56
 *
 * 请求的基类
 */
public abstract class BaseRequest <T extends BaseRequest>{
    protected String url;
    protected String method;
    protected Object tag;
    protected long readTimeOut;
    protected long writeTimeOut;
    protected long connectTimeout;
    protected int retryCount;

    private AbsCallback mCallback;
    private Request request;

    public BaseRequest(String url) {
        this.url = url;
    }
    public  T url(String url){
        this.url =url;
        return (T) this;
    }
    @SuppressWarnings("unchecked")
    public T tag(Object tag) {
        this.tag = tag;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T readTimeOut(long readTimeOut) {
        this.readTimeOut = readTimeOut;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T writeTimeOut(long writeTimeOut) {
        this.writeTimeOut = writeTimeOut;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T connTimeOut(long connTimeOut) {
        this.connectTimeout = connTimeOut;
        return (T) this;
    }
    @SuppressWarnings("unchecked")
    public T setCallback(AbsCallback callback) {
        this.mCallback = callback;
        return (T) this;
    }

    public String getUrl() {
        return url;
    }

    public Object getTag() {
        return tag;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public Request getRequest() {
        return request;
    }

    public AbsCallback getCallback() {
        return mCallback;
    }

    /**
     * 返回当前的请求方法
     * GET,POST,HEAD,PUT,DELETE,OPTIONS
     */
    public String getMethod() {
        return method;
    }

    /**根据不同请求方式和参数，生成不同的请求体 RequestBody*/
    public  abstract RequestBody buildRequestBody();

    /** 对请求体body进行包装，用于进度回调*/
    public RequestBody wrapRequestBody(RequestBody requestBody){
        ProgressRequestBody progressRequestBody = new ProgressRequestBody(requestBody);
        progressRequestBody.setListener(new ProgressRequestBody.ProgressListener() {
            @Override
            public void onRequestProgress(final long bytesWritten, final long contentLength, final long networkSpeed) {
                SkyHttp.getInstance().getDelivery().post(new Runnable() {
                    @Override
                    public void run() {
                        if (mCallback!=null)mCallback.initProgress(bytesWritten,contentLength,bytesWritten*1.0f/contentLength,networkSpeed);
                    }
                });
            }
        });
        return progressRequestBody;
    }

    /** 根据不同的请求方式，将RequestBody转换成Request对象 */
    public abstract Request generateRequest(RequestBody requestBody);

    /** 根据当前的请求参数，生成对应的Call 任务*/
    public Call generateCall(Request request){
        this.request = request;
        if (readTimeOut>0||writeTimeOut>0||connectTimeout>0){
            OkHttpClient.Builder clientBuilder = SkyHttp.getInstance().getOkHttpClient().newBuilder();
            if (readTimeOut>0)clientBuilder.readTimeout(readTimeOut, TimeUnit.MILLISECONDS);
            if (writeTimeOut>0)clientBuilder.readTimeout(writeTimeOut, TimeUnit.MILLISECONDS);
            if (connectTimeout>0)clientBuilder.readTimeout(connectTimeout, TimeUnit.MILLISECONDS);
            return  clientBuilder.build().newCall(request);
        }else {
            return SkyHttp.getInstance().getOkHttpClient().newCall(request);
        }
    }
    /** 获取同步call对象*/
    public  Call getCall(){
        RequestBody requestBody = buildRequestBody();
        request  = generateRequest(wrapRequestBody(requestBody));

        return  generateCall(request);
    }

    /**
     * 同步请求
     * @param */
    public Response execute() throws IOException {
        return  getCall().execute();
    }
    /** 异步请求*/
    public <R> void execute(AbsCallback<R> callback){
        mCallback =callback;
        new RequestCall<R>(this).execute(callback);
    }
}
