package com.wisdom.clound.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.wisdom.clound.tabbar.CartFragment;
import com.wisdom.clound.tabbar.HomeFragment;
import com.wisdom.clound.tabbar.MainFragment;
import com.wisdom.clound.tabbar.VideoFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // 根据位置返回对应Fragment
        switch (position) {
            case 1:
                return new VideoFragment();
            case 2:
                return new CartFragment();
            case 3:
                return new MainFragment();
            default:
                return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        // 4个Tab
        return 4;
    }
}
