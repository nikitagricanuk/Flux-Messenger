package ru.flux.android.features.settings.ui;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import java.util.Calendar;

import eightbitlab.com.blurview.BlurView;
import ru.flux.android.R;
import ru.flux.android.core.network.UpdateUserRequest;
import ru.flux.android.core.network.UserResponse;
import ru.flux.android.core.auth.TokenManager;
import ru.flux.android.features.settings.SettingsViewModel;
import ru.flux.android.features.login.LoginActivity;

public class SettingsProfileFragment extends Fragment {

    private SettingsViewModel viewModel;

    public SettingsProfileFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings_profile, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(SettingsViewModel.class);

        setupBlurViews(view);
        setupBirthDate(view);
        setupEditableRow(view, R.id.rowUsername, R.id.etUsername);
        setupEditableRow(view, R.id.rowPhone, R.id.etPhone);
        setupEditableRow(view, R.id.rowEmail, R.id.etEmail);

        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            setField(view, R.id.etFirstName, user.firstName);
            setField(view, R.id.etLastName, user.lastName);
            setField(view, R.id.etUsername, user.nickname);
            setField(view, R.id.etPhone, user.phone);
            setField(view, R.id.etEmail, user.email);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), err -> {
            if (err != null) Toast.makeText(requireContext(), err, Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.btnBack).setOnClickListener(v -> saveAndGoBack(view));

        view.findViewById(R.id.tvSignOut).setOnClickListener(v -> logout());

        view.findViewById(R.id.tvDeleteAccount).setOnClickListener(v ->
                viewModel.deleteAccount(() -> requireActivity().runOnUiThread(this::navigateToLogin)));
    }

    private void saveAndGoBack(View view) {
        String firstName = getFieldText(view, R.id.etFirstName);
        if (firstName.isEmpty()) {
            Toast.makeText(requireContext(), "Имя обязательно", Toast.LENGTH_SHORT).show();
            return;
        }
        UserResponse current = viewModel.getUser().getValue();
        viewModel.saveUser(new UpdateUserRequest(
                firstName,
                getFieldText(view, R.id.etLastName),
                getFieldText(view, R.id.etUsername),
                getFieldText(view, R.id.etPhone),
                getFieldText(view, R.id.etEmail),
                current != null ? current.notifications : null
        ));
        requireActivity().getOnBackPressedDispatcher().onBackPressed();
    }

    private void logout() {
        try { new TokenManager(requireContext()).clearTokens(); } catch (Exception ignored) {}
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void setField(View root, int id, String value) {
        if (value == null) return;
        ((EditText) root.findViewById(id)).setText(value);
    }

    private String getFieldText(View root, int id) {
        CharSequence text = ((EditText) root.findViewById(id)).getText();
        return text != null ? text.toString().trim() : "";
    }

    private void setupBlurViews(View root) {
        ViewGroup rootView = requireActivity().findViewById(android.R.id.content);
        Drawable windowBackground = requireActivity().getWindow().getDecorView().getBackground();

        int[] ids = { R.id.blurCardName, R.id.blurCardBio, R.id.cardBirthDate,
                      R.id.cardAccountInfo, R.id.cardActions };
        for (int id : ids) {
            BlurView bv = root.findViewById(id);
            if (bv == null) continue;
            bv.setClipToOutline(true);
            bv.setupWith(rootView)
                    .setFrameClearDrawable(windowBackground)
                    .setBlurRadius(20f);
        }
    }

    private void setupBirthDate(View root) {
        BlurView card = root.findViewById(R.id.cardBirthDate);
        TextView tvBirthDate = root.findViewById(R.id.tvBirthDate);
        View divider = root.findViewById(R.id.birthDateDivider);
        TextView tvDelete = root.findViewById(R.id.tvDeleteBirthDate);

        root.findViewById(R.id.rowBirthDate).setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(requireContext(), (picker, year, month, day) -> {
                tvBirthDate.setText(String.format("%02d.%02d.%d", day, month + 1, year));
                TransitionManager.beginDelayedTransition(card, new AutoTransition());
                divider.setVisibility(View.VISIBLE);
                tvDelete.setVisibility(View.VISIBLE);
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        tvDelete.setOnClickListener(v -> {
            tvBirthDate.setText("");
            TransitionManager.beginDelayedTransition(card, new AutoTransition());
            divider.setVisibility(View.GONE);
            tvDelete.setVisibility(View.GONE);
        });
    }

    private void setupEditableRow(View root, int rowId, int editTextId) {
        EditText et = root.findViewById(editTextId);
        root.findViewById(rowId).setOnClickListener(v -> {
            et.setFocusable(true);
            et.setFocusableInTouchMode(true);
            et.requestFocus();
            et.setSelection(et.getText().length());
            InputMethodManager imm = (InputMethodManager) requireContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
        });
    }
}