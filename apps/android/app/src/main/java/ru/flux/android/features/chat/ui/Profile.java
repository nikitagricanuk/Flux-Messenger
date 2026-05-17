package ru.flux.android.features.chat.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;

import java.util.UUID;

import ru.flux.android.R;
import ru.flux.android.features.chat.ProfileAdapter;
import ru.flux.android.features.chat.ProfileViewModel;

public class Profile extends Fragment {

    public Profile() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ProfileViewModel viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        setupTabs(view);
        setupBackButton(view);

        String contactIdStr = getArguments() != null
                ? getArguments().getString("contactId") : null;

        if (contactIdStr != null) {
            viewModel.loadUser(UUID.fromString(contactIdStr));
        }

        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            ((TextView) view.findViewById(R.id.profileName)).setText(
                    user.firstName + (user.lastName != null ? " " + user.lastName : ""));
            ((TextView) view.findViewById(R.id.profileUsername)).setText(
                    user.nickname != null ? "@" + user.nickname : "");
            ((TextView) view.findViewById(R.id.profileBio)).setText(
                    user.bio != null ? user.bio : "");

            if (user.avatarUrl != null) {
                Glide.with(requireContext())
                        .load(user.avatarUrl)
                        .centerCrop()
                        .into((ImageView) view.findViewById(R.id.profileAvatar));
            }
        });
    }

    private void setupTabs(View view) {
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);
        String chatId = getArguments() != null
                ? getArguments().getString("chatId")
                : null;

        viewPager.setAdapter(
                new ProfileAdapter(
                        getChildFragmentManager(),
                        getLifecycle(),
                        chatId
                )
        );

        TextView tabMedia = view.findViewById(R.id.tabMedia);
        TextView tabLinks = view.findViewById(R.id.tabLinks);
        TextView tabGroups = view.findViewById(R.id.tabGroups);

        tabMedia.setOnClickListener(v -> viewPager.setCurrentItem(0));
        tabLinks.setOnClickListener(v -> viewPager.setCurrentItem(1));
        tabGroups.setOnClickListener(v -> viewPager.setCurrentItem(2));

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateTabs(position, tabMedia, tabLinks, tabGroups);
            }
        });

        updateTabs(0, tabMedia, tabLinks, tabGroups);
    }

    private void setupBackButton(View view) {
        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                requireActivity().onBackPressed());
    }

    private void updateTabs(int position, TextView... tabs) {
        for (int i = 0; i < tabs.length; i++) {
            tabs[i].setBackgroundResource(i == position ? R.drawable.bg_tab_selected : 0);
            tabs[i].setAlpha(i == position ? 1f : 0.5f);
        }
    }
}