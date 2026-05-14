package com.wisdom.clound.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.Lifecycle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpUtils {

    // 线程池（避免频繁创建线程）
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    // 主线程Handler（用于回调UI）
    private static final Handler mainHandler = new Handler(Looper.getMainLooper());

    // 回调接口（成功/失败）
    public interface HttpCallback {
        void onSuccess(String result);  // 请求成功，返回JSON字符串
        void onFailed(String error);    // 请求失败，返回错误信息
    }

    // -------------------------- 1. Http Get 请求 --------------------------
    // 原有无生命周期绑定的方法（兼容旧调用）
    public static void get(String url, Map<String, String> params, HttpUtils.HttpCallback callback) {
        get(url, params, null, callback);
    }

    // 简化：无参数Get（兼容旧调用）
    public static void get(String url, HttpUtils.HttpCallback callback) {
        get(url, null, null, callback);
    }

    // 新增：带生命周期绑定的Get重载（核心修复）
    public static void get(String url, Map<String, String> params, Lifecycle lifecycle, HttpUtils.HttpCallback callback) {
        String httpUrl = "https://api.rzkj.qyqd123.cn/Android" + url;
        Log.d("httpsUrl", httpUrl);
        executor.execute(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                // 拼接Get参数
                StringBuilder fullUrl = new StringBuilder(httpUrl);
                if (params != null && !params.isEmpty()) {
                    fullUrl.append("?");
                    for (Map.Entry<String, String> entry : params.entrySet()) {
                        fullUrl.append(URLEncoder.encode(entry.getKey(), "UTF-8"))
                                .append("=")
                                .append(URLEncoder.encode(entry.getValue(), "UTF-8"))
                                .append("&");
                    }
                    fullUrl.deleteCharAt(fullUrl.length() - 1);
                }

                // 建立连接
                URL apiUrl = new URL(fullUrl.toString());
                connection = (HttpURLConnection) apiUrl.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                // 获取响应码
                int code = connection.getResponseCode();
                if (code == 200) {
                    // 读取返回数据
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    // 主线程回调成功（先校验生命周期）
                    mainHandler.post(() -> {
                        if (isLifecycleValid(lifecycle)) {
                            callback.onSuccess(result.toString());
                        }
                    });
                } else {
                    mainHandler.post(() -> {
                        if (isLifecycleValid(lifecycle)) {
                            callback.onFailed("服务器错误，码：" + code);
                        }
                    });
                }

            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (isLifecycleValid(lifecycle)) {
                        callback.onFailed("网络请求失败：" + e.getMessage());
                    }
                });
            } finally {
                // 关闭资源
                try {
                    if (reader != null) reader.close();
                    if (connection != null) connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // -------------------------- 2. Http Post 请求（JSON参数） --------------------------
    // 原有无生命周期绑定的方法（兼容旧调用）
    public static void post(String url, String jsonParams, HttpUtils.HttpCallback callback) {
        post(url, jsonParams, null, callback);
    }

    // 新增：带生命周期绑定的Post重载（核心修复）
    public static void post(String url, String jsonParams, Lifecycle lifecycle, HttpUtils.HttpCallback callback) {
        String httpUrl = "https://api.rzkj.qyqd123.cn/Android" + url;
        executor.execute(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL apiUrl = new URL(httpUrl);
                connection = (HttpURLConnection) apiUrl.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                // Post JSON 必须设置请求头（适配99%后端API）
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                connection.setDoOutput(true); // 允许输出参数

                // 写入JSON参数
                if (jsonParams != null && !jsonParams.isEmpty()) {
                    OutputStream os = connection.getOutputStream();
                    os.write(jsonParams.getBytes("UTF-8"));
                    os.flush();
                    os.close();
                }

                // 获取响应
                int code = connection.getResponseCode();
                if (code == 200) {
                    reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }
                    // 主线程回调成功（先校验生命周期）
                    mainHandler.post(() -> {
                        if (isLifecycleValid(lifecycle)) {
                            callback.onSuccess(result.toString());
                        }
                    });
                } else {
                    mainHandler.post(() -> {
                        if (isLifecycleValid(lifecycle)) {
                            callback.onFailed("服务器错误，码：" + code);
                        }
                    });
                }

            } catch (Exception e) {
                mainHandler.post(() -> {
                    if (isLifecycleValid(lifecycle)) {
                        callback.onFailed("网络请求失败：" + e.getMessage());
                    }
                });
            } finally {
                try {
                    if (reader != null) reader.close();
                    if (connection != null) connection.disconnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    // -------------------------- 工具方法：校验生命周期有效性 --------------------------
    private static boolean isLifecycleValid(Lifecycle lifecycle) {
        // 1. 未传入生命周期 → 兼容旧逻辑，允许回调
        if (lifecycle == null) {
            return true;
        }
        // 2. 传入了生命周期 → 仅当处于STARTED/RESUMED时才允许回调
        return lifecycle.getCurrentState().isAtLeast(Lifecycle.State.STARTED);
    }
}
