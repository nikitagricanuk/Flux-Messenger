package ru.flux.android.features.login.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import ru.flux.android.R;
import ru.flux.android.core.views.input.PasswordInputView;
import ru.flux.android.core.views.input.PhoneInputView;
import ru.flux.android.databinding.FragmentSignUpAuthBinding;

public class SignUpAuthFragment extends Fragment {

    public static final String ARG_PHONE = "phone";
    public static final String ARG_PASSWORD = "password";

    private FragmentSignUpAuthBinding binding;

    public SignUpAuthFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentSignUpAuthBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PhoneInputView phoneInputView = binding.phoneNumber;
        PasswordInputView passwordInputView = binding.password;
        PasswordInputView repeatPasswordInputView = binding.repeatPassword;
        Button signUpButton = binding.login;

        TextWatcher validationWatcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String phone = phoneInputView.getPhone();
                String password = passwordInputView.getText().toString();
                String repeat = repeatPasswordInputView.getText().toString();

                boolean phoneValid = phone.length() >= 11;
                boolean passwordValid = password.length() > 5;
                boolean passwordsMatch = password.equals(repeat);

                if (!repeat.isEmpty() && !passwordsMatch) {
                    repeatPasswordInputView.setError(getString(R.string.passwords_do_not_match));
                } else {
                    repeatPasswordInputView.setError(null);
                }

                signUpButton.setEnabled(phoneValid && passwordValid && passwordsMatch);
            }
        };

        phoneInputView.addTextChangedListener(validationWatcher);
        passwordInputView.addTextChangedListener(validationWatcher);
        repeatPasswordInputView.addTextChangedListener(validationWatcher);

        signUpButton.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString(ARG_PHONE, phoneInputView.getPhone());
            args.putString(ARG_PASSWORD, passwordInputView.getText().toString());
            Navigation.findNavController(v).navigate(R.id.action_signup_to_completion, args);
        });

        binding.textView5.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_signup_to_login));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
