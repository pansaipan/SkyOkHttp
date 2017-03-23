package com.skygeoinfo.skyokhttp.cell;

import okhttp3.Response;

/**
 * 作    者：pansai
 * 创建日期：17/3/23 下午4:51
 */
public abstract class StringCallback extends AbsCallback<String>{

    @Override
    public String convertSuccess(Response response) throws Exception {
        return response.body().string();
    }
}
