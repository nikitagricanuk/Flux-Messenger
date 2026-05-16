package ru.flux.android.features.login.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.IOException;
import java.security.GeneralSecurityException;

import ru.flux.android.MainActivity;
import ru.flux.android.R;
import ru.flux.android.databinding.FragmentSignUpCompletion3rdPartyBinding;
import ru.flux.android.features.login.AuthRepositoryFactory;
import ru.flux.android.features.login.PasskeyAuthManager;

public class SignUpCompletion3rdPartyFragment extends Fragment {

    private FragmentSignUpCompletion3rdPartyBinding binding;
    private PasskeyAuthManager passkeyAuthManager;

    public SignUpCompletion3rdPartyFragment() {}

    public static SignUpCompletion3rdPartyFragment newInstance() {
        return new SignUpCompletion3rdPartyFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSignUpCompletion3rdPartyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            passkeyAuthManager = new PasskeyAuthManager(
                    (ComponentActivity) requireActivity(),
                    AuthRepositoryFactory.create(requireContext())
            );
        } catch (GeneralSecurityException | IOException e) {
            Toast.makeText(requireContext(), R.string.auth_init_failed, Toast.LENGTH_LONG).show();
        }

        TextWatcher validationWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String firstName = binding.avatarInput.getFirstText().toString().trim();
                String username = binding.etUsername.getText().toString().trim();
                String phone = binding.phoneInput.getPhone();
                boolean valid = !firstName.isEmpty()
                        && username.length() >= 3
                        && username.matches("[a-zA-Z0-9_]+")
                        && phone.length() >= 11;
                binding.login.setEnabled(valid);
            }
        };

        binding.avatarInput.addFirstTextChangedListener(validationWatcher);

        binding.etUsername.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tvAtSign.setTextColor(s.length() > 0 ? Color.BLACK : 0xFF9E9E9E);
            }

            @Override
            public void afterTextChanged(Editable s) {
                validationWatcher.afterTextChanged(s);
            }
        });

        binding.phoneInput.addTextChangedListener(validationWatcher);

        binding.login.setOnClickListener(v -> startPasskeyRegistration());
    }

    private void startPasskeyRegistration() {
        if (passkeyAuthManager == null) {
            Toast.makeText(requireContext(), R.string.auth_init_failed, Toast.LENGTH_LONG).show();
            return;
        }

        binding.login.setEnabled(false);
        String phone = binding.phoneInput.getPhone();

        passkeyAuthManager.register(
                (ComponentActivity) requireActivity(),
                phone,
                new PasskeyAuthManager.Callback() {
                    @Override
                    public void onSuccess() {
                        Intent intent = new Intent(requireActivity(), MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }

                    @Override
                    public void onError(@NonNull String message) {
                        binding.login.setEnabled(true);
                        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onRegistrationRequired() {}
                }
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}