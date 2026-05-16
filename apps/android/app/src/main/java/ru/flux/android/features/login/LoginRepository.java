package ru.flux.android.features.login;

import android.util.Log;

import java.io.IOException;

import com.google.gson.Gson;

import retrofit2.Response;
import ru.flux.android.BuildConfig;
import ru.flux.android.core.Result;
import ru.flux.android.core.auth.AuthTokens;
import ru.flux.android.core.auth.TokenManager;
import ru.flux.android.core.network.AuthApi;
import ru.flux.android.core.network.LoginRequest;
import ru.flux.android.core.network.PasskeyAssertionOptionsResponse;
import ru.flux.android.core.network.PasskeyAssertionRequest;
import ru.flux.android.core.network.SignUpRequest;

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of login status and user credentials information.
 */
public class LoginRepository {

    private static final String TAG = "LoginRepository";

    private final AuthApi      authApi;
    private final TokenManager tokenManager;

    public LoginRepository(AuthApi authApi, TokenManager tokenManager) {
        this.authApi      = authApi;
        this.tokenManager = tokenManager;
    }

    public boolean isLoggedIn() {
        boolean loggedIn = tokenManager.getAccessToken() != null;
        Log.d(TAG, "isLoggedIn: " + loggedIn);
        return loggedIn;
    }

    // Run this off the main thread (Executor or LiveData + background thread)
    public Result<String> login(String phone, String password) {
        Log.d(TAG, "login: phone=" + phone);
        try {
            Response<AuthTokens> response =
                    authApi.login(new LoginRequest(phone, password)).execute();

            if (response.isSuccessful() && response.body() != null) {
                Log.d(TAG, "login: success");
                tokenManager.saveTokens(response.body());
                return new Result.Success<>(phone);
            } else {
                String loginErr = response.errorBody() != null ? response.errorBody().string() : "";
                Log.e(TAG, "login: failed, code=" + response.code() + " body=" + loginErr);
                return new Result.Error(new Exception("Login failed: " + response.code()));
            }
        } catch (IOException e) {
            Log.e(TAG, "login: network error", e);
            return new Result.Error(e);
        }
    }

    public Result<String> signUp(String firstName, String lastName, String username,
                                  String phone, String password) {
        Log.d(TAG, "signUp: phone=" + phone + ", username=" + username);
        try {
            Response<AuthTokens> response =
                    authApi.signUp(new SignUpRequest(firstName, lastName, username, phone, password))
                            .execute();
            if (response.isSuccessful() && response.body() != null) {
                Log.d(TAG, "signUp: success");
                tokenManager.saveTokens(response.body());
                return new Result.Success<>(phone);
            } else {
                String signUpErr = response.errorBody() != null ? response.errorBody().string() : "";
                Log.e(TAG, "signUp: failed, code=" + response.code() + " body=" + signUpErr);
                return new Result.Error(new Exception("Sign up failed: " + response.code()));
            }
        } catch (IOException e) {
            Log.e(TAG, "signUp: network error", e);
            return new Result.Error(e);
        }
    }

    public Result<PasskeyAssertionOptionsResponse> getPasskeyAssertionOptions() {
        Log.d(TAG, "getPasskeyAssertionOptions");
        try {
            String url = resolveUrl(BuildConfig.PASSKEY_OPTIONS_PATH);
            Response<PasskeyAssertionOptionsResponse> response =
                    authApi.getPasskeyAssertionOptions(url).execute();
            if (response.isSuccessful() && response.body() != null
                    && response.body().getChallenge() != null
                    && !response.body().getChallenge().isEmpty()) {
                Log.d(TAG, "getPasskeyAssertionOptions: success");
                return new Result.Success<>(response.body());
            } else {
                String passkeyOptErr = response.errorBody() != null ? response.errorBody().string() : "";
                Log.e(TAG, "getPasskeyAssertionOptions: failed, code=" + response.code() + " body=" + passkeyOptErr);
                return new Result.Error(new Exception("Passkey options failed: " + response.code()));
            }
        } catch (IOException e) {
            Log.e(TAG, "getPasskeyAssertionOptions: network error", e);
            return new Result.Error(e);
        }
    }

    public Result<String> signInWithPasskey(String credentialJson) {
        Log.d(TAG, "signInWithPasskey");
        try {
            PasskeyAssertionRequest request =
                    new Gson().fromJson(credentialJson, PasskeyAssertionRequest.class);
            if (request == null
                    || request.getRawId() == null
                    || request.getResponse() == null
                    || request.getResponse().getClientDataJSON() == null
                    || request.getResponse().getAuthenticatorData() == null
                    || request.getResponse().getSignature() == null) {
                Log.e(TAG, "signInWithPasskey: invalid credential payload");
                return new Result.Error(new Exception("Passkey credential payload is invalid"));
            }

            String url = resolveUrl(BuildConfig.PASSKEY_VERIFY_PATH);
            Response<AuthTokens> response =
                    authApi.verifyPasskeyAssertion(url, request)
                            .execute();
            if (response.isSuccessful() && response.body() != null) {
                Log.d(TAG, "signInWithPasskey: success");
                tokenManager.saveTokens(response.body());
                return new Result.Success<>("passkey");
            } else {
                String passkeyErr = response.errorBody() != null ? response.errorBody().string() : "";
                Log.e(TAG, "signInWithPasskey: failed, code=" + response.code() + " body=" + passkeyErr);
                return new Result.Error(new Exception("Passkey login failed: " + response.code()));
            }
        } catch (IOException e) {
            Log.e(TAG, "signInWithPasskey: network error", e);
            return new Result.Error(e);
        }
    }

    public void logout() {
        Log.d(TAG, "logout: clearing tokens");
        tokenManager.clearTokens();
    }

    private String resolveUrl(String path) {
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        String base = BuildConfig.BACKEND_BASE_URL;
        boolean baseEndsWithSlash = base.endsWith("/");
        boolean pathStartsWithSlash = path.startsWith("/");
        if (baseEndsWithSlash && pathStartsWithSlash) {
            return base + path.substring(1);
        }
        if (!baseEndsWithSlash && !pathStartsWithSlash) {
            return base + "/" + path;
        }
        return base + path;
    }
}
