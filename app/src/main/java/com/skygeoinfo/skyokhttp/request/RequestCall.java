package com.skygeoinfo.skyokhttp.request;


import com.skygeoinfo.skyokhttp.SkyHttp;
import com.skygeoinfo.skyokhttp.cell.AbsCallback;

import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 作    者：pansai
 * 创建日期：17/3/23 下午4:59
 * 发送请求的包装类
 */
public class RequestCall<T> {
    private volatile boolean canceled;
    private boolean executed;

    private BaseRequest baseRequest;
    private Call call;
    private AbsCallback<T> callback;


    private int currentRetryCount;

    public  RequestCall(BaseRequest baseRequest){
        this.baseRequest =baseRequest;
    }
    public void  execute(final AbsCallback<T> callback){
        synchronized (this){
            if (executed)throw  new IllegalStateException("Already executed");//已经执行
            executed = true;

            this.callback = callback;

            //请求执行前UI线程调用
            callback.onBefore();


            //构建请求
            RequestBody requestBody = baseRequest.buildRequestBody();

            final Request request = baseRequest.generateRequest(baseRequest.wrapRequestBody(requestBody));

            call = baseRequest.generateCall(request);


            //取消请求
            if (canceled) call.cancel();



            currentRetryCount = 0;

            call.enqueue(new Callback() {
                @Override
                public void onFailure(final Call call, final IOException e) {

                    //超时重连
                    if (e instanceof SocketTimeoutException &&currentRetryCount<baseRequest.getRetryCount()){
                        currentRetryCount++;
                        Call newCall = baseRequest.generateCall(call.request());
                        newCall.enqueue(this);
                    }else {
                        sendFailResultCallback(call,e,callback);

                    }

                }

                @Override
                public void onResponse(Call call, Response response) {


                    try {

                        if (call.isCanceled()){
                            sendFailResultCallback(call,new IOException("Canceled!"),callback);
                            return;
                        }
                        if (!response.isSuccessful()){
                            sendFailResultCallback(call, new IOException("request failed , reponse's code is : " + response.code()), callback);
                            return;
                        }

                        T t = callback.convertSuccess(response);

                        sendSuccessResultCallback(t,callback);
                    } catch (Exception e) {
                        sendFailResultCallback(call,e,callback);
                    }finally {
                        if (response.body()!=null)
                            response.body().close();
                    }

                }
            });
        }
    }

    public void sendFailResultCallback(final Call call, final Exception e, final AbsCallback callback)
    {
        if (callback == null) return;

        SkyHttp.getInstance().getDelivery().post(new Runnable()
        {
            @Override
            public void run()
            {
                callback.onError(call, e);
                callback.onAfter();
            }
        });
    }

    public void sendSuccessResultCallback(final T object, final AbsCallback callback)
    {
        if (callback == null) return;

        SkyHttp.getInstance().getDelivery().post(new Runnable() {
            @Override
            public void run() {
                callback.onResponse(object);
                callback.onAfter();
            }
        });
    }
}
