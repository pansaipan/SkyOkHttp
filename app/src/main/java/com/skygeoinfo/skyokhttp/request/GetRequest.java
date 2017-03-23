package com.skygeoinfo.skyokhttp.request;

import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * 作    者：pansai
 * 创建日期：17/3/23 下午4:58
 *
 * Get请求实现类
 */
public class GetRequest extends BaseRequest<GetRequest> {

    public  GetRequest(String url){
        super(url);
        method = "GET";
    }
    @Override
    public RequestBody buildRequestBody() {
        return null;
    }

    @Override
    public Request generateRequest(RequestBody requestBody) {
        Request.Builder builder = new Request.Builder();
        return builder.get().url(url).tag(tag).build();
    }
}
