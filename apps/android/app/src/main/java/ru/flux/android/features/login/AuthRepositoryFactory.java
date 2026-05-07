package ru.flux.android.features.login;

import android.content.Context;

import java.io.IOException;
import java.security.GeneralSecurityException;

import ru.flux.android.core.auth.TokenManager;
import ru.flux.android.core.network.ApiClient;
import ru.flux.android.core.network.AuthApi;

public final class AuthRepositoryFactory {

    private AuthRepositoryFactory() {}

    public static LoginRepository create(Context context)
            throws GeneralSecurityException, IOException {
        Context appContext = context.getApplicationContext();
        TokenManager tokenManager = new TokenManager(appContext);
        AuthApi authApi = ApiClient.getInstance(tokenManager).create(AuthApi.class);
        return new LoginRepository(authApi, tokenManager);
    }
}
