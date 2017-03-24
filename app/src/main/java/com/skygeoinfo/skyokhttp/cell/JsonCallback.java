package com.skygeoinfo.skyokhttp.cell;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.Response;

/**
 * 作    者：pansai
 * 创建日期：17/3/24 下午4:26
 */
public abstract class JsonCallback<T> extends AbsCallback<T>{
    @Override
    public T convertSuccess(Response response) throws Exception {
        Type genType = getClass().getGenericSuperclass();

        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
        Type type = params[0];


        if (!(type instanceof ParameterizedType)) throw new IllegalStateException("没有填写泛型参数");


        JsonReader jsonReader = new JsonReader(response.body().charStream());

        Gson gson = new Gson();

        return gson.fromJson(jsonReader,type);
    }
}
