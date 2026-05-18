package ru.flux.android.features.chat;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import ru.flux.android.features.chat.ui.GroupsFragment;
import ru.flux.android.features.chat.ui.LinksFragment;
import ru.flux.android.features.chat.ui.MediaFragment;
import ru.flux.android.features.chat.ui.MembersFragment;

public class ProfileAdapter extends FragmentStateAdapter {

    private final String chatId;
    private final boolean isGroup;

    public ProfileAdapter(
            @NonNull FragmentManager fm,
            @NonNull Lifecycle lifecycle,
            String chatId,
            boolean isGroup
    ) {
        super(fm, lifecycle);
        this.chatId = chatId;
        this.isGroup = isGroup;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        Fragment fragment;

        if (position == 0) fragment = new MediaFragment();
        else if (position == 1) fragment = new LinksFragment();
        else fragment = isGroup ? new MembersFragment() : new GroupsFragment();

        Bundle args = new Bundle();
        args.putString("chatId", chatId);
        args.putBoolean("isGroup", isGroup);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}