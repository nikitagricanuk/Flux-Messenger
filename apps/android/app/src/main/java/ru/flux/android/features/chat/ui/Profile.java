package ru.flux.android.features.chat.ui;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.View;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.flux.android.R;
import ru.flux.android.core.network.ApiClient;
import ru.flux.android.core.network.UserResponse;
import ru.flux.android.features.chat.ProfileAdapter;

public class Profile extends Fragment {

    private static final String TAG = "Profile";

    public Profile() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewPager2 viewPager = view.findViewById(R.id.viewPager);
        ProfileAdapter adapter = new ProfileAdapter(getChildFragmentManager(), getLifecycle());
        viewPager.setAdapter(adapter);

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

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                requireActivity().onBackPressed()
        );

        String contactIdStr = getArguments() != null
                ? getArguments().getString("contactId") : null;

        if (contactIdStr != null) {
            loadUserProfile(view, UUID.fromString(contactIdStr));
        }
    }

    private void loadUserProfile(View view, UUID userId) {
        try {
            ApiClient.api(requireContext()).getUserById(userId).enqueue(new Callback<UserResponse>() {
                @Override
                public void onResponse(@NonNull Call<UserResponse> call,
                                       @NonNull Response<UserResponse> response) {
                    if (!response.isSuccessful() || response.body() == null) {
                        Log.e(TAG, "getUserById failed: " + response.code());
                        return;
                    }
                    UserResponse user = response.body();

                    TextView profileName = view.findViewById(R.id.profileName);
                    TextView profileUsername = view.findViewById(R.id.profileUsername);
                    TextView profileBio = view.findViewById(R.id.profileBio);
                    ImageView profileAvatar = view.findViewById(R.id.profileAvatar);

                    String fullName = user.firstName +
                            (user.lastName != null ? " " + user.lastName : "");
                    profileName.setText(fullName);

                    profileUsername.setText(user.nickname != null
                            ? "@" + user.nickname : "");

                    profileBio.setText(user.bio != null ? user.bio : "");

                    if (user.avatarUrl != null) {
                        Glide.with(requireContext())
                                .load(user.avatarUrl)
                                .centerCrop()
                                .into(profileAvatar);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                    Log.e(TAG, "getUserById error", t);
                }
            });
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "ApiClient error", e);
        }
    }

    private void updateTabs(int position, TextView... tabs) {
        for (int i = 0; i < tabs.length; i++) {
            if (i == position) {
                tabs[i].setBackgroundResource(R.drawable.bg_tab_selected);
                tabs[i].setAlpha(1f);
            } else {
                tabs[i].setBackgroundResource(0);
                tabs[i].setAlpha(0.5f);
            }
        }
    }
}