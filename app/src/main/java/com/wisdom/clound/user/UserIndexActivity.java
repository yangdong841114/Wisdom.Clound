package com.wisdom.clound.user;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.wisdom.clound.R;
import com.wisdom.clound.ui.LoginActivity;

public class UserIndexActivity extends AppCompatActivity implements View.OnClickListener {

    // 返回按钮
    private ImageView ivBack;
    // 功能条目
    private LinearLayout llInfoManage;
    private LinearLayout llPwdManage;
    private LinearLayout llPayPwdManage;
    private LinearLayout llAddressManage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_index);

        // 初始化控件
        initView();
        // 设置点击事件
        setClickListener();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        ivBack = findViewById(R.id.iv_back);
        llInfoManage = findViewById(R.id.ll_info_manage);
        llPwdManage = findViewById(R.id.ll_pwd_manage);
        llPayPwdManage = findViewById(R.id.ll_pay_pwd_manage);
        llAddressManage = findViewById(R.id.ll_address_manage);
    }

    /**
     * 设置点击事件
     */
    private void setClickListener() {
        ivBack.setOnClickListener(this);
        llInfoManage.setOnClickListener(this);
        llPwdManage.setOnClickListener(this);
        llPayPwdManage.setOnClickListener(this);
        llAddressManage.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back) {
            // 返回按钮 - 关闭当前页面
            jumpToMineFragment();
        } else if (id == R.id.ll_info_manage) {
            // 信息管理
//            Toast.makeText(this, "点击了信息管理", Toast.LENGTH_SHORT).show();
            // 此处可添加跳转至信息管理页面的逻辑
             Intent intent = new Intent(this, UserInfoActivity.class);
             startActivity(intent);
        } else if (id == R.id.ll_pwd_manage) {
            // 密码管理
            Intent intent = new Intent(this, UserPwdActivity.class);
            startActivity(intent);
            // 此处可添加跳转至密码管理页面的逻辑
        } else if (id == R.id.ll_pay_pwd_manage) {
            // 密码管理
            Intent intent = new Intent(this, UserPayPwdActivity.class);
            startActivity(intent);
            // 此处可添加跳转至支付密码管理页面的逻辑
        } else if (id == R.id.ll_address_manage) {
            // 地址管理
            Intent intent = new Intent(this, UserAddressActivity.class);
            startActivity(intent);
            // 此处可添加跳转至地址管理页面的逻辑
        }
    }

    private void jumpToMineFragment() {
        // 假设MineFragment是在MainActivity中（底部导航/侧边栏切换）
        Intent intent = new Intent(UserIndexActivity.this, com.wisdom.clound.MainActivity.class);
        // 传递标记，让MainActivity切换到MineFragment（可选，根据你的MainActivity逻辑调整）
        intent.putExtra("target_fragment", "mine");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK); // 清除栈顶，避免重复页面
        startActivity(intent);
        finish(); // 关闭登录页
    }
}