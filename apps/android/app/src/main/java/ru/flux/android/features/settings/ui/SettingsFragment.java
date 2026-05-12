package ru.flux.android.features.settings.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import ru.flux.android.R;
import ru.flux.android.core.auth.TokenManager;
import ru.flux.android.databinding.FragmentSettingsBinding;
import ru.flux.android.features.settings.SettingsViewModel;
import ru.flux.android.features.login.LoginActivity;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;

    public SettingsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavController navController = NavHostFragment.findNavController(this);
        viewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);

        binding.profileButton.setOnClickListener(v -> navController.navigate(R.id.settingsProfileFragment));
        binding.notificationsButton.setOnClickListener(v -> navController.navigate(R.id.settingsNotificationsFragment));
        binding.logoutButton.setOnClickListener(v -> logout());

        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            String name = (user.firstName != null ? user.firstName : "")
                    + (user.lastName != null ? " " + user.lastName : "");
            binding.profileName.setText(name.trim());
            binding.profileUsername.setText(user.nickname != null ? "@" + user.nickname : "");
            boolean hasBio = user.bio != null && !user.bio.isBlank();
            binding.profileBio.setVisibility(hasBio ? View.VISIBLE : View.GONE);
            if (hasBio) binding.profileBio.setText(user.bio);
        });

        viewModel.loadUser();
    }

    private void logout() {
        try {
            new TokenManager(requireContext()).clearTokens();
        } catch (Exception ignored) {}
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}