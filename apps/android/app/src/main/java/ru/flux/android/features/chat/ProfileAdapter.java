package ru.flux.android.features.chat;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import ru.flux.android.features.chat.ui.GroupsFragment;
import ru.flux.android.features.chat.ui.LinksFragment;
import ru.flux.android.features.chat.ui.MediaFragment;

public class ProfileAdapter  extends FragmentStateAdapter {

    public ProfileAdapter(@NonNull FragmentManager fm, Lifecycle lifecycle) {
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