package com.wisdom.clound.ui;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.wisdom.clound.R;

import org.json.JSONException;
import org.json.JSONObject;

import com.wisdom.clound.utils.HttpUtils;

public class RegisterActivity extends AppCompatActivity {

    // 控件声明（不变）
    private EditText etInviteCode;
    private EditText etAccount;
    private EditText etPwd;
    private EditText etConfirmPwd;
    private ImageButton btnTogglePwd;
    private ImageButton btnToggleConfirmPwd;
    private TextView btnRegister;
    private TextView tvBackLogin;

    // 密码显示状态标记（不变）
    private boolean isPwdShow = false;
    private boolean isConfirmPwdShow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initView();
        setPwdToggleListener();
        setRegisterBtnListener();
        setBackLoginListener();
    }

    // 初始化控件（不变）
    private void initView() {
        etInviteCode = findViewById(R.id.et_invite_code);
        etAccount = findViewById(R.id.et_account);
        etPwd = findViewById(R.id.et_pwd);
        etConfirmPwd = findViewById(R.id.et_confirm_pwd);
        btnTogglePwd = findViewById(R.id.btn_toggle_pwd);
        btnToggleConfirmPwd = findViewById(R.id.btn_toggle_confirm_pwd);
        btnRegister = findViewById(R.id.btn_register);
        tvBackLogin = findViewById(R.id.tv_back_login);
    }

    // 密码显示/隐藏切换（不变）
    private void setPwdToggleListener() {
        btnTogglePwd.setOnClickListener(v -> {
            if (isPwdShow) {
                etPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnTogglePwd.setImageResource(R.drawable.ic_eye_close);
            } else {
                etPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnTogglePwd.setImageResource(R.drawable.ic_eye_open);
            }
            isPwdShow = !isPwdShow;
            etPwd.setSelection(etPwd.getText().length());
        });

        btnToggleConfirmPwd.setOnClickListener(v -> {
            if (isConfirmPwdShow) {
                etConfirmPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                btnToggleConfirmPwd.setImageResource(R.drawable.ic_eye_close);
            } else {
                etConfirmPwd.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                btnToggleConfirmPwd.setImageResource(R.drawable.ic_eye_open);
            }
            isConfirmPwdShow = !isConfirmPwdShow;
            etConfirmPwd.setSelection(etConfirmPwd.getText().length());
        });
    }

    // 注册按钮点击事件（不变）
    private void setRegisterBtnListener() {
        btnRegister.setOnClickListener(v -> {
            String inviteCode = etInviteCode.getText().toString().trim();
            String account = etAccount.getText().toString().trim();
            String pwd = etPwd.getText().toString().trim();
            String confirmPwd = etConfirmPwd.getText().toString().trim();

            if (!checkInput(account, pwd, confirmPwd)) {
                return;
            }

            // 调用真实注册接口（替换原模拟逻辑）
            doRegister(inviteCode, account, pwd);
        });
    }

    // 输入校验（不变）
    private boolean checkInput(String account, String pwd, String confirmPwd) {
        if (account.isEmpty()) {
            Toast.makeText(this, "请输入账号", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (pwd.isEmpty()) {
            Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (pwd.length() < 6) {
            Toast.makeText(this, "密码长度不能少于6位", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (confirmPwd.isEmpty()) {
            Toast.makeText(this, "请确认密码", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!pwd.equals(confirmPwd)) {
            Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    /**
     * 核心修改：调用封装的POST接口完成注册
     * @param inviteCode 邀请码
     * @param account 账号
     * @param pwd 密码
     */
    private void doRegister(String inviteCode, String account, String pwd) {
        // 1. 获取手机唯一标识（Android ID，适配所有版本）
        String deviceId = getDeviceUniqueId();

        // 2. 构造POST请求的JSON参数
        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("inviteCode", inviteCode); // 邀请码（可为空）
            jsonParams.put("account", account);       // 账号
            jsonParams.put("password", pwd);          // 密码
            jsonParams.put("deviceId", deviceId);     // 设备唯一标识
        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "参数构造失败", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. 调用你封装的POST方法（绑定生命周期，避免内存泄漏）
        HttpUtils.post(
                "/RegisterActivity/Register",                // API路径（拼接基础URL后为完整地址）
                jsonParams.toString(),      // JSON参数
                getLifecycle(),             // 绑定Activity生命周期
                new HttpUtils.HttpCallback() {
                    @Override
                    public void onSuccess(String result) {
                        // 注册成功：解析返回结果（根据后端格式调整）
                        try {
                            JSONObject response = new JSONObject(result);
                            int code = response.getInt("code"); // 假设后端返回code=200为成功
                            String msg = response.getString("msg");
                            if (code == 200) {
                                Toast.makeText(RegisterActivity.this, "注册成功！即将跳转到登录页", Toast.LENGTH_SHORT).show();
                                // 延迟跳转登录页
                                new android.os.Handler().postDelayed(() -> {
                                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                    startActivity(intent);
                                    finish();
                                }, 1000);
                            } else {
                                Log.d("RegisterActivity", "onSuccess: ");
                                Log.d("RegisterActivity", jsonParams.toString());
                                Toast.makeText(RegisterActivity.this, "注册失败：系统错误", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(RegisterActivity.this, "响应解析失败", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailed(String error) {
                        // 注册失败：提示错误信息
                        Toast.makeText(RegisterActivity.this, "注册失败：" + error, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    /**
     * 获取手机唯一标识（Android ID）
     * 兼容 Android 所有版本，无需动态权限（Android 10+ 也可正常获取）
     */
    private String getDeviceUniqueId() {
        String androidId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );
        // 兜底：如果Android ID为空，用设备信息拼接唯一标识
        if (androidId == null || androidId.isEmpty()) {
            androidId = Build.MODEL + "_" + Build.SERIAL + "_" + System.currentTimeMillis();
        }
        return androidId;
    }

    // 返回登录（不变）
    private void setBackLoginListener() {
        tvBackLogin.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
