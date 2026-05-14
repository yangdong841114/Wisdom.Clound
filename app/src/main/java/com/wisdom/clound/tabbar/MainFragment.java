package com.wisdom.clound.tabbar;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.wisdom.clound.Bean.UserBean;
import com.wisdom.clound.R;
import com.wisdom.clound.ui.LoginActivity;
import com.wisdom.clound.utils.SPUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainFragment extends Fragment {

    // UI控件（新增淘金币控件）
    private ImageView ivAvatar;
    private TextView tvNickname;

    // 主线程Handler（更新UI用）
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    // ========== 核心修改1：避免Handler内存泄漏 ==========
    @Override
    public void onDestroy() {
        super.onDestroy();
        // 移除未执行的回调，避免内存泄漏+空Context回调
        mainHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        initView(rootView);

        // ========== 核心修改2：获取userId前先校验Context ==========
        String userId = SPUtils.getUserId(requireContext());
        if (userId.isEmpty()) {
            showLoginState();
        } else {
            requestUserInfo(userId);
        }

        tvNickname.setOnClickListener(v -> {
            String nicknameText = tvNickname.getText().toString().trim();
            if ("立即登录".equals(nicknameText)) {
                // 改用requireActivity()（自带非空校验，空则抛明确异常）
                Intent intent = new Intent(requireActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });


        return rootView;
    }

    private void initView(View rootView) {
        ivAvatar = rootView.findViewById(R.id.iv_avatar);
        tvNickname = rootView.findViewById(R.id.tv_nickname);
    }

    private void showLoginState() {
        tvNickname.setText("立即登录");
        ivAvatar.setImageResource(R.drawable.ic_avatar);
    }

    private void requestUserInfo(final String userId) {
        Log.d("Mine", "requestUserInfo: " + userId);
        new Thread(() -> {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL("http://你的接口域名/getUserById?userId=" + userId);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);

                if (connection.getResponseCode() == 200) {
                    InputStream inputStream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    UserBean userBean = UserBean.fromJson(response.toString());
                    mainHandler.post(() -> updateUserInfo(userBean));
                } else {
                    // ========== 核心修改4：Toast前校验Context ==========
                    mainHandler.post(() -> {
                        showToast("请求失败");
                        showLoginState();
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
                // ========== 核心修改4：Toast前校验Context ==========
                mainHandler.post(() -> {
                    showToast("网络异常");
                    showLoginState();
                });
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }

    private void updateUserInfo(UserBean userBean) {
        if (userBean.getNickname() != null && !userBean.getNickname().isEmpty()) {
            tvNickname.setText(userBean.getNickname());
        } else {
            tvNickname.setText("立即登录");
        }

        if (userBean.getAvatarUrl() != null && !userBean.getAvatarUrl().isEmpty()) {
            loadAvatar(userBean.getAvatarUrl());
        } else {
            ivAvatar.setImageResource(R.mipmap.ic_launcher);
        }

    }

    private void loadAvatar(final String avatarUrl) {
        new Thread(() -> {
            try {
                URL url = new URL(avatarUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(5000);
                InputStream inputStream = connection.getInputStream();
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                mainHandler.post(() -> {
                    // ========== 核心修改5：更新UI前校验View是否附着 ==========
                    if (isAdded() && !isDetached() && ivAvatar != null) {
                        ivAvatar.setImageBitmap(bitmap);
                    }
                });
                connection.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() -> {
                    if (isAdded() && !isDetached() && ivAvatar != null) {
                        ivAvatar.setImageResource(R.mipmap.ic_launcher);
                    }
                });
            }
        }).start();
    }

    // ========== 核心工具方法：安全显示Toast（统一处理Context空值） ==========
    private void showToast(String msg) {
        // 1. 校验Fragment是否附着到Activity
        if (!isAdded() || getActivity() == null) {
            android.util.Log.e("MineFragment", "Context为空，跳过Toast：" + msg);
            return;
        }
        // 2. 用Application Context避免Activity生命周期影响（可选，更安全）
        Toast.makeText(getActivity().getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
