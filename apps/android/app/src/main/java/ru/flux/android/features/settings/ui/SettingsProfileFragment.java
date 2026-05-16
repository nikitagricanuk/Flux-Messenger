package ru.flux.android.features.settings.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import com.bumptech.glide.Glide;

import java.util.Calendar;

import ru.flux.android.R;
import ru.flux.android.core.auth.TokenManager;
import ru.flux.android.core.network.UpdateUserRequest;
import ru.flux.android.core.network.UserResponse;
import ru.flux.android.databinding.FragmentSettingsProfileBinding;
import ru.flux.android.features.login.LoginActivity;
import ru.flux.android.features.settings.SettingsViewModel;

public class SettingsProfileFragment extends Fragment {

    private static final String TAG = "SettingsProfileFragment";
    private FragmentSettingsProfileBinding binding;
    private SettingsViewModel viewModel;

    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    public SettingsProfileFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri == null) return;
            Glide.with(this).load(uri).circleCrop()
                    .into(binding.blurCardName.getAvatar());
            viewModel.uploadAvatar(uri);
        });
        binding = FragmentSettingsProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);

        setupBirthDate();
        setupEditableRow(binding.rowUsername, binding.etUsername);
        setupEditableRow(binding.rowPhone, binding.etPhone);
        setupEditableRow(binding.rowEmail, binding.etEmail);

        viewModel.loadUser();

        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            binding.blurCardName.setFirstText(user.firstName);
            binding.blurCardName.setSecondText(user.lastName);
            setField(binding.etUsername, user.nickname);
            setField(binding.etPhone, user.phone);
            setField(binding.etEmail, user.email);
            if (user.bio != null) {
                binding.etBio.setText(user.bio);
            }
            if (user.dateOfBirth != null && !user.dateOfBirth.isEmpty()) {
                binding.tvBirthDate.setText(isoToDisplay(user.dateOfBirth));
                TransitionManager.beginDelayedTransition(binding.cardBirthDate, new AutoTransition());
                binding.birthDateDivider.setVisibility(View.VISIBLE);
                binding.tvDeleteBirthDate.setVisibility(View.VISIBLE);
            }
            if (user.avatarUrl != null && !user.avatarUrl.isBlank()) {
                Glide.with(requireContext()).load(user.avatarUrl)
                                .placeholder(R.drawable.bg_avatar_placeholder)
                                .error(R.drawable.bg_avatar_placeholder)
                                .circleCrop()
                                .into(binding.blurCardName.getAvatar());
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null) Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
        });

        binding.btnBack.setOnClickListener(v -> saveAndGoBack());
        binding.tvSignOut.setOnClickListener(v -> logout());
        binding.tvDeleteAccount.setOnClickListener(v ->
                viewModel.deleteAccount(() -> requireActivity().runOnUiThread(this::navigateToLogin)));

        binding.blurCardName.getAvatar().setOnClickListener(v -> {
            pickMedia.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });
    }

    private void saveAndGoBack() {
        String firstName = binding.blurCardName.getFirstText().toString().trim();
        if (firstName.isEmpty()) {
            Log.w(TAG, "saveAndGoBack: firstName is empty");
            Toast.makeText(requireContext(), "Имя обязательно", Toast.LENGTH_SHORT).show();
            return;
        }

        UserResponse current = viewModel.getUser().getValue();

        // Use current values as fallback if required fields were cleared
        String nickname = binding.etUsername.getText().toString().trim();
        if (nickname.isEmpty() && current != null && current.nickname != null) {
            nickname = current.nickname;
        }
        String phone = binding.etPhone.getText().toString().trim();
        if (phone.isEmpty() && current != null && current.phone != null) {
            phone = current.phone;
        }

        String email = binding.etEmail.getText().toString().trim();
        String bio = binding.etBio.getText().toString().trim();
        String dateOfBirth = displayToIso(binding.tvBirthDate.getText().toString().trim());

        Log.d(TAG, "saveAndGoBack: saving profile, nickname=" + nickname + ", dob=" + dateOfBirth);
        viewModel.saveUser(new UpdateUserRequest(
                firstName,
                binding.blurCardName.getSecondText().toString().trim(),
                nickname,
                dateOfBirth,
                phone,
                email.isEmpty() ? null : email,
                current != null ? current.notifications : null,
                bio.isEmpty() ? null : bio,
                current != null ? current.avatarUrl : null
        ));
        requireActivity().getOnBackPressedDispatcher().onBackPressed();
    }

    private void logout() {
        Log.d(TAG, "logout");
        try { new TokenManager(requireContext()).clearTokens(); } catch (Exception e) {
            Log.e(TAG, "logout: failed to clear tokens", e);
        }
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private static void setField(android.widget.EditText field, String value) {
        if (value != null) field.setText(value);
    }

    /** "DD.MM.YYYY" → "YYYY-MM-DD" for the backend, null if blank */
    private static String displayToIso(String display) {
        if (display == null || display.isEmpty()) return null;
        String[] parts = display.split("\\.");
        if (parts.length != 3) return null;
        return parts[2] + "-" + parts[1] + "-" + parts[0];
    }

    /** "YYYY-MM-DD" → "DD.MM.YYYY" for display */
    private static String isoToDisplay(String iso) {
        if (iso == null || iso.isEmpty()) return "";
        String[] parts = iso.split("-");
        if (parts.length != 3) return iso;
        return parts[2] + "." + parts[1] + "." + parts[0];
    }

    private void setupBirthDate() {
        binding.rowBirthDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (picker, year, month, day) -> {
                binding.tvBirthDate.setText(String.format(java.util.Locale.ROOT, "%02d.%02d.%d", day, month + 1, year));
                TransitionManager.beginDelayedTransition(binding.cardBirthDate, new AutoTransition());
                binding.birthDateDivider.setVisibility(View.VISIBLE);
                binding.tvDeleteBirthDate.setVisibility(View.VISIBLE);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        binding.tvDeleteBirthDate.setOnClickListener(v -> {
            binding.tvBirthDate.setText("");
            TransitionManager.beginDelayedTransition(binding.cardBirthDate, new AutoTransition());
            binding.birthDateDivider.setVisibility(View.GONE);
            binding.tvDeleteBirthDate.setVisibility(View.GONE);
        });
    }

    private void setupEditableRow(View row, android.widget.EditText et) {
        View.OnClickListener listener = v -> {
            et.setFocusable(true);
            et.setFocusableInTouchMode(true);
            et.requestFocus();
            et.setSelection(et.getText().length());
            InputMethodManager imm = (InputMethodManager) requireContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
        };
        row.setOnClickListener(listener);
        et.setOnClickListener(listener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}