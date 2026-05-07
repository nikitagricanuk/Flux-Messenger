package ru.flux.android.features.login.ui;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.Toast;

import ru.flux.android.MainActivity;
import ru.flux.android.features.login.LoggedInUserView;
import ru.flux.android.features.login.LoginFormState;
import ru.flux.android.features.login.LoginResult;
import ru.flux.android.features.login.LoginViewModel;
import ru.flux.android.features.login.LoginViewModelFactory;
import ru.flux.android.core.views.input.PasswordInputView;
import ru.flux.android.core.views.input.PhoneInputView;
import ru.flux.android.databinding.FragmentLoginBinding;
import ru.flux.android.R;

public class LoginFragment extends Fragment {

    private LoginViewModel loginViewModel;
    private FragmentLoginBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory(requireContext()))
                .get(LoginViewModel.class);

        final PhoneInputView phoneInputView = binding.phone;
        final PasswordInputView passwordInputView = binding.password;
        final Button loginButton = binding.login;

        loginViewModel.getLoginFormState().observe(getViewLifecycleOwner(), new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getPhoneError() != null) {
                    phoneInputView.setError(getString(loginFormState.getPhoneError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordInputView.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(getViewLifecycleOwner(), new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                }
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(
                        phoneInputView.getPhone(),
                        passwordInputView.getText().toString());
            }
        };
        phoneInputView.addTextChangedListener(afterTextChangedListener);
        passwordInputView.addTextChangedListener(afterTextChangedListener);
        passwordInputView.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                loginViewModel.login(
                        phoneInputView.getPhone(),
                        passwordInputView.getText().toString());
            }
            return false;
        });

        loginButton.setOnClickListener(v ->
                loginViewModel.login(
                        phoneInputView.getPhone(),
                        passwordInputView.getText().toString()));

        view.findViewById(R.id.textView5).setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_login_to_signup));
    }

    private void updateUiWithUser(LoggedInUserView model) {
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        if (getContext() != null && getContext().getApplicationContext() != null) {
            Toast.makeText(
                    getContext().getApplicationContext(),
                    errorString,
                    Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}