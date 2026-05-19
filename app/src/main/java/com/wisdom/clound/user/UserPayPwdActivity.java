package com.wisdom.clound.user;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.wisdom.clound.R;
import com.wisdom.clound.utils.SPUtils;
import org.json.JSONObject;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserPayPwdActivity extends AppCompatActivity {

    private EditText etOldPwd, etNewPwd, etConfirmPwd;
    private LinearLayout btnSavePwd;
    private ImageView ivBack;
    private String userId;
    // 接口返回的原密码（解密后）
    private String originalPwd;
    // 网络请求客户端
    private OkHttpClient okHttpClient;
    private static final String URL_UPDATE_USER_INFO = "https://api.rzkj.qyqd123.cn/Android/MineFragment/UserPayPwdEdit";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_pay);

        // 初始化OkHttp
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5000, java.util.concurrent.TimeUnit.MILLISECONDS)
                .readTimeout(5000, java.util.concurrent.TimeUnit.MILLISECONDS)
                .build();

        // 初始化控件
        initView();
        // 获取用户ID
        getUserId();
        // 从接口获取原密码
        getOriginalPwdFromApi();
        // 设置保存按钮点击事件
        setSaveBtnClickListener();
        toBack();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        etOldPwd = findViewById(R.id.et_old_pwd);
        etNewPwd = findViewById(R.id.et_new_pwd);
        etConfirmPwd = findViewById(R.id.et_confirm_pwd);
        btnSavePwd = findViewById(R.id.btn_save);
        ivBack = findViewById(R.id.iv_back);
    }

    /**
     * 从SP获取用户ID
     */
    private void getUserId() {
        userId = SPUtils.getUserId(getApplicationContext());
        if (userId.isEmpty()) {
            Toast.makeText(this, "用户ID获取失败，请重新登录", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * 从接口获取原密码
     */
    private void getOriginalPwdFromApi() {
        // 2. 构建请求URL
        String url = "https://api.rzkj.qyqd123.cn/Android/MineFragment/GetUserById?userId=" + userId;
        // 3. 构建请求
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();
        // 4. 发起异步请求
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                // 网络请求失败（子线程）
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(UserPayPwdActivity.this, "获取原密码失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                // 网络请求成功（子线程）
                if (response.isSuccessful() && response.body() != null) {
                    String responseStr = response.body().string();
                    try {
                        // 解析JSON
                        JSONObject jsonObject = new JSONObject(responseStr);
                        int code = jsonObject.getInt("code");
                        if (code == 200) {
                            JSONObject dataObj = jsonObject.getJSONObject("data");
                            // 获取解密后的原密码
                            originalPwd = dataObj.getString("UserPayPwdDecrypt");
                        } else {
                            String msg = jsonObject.getString("msg");
                            new Handler(Looper.getMainLooper()).post(() -> {
                                Toast.makeText(UserPayPwdActivity.this, msg, Toast.LENGTH_SHORT).show();
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        new Handler(Looper.getMainLooper()).post(() -> {
                            Toast.makeText(UserPayPwdActivity.this, "原密码解析失败：" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        Toast.makeText(UserPayPwdActivity.this, "接口返回空数据", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    /**
     * 设置保存按钮点击事件
     */
    private void setSaveBtnClickListener() {
        btnSavePwd.setOnClickListener(v -> {
            // 1. 获取输入的密码
            String inputOldPwd = etOldPwd.getText().toString().trim();
            String inputNewPwd = etNewPwd.getText().toString().trim();
            String inputConfirmPwd = etConfirmPwd.getText().toString().trim();

            // 2. 输入验证
            if (!validateInput(inputOldPwd, inputNewPwd, inputConfirmPwd)) {
                return;
            }

            // 3. 验证通过，执行密码修改逻辑（此处仅示例，需替换为实际修改密码的接口）
            modifyPassword(inputNewPwd);
        });
    }

    /**
     * 输入验证逻辑
     * @param inputOldPwd 输入的原密码
     * @param inputNewPwd 输入的新密码
     * @param inputConfirmPwd 输入的确认密码
     * @return 验证结果
     */
    private boolean validateInput(String inputOldPwd, String inputNewPwd, String inputConfirmPwd) {
        // 验证原密码是否为空
        if (inputOldPwd.isEmpty()) {
            Toast.makeText(this, "请输入原密码", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 验证新密码是否为空
        if (inputNewPwd.isEmpty()) {
            Toast.makeText(this, "请输入新密码", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 验证确认密码是否为空
        if (inputConfirmPwd.isEmpty()) {
            Toast.makeText(this, "请确认新密码", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 验证原密码是否正确
        if (originalPwd == null || !originalPwd.equals(inputOldPwd)) {
            Toast.makeText(this, "原密码输入错误", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 验证新密码是否和原密码一致
        if (inputNewPwd.equals(inputOldPwd)) {
            Toast.makeText(this, "新密码不能与原密码一致", Toast.LENGTH_SHORT).show();
            return false;
        }

        // 验证确认密码是否和新密码一致
        if (!inputConfirmPwd.equals(inputNewPwd)) {
            Toast.makeText(this, "确认密码与新密码不一致", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    /**
     * 执行密码修改逻辑（需替换为实际的修改密码接口）
     * @param newPwd 新密码
     */
    private void modifyPassword(String newPwd) {
        // 构建请求参数
        JSONObject params = new JSONObject();
        try {
            params.put("userId", userId);
            params.put("newPwd", newPwd);
        } catch (Exception e) {
            e.printStackTrace();
            showToast("参数构建失败");
            return;
        }

        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/json; charset=utf-8"),
                params.toString()
        );

        Request request = new Request.Builder()
                .url(URL_UPDATE_USER_INFO)
                .post(requestBody)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> showToast("保存失败：网络异常"));
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        try {
                            String responseStr = response.body().string();
                            JSONObject jsonObject = new JSONObject(responseStr);
                            if (jsonObject.getInt("code") == 200) {
                                showToast("修改密码成功");
                                jumpToMineFragment();
                            } else {
                                showToast(jsonObject.getString("msg"));
//                                showToast("保存失败：" + jsonObject.optString("msg", "未知错误"));
                            }
                        } catch (Exception e) {
                            showToast("解析返回结果失败");
                            e.printStackTrace();
                        }
                    } else {
                        showToast("保存失败：服务器错误");
                    }
                });
            }
        });
    }
    private void toBack() {
        ivBack.setOnClickListener(v -> {
            finish();
        });
    }
    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
    private void jumpToMineFragment() {
        // 假设MineFragment是在MainActivity中（底部导航/侧边栏切换）
        Intent intent = new Intent(UserPayPwdActivity.this, com.wisdom.clound.MainActivity.class);
        // 传递标记，让MainActivity切换到MineFragment（可选，根据你的MainActivity逻辑调整）
        intent.putExtra("target_fragment", "mine");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // 清除栈顶，避免重复页面
        startActivity(intent);
        finish(); // 关闭登录页
    }
}