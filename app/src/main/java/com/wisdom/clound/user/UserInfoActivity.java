package com.wisdom.clound.user;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.wisdom.clound.R;
import com.wisdom.clound.utils.SPUtils;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UserInfoActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "UserInfoActivity";
    private static final int REQUEST_CODE_PICK_AVATAR = 101;
    private static final int REQUEST_CODE_CROP_AVATAR = 102;

    // UI控件
    private ImageView ivAvatar;
    private EditText etNickname;
    private EditText etAccount;
    private LinearLayout tvSave;
    private ImageView ivBack;

    // 网络请求客户端
    private OkHttpClient okHttpClient;
    // 用户信息
    private String userId;
    private String currentAvatarUrl;
    private File tempAvatarFile; // 裁剪后的头像临时文件

    // 接口地址
    private static final String URL_GET_USER_INFO = "https://api.rzkj.qyqd123.cn/Android/MineFragment/GetUserById";
    private static final String URL_UPDATE_USER_INFO = "https://api.rzkj.qyqd123.cn/Android/MineFragment/UserEdit"; // 假设的更新接口
    private static final String URL_UPDATE_AVATAR = "https://api.rzkj.qyqd123.cn/Android/MineFragment/UpdateAvatar"; // 假设的更新头像接口

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);

        // 初始化OkHttp
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5000, java.util.concurrent.TimeUnit.MILLISECONDS)
                .readTimeout(5000, java.util.concurrent.TimeUnit.MILLISECONDS)
                .build();

        // 初始化控件
        initView();
        // 获取用户ID
        userId = SPUtils.getUserId(getApplicationContext());
        if (userId.isEmpty()) {
            showToast("未获取到用户信息，请先登录");
            finish();
            return;
        }
        // 请求用户信息
        requestUserInfo();
    }

    private void initView() {
        ivAvatar = findViewById(R.id.iv_avatar);
        etNickname = findViewById(R.id.et_edit_nickname);
        etAccount = findViewById(R.id.et_edit_account);
        tvSave = findViewById(R.id.btn_save);
        ivBack = findViewById(R.id.iv_back);

        // 设置点击事件
        ivAvatar.setOnClickListener(this);
        tvSave.setOnClickListener(this);
        ivBack.setOnClickListener(this);

        // 账号输入框默认不可编辑（如需可编辑可去掉此行）
//        etAccount.setEnabled(false);
    }

    /**
     * 请求用户信息
     */
    private void requestUserInfo() {
        String url = URL_GET_USER_INFO + "?userId=" + userId;
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    showToast("网络异常，获取用户信息失败");
                    finish();
                });
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseStr = response.body().string();
                    try {
                        // 解析用户信息
                        JSONObject jsonObject = new JSONObject(responseStr);
                        if (jsonObject.getInt("code") == 200) {
                            JSONObject dataObj = jsonObject.getJSONObject("data");
                            String userName = dataObj.getString("UserName");
                            String userLoginName = dataObj.getString("UserLoginName");
                            currentAvatarUrl = dataObj.getString("UserAvatar");

                            // 更新UI
                            runOnUiThread(() -> {
                                etNickname.setText(userName);
                                etAccount.setText(userLoginName);
                                loadAvatar(currentAvatarUrl);
                            });
                        } else {
                            runOnUiThread(() -> {
                                showToast("获取用户信息失败：" + jsonObject.optString("msg", "未知错误"));
                                finish();
                            });
                        }
                    } catch (Exception e) {
                        runOnUiThread(() -> {
                            showToast("解析用户信息失败");
                            finish();
                        });
                        e.printStackTrace();
                    }
                } else {
                    runOnUiThread(() -> {
                        showToast("服务器返回错误，状态码：" + response.code());
                        finish();
                    });
                }
            }
        });
    }

    /**
     * 加载头像
     */
    private void loadAvatar(String avatarUrl) {
        if (avatarUrl.isEmpty()) {
            ivAvatar.setImageResource(R.drawable.ic_avatar);
            return;
        }

        // 异步加载头像
        new Thread(() -> {
            try {
                Request request = new Request.Builder().url(avatarUrl).build();
                Response response = okHttpClient.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
                    runOnUiThread(() -> {
                        if (bitmap != null) {
                            ivAvatar.setImageBitmap(bitmap);
                        } else {
                            ivAvatar.setImageResource(R.drawable.ic_avatar);
                        }
                    });
                } else {
                    runOnUiThread(() -> ivAvatar.setImageResource(R.drawable.ic_avatar));
                }
            } catch (Exception e) {
                runOnUiThread(() -> ivAvatar.setImageResource(R.drawable.ic_avatar));
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 选择头像（打开相册）
     */
    private void chooseAvatar() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_CODE_PICK_AVATAR);
    }

    /**
     * 裁剪头像
     */
    private void cropAvatar(Uri uri) {
        // 创建临时文件
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
            String fileName = "AVATAR_" + timeStamp + ".jpg";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            tempAvatarFile = File.createTempFile(fileName, ".jpg", storageDir);

            // 裁剪意图
            Intent intent = new Intent("com.android.camera.action.CROP");
            intent.setDataAndType(uri, "image/*");
            intent.putExtra("crop", "true");
            // 裁剪比例 1:1
            intent.putExtra("aspectX", 1);
            intent.putExtra("aspectY", 1);
            // 输出尺寸
            intent.putExtra("outputX", 200);
            intent.putExtra("outputY", 200);
            intent.putExtra("return-data", false);
            // 保存到临时文件
            Uri outputUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", tempAvatarFile);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputUri);
            intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
            intent.putExtra("noFaceDetection", true);

            startActivityForResult(intent, REQUEST_CODE_CROP_AVATAR);
        } catch (Exception e) {
            showToast("创建裁剪文件失败");
            e.printStackTrace();
        }
    }

    /**
     * 上传头像
     */
    private void uploadAvatar() {
        if (tempAvatarFile == null || !tempAvatarFile.exists()) {
            showToast("头像文件不存在");
            return;
        }

        // 构建请求体（根据实际接口要求调整，此处为示例）
        RequestBody requestBody = new okhttp3.MultipartBody.Builder()
                .setType(okhttp3.MultipartBody.FORM)
                .addFormDataPart("userId", userId)
                .addFormDataPart("avatar", tempAvatarFile.getName(),
                        RequestBody.create(MediaType.parse("image/jpeg"), tempAvatarFile))
                .build();

        Request request = new Request.Builder()
                .url(URL_UPDATE_AVATAR)
                .post(requestBody)
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> showToast("上传头像失败：网络异常"));
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        showToast("头像更新成功");
                        // 重新加载头像
                        loadAvatar(currentAvatarUrl); // 实际应替换为新头像URL
                    } else {
                        showToast("上传头像失败：服务器错误");
                    }
                });
            }
        });
    }

    /**
     * 保存用户信息（修改昵称/账号）
     */
    private void saveUserInfo() {
        String newNickname = etNickname.getText().toString().trim();
        String newAccount = etAccount.getText().toString().trim();

        if (newNickname.isEmpty()) {
            showToast("昵称不能为空");
            return;
        }

        // 构建请求参数
        JSONObject params = new JSONObject();
        try {
            params.put("userId", userId);
            params.put("userName", newNickname);
            params.put("userLoginName", newAccount); // 如需修改账号则传，否则去掉
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
                                showToast("修改个人信息成功");
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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_avatar) {
            // 选择头像
            chooseAvatar();
        } else if (id == R.id.btn_save) {
            // 保存信息
            saveUserInfo();
        } else if (id == R.id.iv_back) {
            // 返回
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_CODE_PICK_AVATAR) {
            // 选择图片返回
            if (data != null && data.getData() != null) {
                cropAvatar(data.getData());
            }
        } else if (requestCode == REQUEST_CODE_CROP_AVATAR) {
            // 裁剪图片返回
            if (tempAvatarFile != null && tempAvatarFile.exists()) {
                // 显示裁剪后的头像
                Bitmap bitmap = BitmapFactory.decodeFile(tempAvatarFile.getAbsolutePath());
                if (bitmap != null) {
                    ivAvatar.setImageBitmap(bitmap);
                    // 上传头像
                    uploadAvatar();
                }
            }
        }
    }

    /**
     * 安全显示Toast
     */
    private void showToast(String msg) {
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 清理临时文件
        if (tempAvatarFile != null && tempAvatarFile.exists()) {
            tempAvatarFile.delete();
        }
    }

    private void jumpToMineFragment() {
        // 假设MineFragment是在MainActivity中（底部导航/侧边栏切换）
        Intent intent = new Intent(UserInfoActivity.this, com.wisdom.clound.MainActivity.class);
        // 传递标记，让MainActivity切换到MineFragment（可选，根据你的MainActivity逻辑调整）
        intent.putExtra("target_fragment", "mine");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // 清除栈顶，避免重复页面
        startActivity(intent);
        finish(); // 关闭登录页
    }
}