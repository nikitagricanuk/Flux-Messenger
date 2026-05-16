package ru.flux.android.features.chat.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import ru.flux.android.R;
import ru.flux.android.features.chat.ProfileAdapter;

public class Profile extends Fragment {

    public Profile() {
        super(R.layout.fragment_profile);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewPager2 viewPager = view.findViewById(R.id.viewPager);

        ProfileAdapter adapter = new ProfileAdapter(
                getChildFragmentManager(), getLifecycle()
        );
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