package com.wisdom.clound.tabbar;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.wisdom.clound.Bean.UserResponse;
import com.wisdom.clound.R;
import com.wisdom.clound.ui.LoginActivity;
import com.wisdom.clound.user.UserIndexActivity;
import com.wisdom.clound.utils.OkHttpUtils;
import com.wisdom.clound.utils.SPUtils;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainFragment extends Fragment {

    private ImageView ivAvatar;
    private TextView tvNickname;
    private TextView tvUserCode;
    private TextView tvUserStatus;
    private TextView tvUserFee;
    private TextView tvUserWallet;
    private TextView tvUserPoints;
    private TextView tvUserGrade;
    private LinearLayout tmExit; // 退出按钮
    private LinearLayout tmUserInfo;
    private SwipeRefreshLayout swipeRefreshLayout;

    private OkHttpClient okHttpClient;
    private Gson gson;
    private Handler mainHandler;
    private static final String TAG = "MainFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        okHttpClient = OkHttpUtils.getInstance();
        gson = new Gson();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        initView(rootView);
        initListener();
        checkLoginStatus();
        return rootView;
    }

    /**
     * 初始化控件
     */
    private void initView(View rootView) {
        // 原有控件初始化
        ivAvatar = rootView.findViewById(R.id.iv_avatar);
        tvNickname = rootView.findViewById(R.id.tv_userName);
        tvUserCode = rootView.findViewById(R.id.tv_code);
        tvUserStatus = rootView.findViewById(R.id.tv_status);
        tvUserFee = rootView.findViewById(R.id.tv_fee);
        tvUserWallet = rootView.findViewById(R.id.tv_wallet);
        tvUserPoints = rootView.findViewById(R.id.tv_points);
        tvUserGrade = rootView.findViewById(R.id.tv_grade);
        tmExit = rootView.findViewById(R.id.item_exit);
        tmUserInfo = rootView.findViewById(R.id.item_userInfo);

        // 下拉刷新控件
        swipeRefreshLayout = rootView.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_light,
                android.R.color.holo_red_light,
                android.R.color.holo_green_light
        );
    }

    /**
     * 初始化监听 → 新增【退出按钮】点击监听
     */
    private void initListener() {
        // 原有昵称点击监听
        tvNickname.setOnClickListener(v -> {
            String nicknameText = tvNickname.getText().toString().trim();
            if ("立即登录".equals(nicknameText)) {
                if (getActivity() == null) return;
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });

        // 新增：退出按钮点击监听
        tmExit.setOnClickListener(v -> showExitConfirmDialog());

        // 新增：用户信息条目（item_userInfo）点击监听
        tmUserInfo.setOnClickListener(v -> {
            // 1. 判断是否已登录（未登录则提示并跳转到登录页）
            String userId = SPUtils.getUserId(getContext());
            if (TextUtils.isEmpty(userId) || "立即登录".equals(tvNickname.getText().toString().trim())) {
                showToast("请登录后再使用此功能");
                Intent loginIntent = new Intent(getActivity(), LoginActivity.class);
                startActivity(loginIntent);
                return;
            }

            // 2. 已登录则跳转到 UserIndexActivity
            if (getActivity() == null) return;
            Intent userIndexIntent = new Intent(getActivity(), UserIndexActivity.class);
            // 可选：传递用户信息到目标页面（比如userId、用户名等）
            userIndexIntent.putExtra("userId", userId);
            startActivity(userIndexIntent);
        });

        // 下拉刷新监听
        swipeRefreshLayout.setOnRefreshListener(this::checkLoginStatus);
    }

    /**
     * 显示退出确认弹窗
     */
    private void showExitConfirmDialog() {
        if (!isAdded() || getActivity() == null) return;

        new AlertDialog.Builder(getActivity())
                .setTitle("退出确认")
                .setMessage("是否确定退出当前账号？")
                .setPositiveButton("确定", (dialog, which) -> {
                    // 1. 清空userId缓存
                    SPUtils.clearUserId(getContext());
                    Log.d(TAG, "已清空用户ID缓存");
                    // 2. 刷新页面（重新检查登录状态）
                    checkLoginStatus();
                    // 3. 提示退出成功
                    showToast("退出成功");
                    // 4. 关闭对话框
                    dialog.dismiss();
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    // 取消退出，关闭对话框
                    dialog.dismiss();
                })
                .setCancelable(true) // 点击外部可取消
                .show();
    }

    /**
     * 检查登录状态：有userId则请求用户信息，无则显示登录入口
     */
    private void checkLoginStatus() {
        if (!isAdded() || getContext() == null) return;

        String userId = SPUtils.getUserId(getContext());
        Log.d(TAG, "当前用户ID：" + userId);

        if (TextUtils.isEmpty(userId)) {
            showLoginEntry();
        } else {
            requestUserInfo(userId);
        }
    }

    /**
     * 显示未登录状态
     */
    private void showLoginEntry() {
        tvNickname.setText("立即登录");
        ivAvatar.setImageResource(R.drawable.ic_avatar);
        // 清空其他用户信息（可选，优化体验）
        tvUserCode.setText("000000");
        tvUserStatus.setText("未激活");
        tvUserFee.setText("0%");
        tvUserWallet.setText("￥0元");
        tvUserPoints.setText("0.00");
        tvUserGrade.setText("无团队级别");
        // 停止下拉刷新动画
        stopRefreshAnimation();
    }

    /**
     * 请求用户信息接口
     * @param userId 用户ID
     */
    private void requestUserInfo(String userId) {
        if (okHttpClient == null) {
            Log.e(TAG, "OkHttpClient 初始化失败，无法发起请求");
            showLoginEntry();
            return;
        }

        String apiUrl = "https://api.rzkj.qyqd123.cn/Android/MineFragment/GetUser?userId=" + userId;
        Request request = new Request.Builder()
                .url(apiUrl)
                .get()
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "用户信息请求失败：" + e.getMessage());
                mainHandler.post(() -> {
                    showLoginEntry();
                    stopRefreshAnimation();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "响应失败或响应体为空");
                    mainHandler.post(() -> {
                        showLoginEntry();
                        stopRefreshAnimation();
                    });
                    return;
                }

                String jsonStr = response.body().string();
                if (TextUtils.isEmpty(jsonStr)) {
                    Log.e(TAG, "响应体JSON为空");
                    mainHandler.post(() -> {
                        showLoginEntry();
                        stopRefreshAnimation();
                    });
                    return;
                }

                try {
                    UserResponse userResponse = gson.fromJson(jsonStr, UserResponse.class);
                    if (userResponse != null && userResponse.getCode() == 200 && userResponse.getData() != null) {
                        String userName = userResponse.getData().getUserName();
                        String userAvatar = userResponse.getData().getUserAvatar();
                        String userCode = userResponse.getData().getUserCode();
                        String userGrade = userResponse.getData().getUserGrade();
                        String userStatus = userResponse.getData().getUserStatus();
                        String userWallet = userResponse.getData().getUserWallet();
                        String userPoints = userResponse.getData().getUserPoints();
                        String userFee = userResponse.getData().getUserFee();
                        mainHandler.post(() -> {
                            updateUserUI(userName, userAvatar, userCode, userGrade, userStatus, userWallet, userPoints, userFee);
                            stopRefreshAnimation();
                        });
                    } else {
                        Log.e(TAG, "接口返回异常：" + (userResponse != null ? userResponse.getCode() : "null"));
                        mainHandler.post(() -> {
                            showLoginEntry();
                            stopRefreshAnimation();
                        });
                    }
                } catch (Exception e) {
                    Log.e(TAG, "JSON解析失败：" + e.getMessage());
                    mainHandler.post(() -> {
                        showLoginEntry();
                        stopRefreshAnimation();
                    });
                }
            }
        });
    }

    /**
     * 更新用户信息UI
     */
    private void updateUserUI(String userName, String userAvatar, String userCode, String userGrade, String userStatus, String userWallet, String userPoints, String userFee) {
        if (!isAdded() || getContext() == null) return;

        tvNickname.setText(TextUtils.isEmpty(userName) ? "立即登录" : userName);
        tvUserCode.setText(TextUtils.isEmpty(userCode) ? "000000" : userCode);
        tvUserGrade.setText(TextUtils.isEmpty(userGrade) ? "普通会员" : userGrade);
        tvUserStatus.setText(TextUtils.isEmpty(userStatus) ? "未激活" : userStatus);
        tvUserWallet.setText(TextUtils.isEmpty(userWallet) ? "￥0.00元" : userWallet);
        tvUserPoints.setText(TextUtils.isEmpty(userPoints) ? "0.00" : userPoints);
        tvUserFee.setText(TextUtils.isEmpty(userFee) ? "0%" : userFee);

        Glide.with(this)
                .load(userAvatar)
                .placeholder(R.drawable.ic_avatar)
                .error(R.drawable.ic_avatar)
                .into(ivAvatar);

        stopRefreshAnimation();
    }

    /**
     * 停止下拉刷新动画
     */
    private void stopRefreshAnimation() {
        if (swipeRefreshLayout != null && swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }

    /**
     * 安全显示Toast
     */
    private void showToast(String msg) {
        if (!isAdded() || getActivity() == null) return;
        Toast.makeText(getActivity().getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * 销毁时移除Handler回调+停止刷新
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }
        stopRefreshAnimation();
    }
}