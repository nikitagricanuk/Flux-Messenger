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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import ru.flux.android.MainActivity;
import ru.flux.android.databinding.FragmentSignUpCompletionInternalBinding;
import ru.flux.android.features.login.LoginViewModel;
import ru.flux.android.features.login.LoginViewModelFactory;

public class SignUpCompletionInternalFragment extends Fragment {

    private FragmentSignUpCompletionInternalBinding binding;
    private LoginViewModel loginViewModel;

    public SignUpCompletionInternalFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSignUpCompletionInternalBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory(requireContext()))
                .get(LoginViewModel.class);

        String phone = getArguments() != null
                ? getArguments().getString(SignUpAuthFragment.ARG_PHONE, "") : "";
        String password = getArguments() != null
                ? getArguments().getString(SignUpAuthFragment.ARG_PASSWORD, "") : "";

        loginViewModel.getSignUpResult().observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            if (result.getError() != null) {
                Toast.makeText(requireContext(), result.getError(), Toast.LENGTH_LONG).show();
                binding.btnDone.setClickable(true);
            }
            if (result.getSuccess() != null) {
                Intent intent = new Intent(requireActivity(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        TextWatcher validationWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String firstName = binding.etFirstName.getText().toString().trim();
                String username = binding.etUsername.getText().toString().trim();
                boolean valid = !firstName.isEmpty()
                        && username.length() >= 3
                        && username.matches("[a-zA-Z0-9_]+");
                binding.btnDone.setAlpha(valid ? 1f : 0.5f);
                binding.btnDone.setClickable(valid);
            }
        };

        binding.etFirstName.addTextChangedListener(validationWatcher);
        binding.etUsername.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                binding.tvAtSign.setTextColor(
                        s.length() > 0 ? Color.BLACK : Color.parseColor("#FF9E9E9E"));
            }

            @Override
            public void afterTextChanged(Editable s) {
                validationWatcher.afterTextChanged(s);
            }
        });

        binding.btnDone.setAlpha(0.5f);
        binding.btnDone.setClickable(false);
        binding.btnDone.setOnClickListener(v -> {
            String firstName = binding.etFirstName.getText().toString().trim();
            String lastName = binding.etLastName.getText().toString().trim();
            String username = binding.etUsername.getText().toString().trim();
            binding.btnDone.setClickable(false);
            loginViewModel.signUp(firstName, lastName, username, phone, password);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
