package ru.flux.android.data;

import java.io.IOException;

import retrofit2.Response;

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
public class LoginRepository {

    private final AuthApi      authApi;
    private final TokenManager tokenManager;

    public LoginRepository(AuthApi authApi, TokenManager tokenManager) {
        this.authApi      = authApi;
        this.tokenManager = tokenManager;
    }

    public boolean isLoggedIn() {
        return tokenManager.getAccessToken() != null;
    }

    // Run this off the main thread (Executor or LiveData + background thread)
    public Result<String> login(String phone, String password) {
        try {
            Response<AuthTokens> response =
                    authApi.login(new LoginRequest(phone, password)).execute();

            if (response.isSuccessful() && response.body() != null) {
                tokenManager.saveTokens(response.body());
                return new Result.Success<>(phone);
            } else {
                return new Result.Error(new Exception("Login failed: " + response.code()));
            }
        } catch (IOException e) {
            return new Result.Error(e);
        }
    }

    public void logout() {
        tokenManager.clearTokens();
    }
}