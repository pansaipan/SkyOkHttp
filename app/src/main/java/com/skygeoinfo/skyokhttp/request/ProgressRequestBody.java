package com.skygeoinfo.skyokhttp.request;


import com.skygeoinfo.skyokhttp.SkyHttp;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * 作    者：pansai
 * 创建日期：17/3/23 下午4:55
 *
 * 包装请求体 回调进度  用于上传大文件
 */
public class ProgressRequestBody extends RequestBody {

    protected  RequestBody requestBody;//实际待包装请求体
    protected  ProgressListener listener;//进度回调接口
    //包装完成的BufferedSink
    private BufferedSink bufferedSink;
    public ProgressRequestBody(RequestBody requestBody){
        this.requestBody =requestBody;
    }
    public ProgressRequestBody(RequestBody requestBody,ProgressListener listener){
        this.requestBody =requestBody;
        this.listener = listener;
    }

    public void setListener(ProgressListener listener) {
        this.listener = listener;
    }
    /**
     * 重写调用实际的响应体的contentType
     * @return MediaType
     */
    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    /**
     * 重写调用实际的响应体的contentLength
     * @return contentLength
     * @throws IOException 异常
     */
    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }
    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        bufferedSink = Okio.buffer(sink(sink));//包装
        requestBody.writeTo(bufferedSink);//写入
        bufferedSink.flush();  //必须调用flush，否则最后一部分数据可能不会被写入

    }

    /**
     * 写入，回调进度接口
     * @param sink
     * @return Sink
     */

    private Sink sink(Sink sink){

        return new ForwardingSink(sink) {
            //当前写入字节数
            private long bytesWritten = 0L;
            //总字节长度，避免多次调用contentLength()方法
            private long contentLength = 0L;
            private long lastRefreshUiTime;  //最后一次刷新的时间
            private long lastWriteBytes;     //最后一次写入字节数据
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (contentLength<=0)contentLength =contentLength();//获得contentLength的值，后续不再调用
                bytesWritten +=byteCount;

                long curTime = System.currentTimeMillis();
                //每100毫秒刷新一次数据
                if (curTime -lastRefreshUiTime>= SkyHttp.REFRESH_TIME||bytesWritten==contentLength){
                    //计算下载速度
                    long diffTime = (curTime-lastRefreshUiTime)/1000;
                    if (diffTime==0)diffTime+=1;
                    long diffBytes = bytesWritten - lastWriteBytes;
                    long networkSpeed = diffBytes/diffTime;
                    if (listener!=null)listener.onRequestProgress(bytesWritten,contentLength,networkSpeed);

                    lastRefreshUiTime =System.currentTimeMillis();
                    lastWriteBytes = bytesWritten;
                }
            }
        };
    }
    /**
     * 进度回调接口*/
    public interface ProgressListener {
        void onRequestProgress(long bytesWritten, long contentLength, long networkSpeed);
    }
}
