package ru.flux.android.features.settings.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.materialswitch.MaterialSwitch;

import ru.flux.android.R;
import ru.flux.android.core.network.UpdateUserRequest;
import ru.flux.android.core.network.UserResponse;
import ru.flux.android.features.settings.SettingsViewModel;

public class SettingsNotificationsFragment extends Fragment {

    public SettingsNotificationsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_notifications, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SettingsViewModel viewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);
        MaterialSwitch sw = view.findViewById(R.id.switchNotifications);

        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            sw.setOnCheckedChangeListener(null);
            sw.setChecked(Boolean.TRUE.equals(user.notifications));
            sw.setOnCheckedChangeListener((btn, checked) -> {
                UserResponse current = viewModel.getUser().getValue();
                if (current == null) return;
                viewModel.saveUser(new UpdateUserRequest(
                        current.firstName,
                        current.lastName,
                        current.nickname,
                        current.phone,
                        current.email,
                        checked
                ));
            });
        });

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                requireActivity().getOnBackPressedDispatcher().onBackPressed());
    }
}
