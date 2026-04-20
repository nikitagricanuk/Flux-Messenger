package ru.flux.android;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ProfilePagerAdapter extends FragmentStateAdapter {

    public ProfilePagerAdapter(@NonNull FragmentManager fm, Lifecycle lifecycle) {
        super(fm, lifecycle);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new MediaFragment();
            case 1: return new LinksFragment();
            case 2: return new GroupsFragment();
            default: return new MediaFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}