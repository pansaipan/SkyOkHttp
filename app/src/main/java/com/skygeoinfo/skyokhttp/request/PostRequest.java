package com.skygeoinfo.skyokhttp.request;


import com.skygeoinfo.skyokhttp.model.PostParams;

import java.io.File;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 作    者：pansai
 * 创建日期：17/3/23 下午4:58
 * post请求
 */
public class PostRequest extends BaseRequest<PostRequest> {
    public static final MediaType MEDIA_TYPE_PLAIN = MediaType.parse("text/plain;charset=utf-8");
    public static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json;charset=utf-8");
    public static final MediaType MEDIA_TYPE_STREAM = MediaType.parse("application/octet-stream");

    protected MediaType mediaType;      //上传的MIME类型
    protected String content;           //上传的文本内容
    protected byte[] bs;                //上传的字节数据
    protected RequestBody requestBody;

    private PostParams postParams;

    public PostRequest(String url) {
        super(url);
        method = "POST";
        postParams = new PostParams();
    }

    public PostRequest requestBody(RequestBody requestBody) {
        this.requestBody = requestBody;
        return this;
    }

    /**
     * 调用这个方法会清空其他参数
     */
    public PostRequest upString(String content) {
        this.content = content;
        mediaType = MEDIA_TYPE_PLAIN;
        return this;
    }

    /**
     * 调用这个方法会清空其他参数
     */
    public PostRequest upJson(String json) {
        this.content = json;
        mediaType = MEDIA_TYPE_JSON;
        return this;
    }

    /**
     * 调用这个方法会清空其他参数
     */
    public PostRequest upBytes(byte[] bs) {
        this.bs = bs;
        mediaType = MEDIA_TYPE_STREAM;
        return this;
    }

    public PostRequest params(String key, String value) {
        postParams.put(key, value);
        return this;
    }


    public PostRequest params(Map<String, String> params) {
        postParams.put(params);

        return this;
    }

    public PostRequest params(String key, List<String> values) {
        postParams.put(key, values);
        return this;
    }

    public PostRequest paramsFile(String key, File file) {
        postParams.putFile(key, file);
        return this;
    }


    public PostRequest paramsFile(List<String> keys, List<File> files) {
        postParams.putFile(keys, files);

        return this;
    }

    public PostRequest paramsFile(String key, List<File> files) {
        postParams.putFile(key, files);
        return this;
    }

    @Override
    public RequestBody buildRequestBody() {

        if (requestBody != null)
            return requestBody;                               //自定义的请求体
        if (content != null && mediaType != null)
            return RequestBody.create(mediaType, content);    //post上传字符串数据
        if (bs != null && mediaType != null)
            return RequestBody.create(mediaType, bs);         //post上传字节数组

        return generateMultipartRequestBody();
    }

    @Override
    public Request generateRequest(RequestBody requestBody) {

        Request.Builder builder = new Request.Builder();
        return builder.post(requestBody).url(url).tag(tag).build();
    }

    /**
     * 生成类似表单的请求体
     */
    private RequestBody generateMultipartRequestBody() {
        // 判断是否有文件
        if (postParams.fileParams.isEmpty()) {
            //表单提交，没有文件
            FormBody.Builder builder = new FormBody.Builder();
            for (String key : postParams.params.keySet()) {
                List<String> values = postParams.params.get(key);
                for (String value : values) {
                    builder.add(key, value);
                }
            }
            return builder.build();
        } else {
            //表单提交有文件
            MultipartBody.Builder builder = new MultipartBody.Builder();

            //拼接键值对
            if (!postParams.params.isEmpty()) {
                for (String key : postParams.params.keySet()) {
                    List<String> values = postParams.params.get(key);
                    for (String value : values) {
                        builder.addFormDataPart(key, value);
                    }
                }
            }

            //拼接文件
            RequestBody fileBody = null;
            for (String key : postParams.fileParams.keySet()) {
                List<File> files = postParams.fileParams.get(key);
                for (File file : files) {
                    String fileName = file.getName();
                    fileBody = RequestBody.create(MediaType.parse(guessMimeType(fileName)), file);
                    builder.addFormDataPart(key, fileName, fileBody);
                }
            }
            return builder.build();
        }
    }

    private String guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(path);
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }

        return contentTypeFor;
    }
}
