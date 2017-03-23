package com.skygeoinfo.skyokhttp.cell;

import android.os.Environment;
import android.text.TextUtils;

import com.skygeoinfo.skyokhttp.SkyHttp;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Response;

/**
 * 作    者：pansai
 * 创建日期：17/3/23 下午4:53
 * 文件下载
 */
public abstract class FileCallback extends AbsCallback<File> {

    private String destFileDir;     //目标文件存储的文件夹路径
    private String destFileName;    //目标文件存储的文件名
    private  AbsCallback callback = this;
    public  FileCallback(String destFileDir,String destFileName){
        this.destFileDir =destFileDir;
        this.destFileName =destFileName;
    }
    @Override
    public File convertSuccess(Response response) throws Exception {
        if (TextUtils.isEmpty(destFileDir)) destFileDir = Environment.getExternalStorageDirectory() + "/download";
        if (TextUtils.isEmpty(destFileName)) destFileName = getFileName(response.request().url().toString());

        File dir = new File(destFileDir);
        if (!dir.exists()) dir.mkdirs();
        File file = new File(dir, destFileName);
        if (file.exists()) file.delete();

        long lastRefreshUiTime = 0;  //最后一次刷新的时间
        long lastWriteBytes = 0;     //最后一次写入字节数据

        InputStream is = null;
        byte[] buf = new byte[2048];
        FileOutputStream fos = null;
        try {
            is = response.body().byteStream();
            final long total = response.body().contentLength();
            long sum = 0;
            int len;
            fos = new FileOutputStream(file);
            while ((len = is.read(buf)) != -1) {
                sum += len;
                fos.write(buf, 0, len);

                //下载进度回调
                final long finalSum = sum;
                long curTime = System.currentTimeMillis();
                //每200毫秒刷新一次数据
                if (curTime - lastRefreshUiTime >= 100 || finalSum == total) {
                    //计算下载速度
                    long diffTime = (curTime - lastRefreshUiTime) / 1000;
                    if (diffTime == 0) diffTime += 1;
                    long diffBytes = finalSum - lastWriteBytes;
                    final long networkSpeed = diffBytes / diffTime;
                    SkyHttp.getInstance().getDelivery().post(new Runnable() {
                        @Override
                        public void run() {
                            callback.initProgress(finalSum, total, finalSum * 1.0f / total, networkSpeed);   //进度回调的方法
                        }
                    });

                    lastRefreshUiTime = System.currentTimeMillis();
                    lastWriteBytes = finalSum;
                }
            }
            fos.flush();
            return file;
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fos != null) fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /** 根据url获取文件名 */
    private String getFileName(String url) {
        int separatorIndex = url.lastIndexOf("/");
        return (separatorIndex < 0) ? url : url.substring(separatorIndex + 1,
                url.length());
    }
}
