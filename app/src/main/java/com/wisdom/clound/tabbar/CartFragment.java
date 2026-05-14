package com.wisdom.clound.tabbar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.wisdom.clound.R;

public class CartFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 加载购物车空页面布局
        View view = inflater.inflate(R.layout.fragment_cart, container, false);

        // 绑定「去商城逛逛」按钮
        Button btnGoShop = view.findViewById(R.id.btn_go_shop);
        btnGoShop.setOnClickListener(v -> {
            // 核心：跳转到Home页（同步更新ViewPager2和底部导航）
            jumpToHomePage();
        });

        return view;
    }

    /**
     * 跳转到Home页：同时更新ViewPager2和底部导航选中状态（体验一致）
     */
    private void jumpToHomePage() {
        // 1. 判空：避免Fragment未依附Activity时崩溃
        if (getActivity() == null) {
            Toast.makeText(getContext(), "跳转失败，请稍后重试", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. 找到ViewPager2（ID和你布局中一致：viewPager）
        ViewPager2 viewPager2 = getActivity().findViewById(R.id.viewPager);
        if (viewPager2 == null) {
            Toast.makeText(getContext(), "未找到页面容器", Toast.LENGTH_SHORT).show();
            return;
        }

        // 3. 找到底部导航栏（同步选中Home项）
        BottomNavigationView bottomNav = getActivity().findViewById(R.id.bottomNavigation);
        if (bottomNav == null) {
            Toast.makeText(getContext(), "未找到底部导航", Toast.LENGTH_SHORT).show();
            return;
        }

        // 4. 核心操作：
        // 👉 把数字0改成Home页在ViewPager2中的索引（必改！）
        //    比如：Home=0、视频页=1、购物车=2
        viewPager2.setCurrentItem(0, true); // true=平滑切换到Home页
        bottomNav.setSelectedItemId(R.id.nav_home); // 同步选中底部导航的Home项（必改！）

        // 5. 提示跳转成功
//        Toast.makeText(getActivity(), "已跳转到商城首页", Toast.LENGTH_SHORT).show();
    }
}

