package ru.flux.android.ui.login;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;
import java.security.GeneralSecurityException;

import ru.flux.android.ApiClient;
import ru.flux.android.data.AuthApi;
import ru.flux.android.data.LoginRepository;
import ru.flux.android.data.TokenManager;

public class LoginViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;

    public LoginViewModelFactory(Context context) {
        this.context = context.getApplicationContext();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(LoginViewModel.class)) {
            try {
                TokenManager tokenManager = new TokenManager(context);
                AuthApi authApi = ApiClient.getInstance(tokenManager).create(AuthApi.class);
                return (T) new LoginViewModel(new LoginRepository(authApi, tokenManager));
            } catch (GeneralSecurityException | IOException e) {
                throw new RuntimeException("Failed to initialize login", e);
            }
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
