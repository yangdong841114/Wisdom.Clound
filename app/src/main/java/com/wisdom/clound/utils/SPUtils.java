package com.wisdom.clound.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SPUtils {
    // SP文件名（全局唯一，避免和其他SP冲突）
    private static final String SP_NAME = "user_info";
    // userid的缓存key（全局唯一）
    public static final String KEY_USER_ID = "user_id";
    private static SharedPreferences sp;

    // 初始化SP（建议在Application中初始化，或使用时传入ApplicationContext）
    private static SharedPreferences getSP(Context context) {
        if (sp == null) {
            // 注意模式：MODE_PRIVATE（仅当前应用可访问）
            sp = context.getApplicationContext().getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        }
        return sp;
    }

    /**
     * 保存userid
     */
    public static void saveUserId(Context context, String userId) {
        getSP(context).edit().putString(KEY_USER_ID, userId).apply(); // apply()异步提交（推荐），commit()同步
    }

    /**
     * 读取userid
     * @return 无缓存时返回空字符串
     */
    public static String getUserId(Context context) {
        return getSP(context).getString(KEY_USER_ID, "");
    }

    /**
     * 清空userid（退出登录时用）
     */
    public static void clearUserId(Context context) {
        getSP(context).edit().remove(KEY_USER_ID).apply();
    }
}
