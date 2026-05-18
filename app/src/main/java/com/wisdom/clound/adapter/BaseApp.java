package com.wisdom.clound.adapter;

import android.app.Application;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class BaseApp  extends Application {
    private static OkHttpClient sOkHttpClient;

    @Override
    public void onCreate() {
        super.onCreate();
        // 全局初始化 OkHttpClient
        sOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();
    }
    public static OkHttpClient getOkHttpClient() {
        return sOkHttpClient;
    }
}
