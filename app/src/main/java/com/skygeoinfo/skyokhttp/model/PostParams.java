package com.skygeoinfo.skyokhttp.model;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 作    者：pansai
 * 创建日期：17/3/23 下午4:47
 * 请求参数包装类
 */
public class PostParams {

    /**
     * 普通健值对参数
     **/
    public LinkedHashMap<String, List<String>> params;
    /**
     * 文件健值对参数
     **/
    public LinkedHashMap<String, List<File>> fileParams;


    public void PostParams() {
        params = new LinkedHashMap<>();
        fileParams = new LinkedHashMap<>();
    }

    public void put(String key, String value) {
        puts(key, value);
    }

    public void put(Map<String, String> params) {
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, String> entry : params.entrySet()) {
                puts(entry.getKey(), entry.getValue());
            }
        }

    }

    public void put(String key, List<String> values) {
        if (values != null) {
            for (String value : values) {
                puts(key, value);
            }
        }
    }

    private void puts(String key, String value) {
        if (key != null && value != null) {
            List<String> valuesList = params.get(key);
            if (valuesList == null) {
                valuesList = new ArrayList<>();
                params.put(key, valuesList);
            }
            valuesList.add(value);
        }
    }

    public void putFile(String key, File file) {
        putFiles(key, file);
    }

    public void putFile(String key, List<File> files) {
        if (files != null) {
            for (File file : files) {
                putFiles(key, file);
            }
        }
    }

    public void putFile(List<String> keys, List<File> files) {
        if (keys!=null&&files != null&&keys.size()==files.size()) {
            for (int i=0;i<keys.size();i++) {
                String key = keys.get(i);
                File file  = files.get(i);
                putFiles(key,file);
            }
        }
    }


    private void putFiles(String key, File file) {
        if (key != null && file != null) {
            List<File> files = fileParams.get(key);
            if (files == null) {
                files = new ArrayList<>();
                fileParams.put(key, files);
            }
            files.add(file);
        }
    }
}
