package ru.flux.android.features.login;

import android.util.Log;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import ru.flux.android.BuildConfig;
import ru.flux.android.core.Result;
import ru.flux.android.core.auth.AuthTokens;
import ru.flux.android.core.auth.TokenManager;
import ru.flux.android.core.network.AuthApi;
import ru.flux.android.core.network.LoginRequest;
import ru.flux.android.core.network.PasskeyAssertionOptions;
import ru.flux.android.core.network.PasskeyRegistrationOptions;
import ru.flux.android.core.network.PasskeyRegistrationRequest;
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

    public Result<PasskeyRegistrationOptions> getPasskeyRegistrationOptions(String phone) {
        Log.d(TAG, "getPasskeyRegistrationOptions: phone=" + phone);
        try {
            String url = resolveUrl(BuildConfig.PASSKEY_OPTIONS_PATH);
            Response<ResponseBody> response =
                    authApi.getPasskeyOptions(url, new PasskeyRegistrationRequest(phone)).execute();
            if (response.isSuccessful() && response.body() != null) {
                String optionsJson = response.body().string();
                String nonce = response.headers().get("X-Challenge-Nonce");
                if (nonce == null || nonce.isEmpty()) {
                    Log.e(TAG, "getPasskeyRegistrationOptions: missing X-Challenge-Nonce header");
                    return new Result.Error(new Exception("Server did not return challenge nonce"));
                }
                Log.d(TAG, "getPasskeyRegistrationOptions: success");
                return new Result.Success<>(new PasskeyRegistrationOptions(optionsJson, nonce));
            } else {
                String err = response.errorBody() != null ? response.errorBody().string() : "";
                Log.e(TAG, "getPasskeyRegistrationOptions: failed, code=" + response.code() + " body=" + err);
                return new Result.Error(new Exception("Passkey options failed: " + response.code()));
            }
        } catch (IOException e) {
            Log.e(TAG, "getPasskeyRegistrationOptions: network error", e);
            return new Result.Error(e);
        }
    }

    public Result<String> completePasskeyRegistration(String nonce, String credentialJson) {
        Log.d(TAG, "completePasskeyRegistration");
        try {
            String url = resolveUrl(BuildConfig.PASSKEY_COMPLETE_PATH);
            RequestBody body = RequestBody.create(credentialJson, MediaType.get("application/json"));
            Response<AuthTokens> response =
                    authApi.completePasskey(url, nonce, body).execute();
            if (response.isSuccessful() && response.body() != null) {
                Log.d(TAG, "completePasskeyRegistration: success");
                tokenManager.saveTokens(response.body());
                return new Result.Success<>("passkey");
            } else {
                String err = response.errorBody() != null ? response.errorBody().string() : "";
                Log.e(TAG, "completePasskeyRegistration: failed, code=" + response.code() + " body=" + err);
                return new Result.Error(new Exception("Passkey failed: " + response.code()));
            }
        } catch (IOException e) {
            Log.e(TAG, "completePasskeyRegistration: network error", e);
            return new Result.Error(e);
        }
    }

    public Result<PasskeyAssertionOptions> startPasskeyAuthentication() {
        Log.d(TAG, "startPasskeyAuthentication");
        try {
            String url = resolveUrl("api/auth/passkey/authenticate/start");
            Response<ResponseBody> response = authApi.startPasskeyAuthentication(url).execute();
            if (response.isSuccessful() && response.body() != null) {
                String optionsJson = response.body().string();
                String nonce = response.headers().get("X-Challenge-Nonce");
                if (nonce == null || nonce.isEmpty()) {
                    Log.e(TAG, "startPasskeyAuthentication: missing X-Challenge-Nonce header");
                    return new Result.Error(new Exception("Server did not return challenge nonce"));
                }
                Log.d(TAG, "startPasskeyAuthentication: success");
                return new Result.Success<>(new PasskeyAssertionOptions(optionsJson, nonce));
            } else {
                String err = response.errorBody() != null ? response.errorBody().string() : "";
                Log.e(TAG, "startPasskeyAuthentication: failed code=" + response.code() + " " + err);
                return new Result.Error(new Exception("Authentication start failed: " + response.code()));
            }
        } catch (IOException e) {
            Log.e(TAG, "startPasskeyAuthentication: network error", e);
            return new Result.Error(e);
        }
    }

    public Result<String> finishPasskeyAuthentication(String nonce, String credentialJson) {
        Log.d(TAG, "finishPasskeyAuthentication");
        try {
            String url = resolveUrl("api/auth/passkey/authenticate/finish");
            RequestBody body = RequestBody.create(credentialJson, MediaType.get("application/json"));
            Response<AuthTokens> response = authApi.finishPasskeyAuthentication(url, nonce, body).execute();
            if (response.isSuccessful() && response.body() != null) {
                Log.d(TAG, "finishPasskeyAuthentication: success");
                tokenManager.saveTokens(response.body());
                return new Result.Success<>("passkey");
            } else {
                String err = response.errorBody() != null ? response.errorBody().string() : "";
                Log.e(TAG, "finishPasskeyAuthentication: failed code=" + response.code() + " " + err);
                return new Result.Error(new Exception("Authentication failed: " + response.code()));
            }
        } catch (IOException e) {
            Log.e(TAG, "finishPasskeyAuthentication: network error", e);
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
