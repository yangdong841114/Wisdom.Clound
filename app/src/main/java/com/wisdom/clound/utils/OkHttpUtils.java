package com.wisdom.clound.utils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

public class OkHttpUtils {
    // 单例实例（volatile 保证多线程可见性）
    private static volatile OkHttpClient INSTANCE;
    // 私有构造，禁止外部new
    private OkHttpUtils() {}
    public static OkHttpClient getInstance() {
        if (INSTANCE == null) {
            synchronized (OkHttpUtils.class) {
                if (INSTANCE == null) {
                    // 初始化：设置超时时间，可扩展拦截器、证书等
                    INSTANCE = new OkHttpClient.Builder()
                            .connectTimeout(15, TimeUnit.SECONDS)  // 连接超时
                            .readTimeout(15, TimeUnit.SECONDS)     // 读取超时
                            .writeTimeout(15, TimeUnit.SECONDS)    // 写入超时
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
