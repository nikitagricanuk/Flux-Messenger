package ru.flux.android.data;

import java.io.IOException;

import com.google.gson.Gson;

import retrofit2.Response;
import ru.flux.android.BuildConfig;

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
                tokenManager.saveUserId(response.body().getUserId());
                return new Result.Success<>(phone);
            } else {
                return new Result.Error(new Exception("Login failed: " + response.code()));
            }
        } catch (IOException e) {
            return new Result.Error(e);
        }
    }

    public Result<String> signUp(String firstName, String lastName, String username,
                                  String phone, String password) {
        try {
            Response<AuthTokens> response =
                    authApi.signUp(new SignUpRequest(firstName, lastName, username, phone, password))
                            .execute();
            if (response.isSuccessful() && response.body() != null) {
                tokenManager.saveTokens(response.body());
                tokenManager.saveUserId(response.body().getUserId());
                return new Result.Success<>(phone);
            } else {
                return new Result.Error(new Exception("Sign up failed: " + response.code()));
            }
        } catch (IOException e) {
            return new Result.Error(e);
        }
    }

    public Result<PasskeyAssertionOptionsResponse> getPasskeyAssertionOptions() {
        try {
            String url = resolveUrl(BuildConfig.PASSKEY_OPTIONS_PATH);
            Response<PasskeyAssertionOptionsResponse> response =
                    authApi.getPasskeyAssertionOptions(url).execute();
            if (response.isSuccessful() && response.body() != null
                    && response.body().getChallenge() != null
                    && !response.body().getChallenge().isEmpty()) {
                return new Result.Success<>(response.body());
            } else {
                return new Result.Error(new Exception("Passkey options failed: " + response.code()));
            }
        } catch (IOException e) {
            return new Result.Error(e);
        }
    }

    public Result<String> signInWithPasskey(String credentialJson) {
        try {
            PasskeyAssertionRequest request =
                    new Gson().fromJson(credentialJson, PasskeyAssertionRequest.class);
            if (request == null
                    || request.getRawId() == null
                    || request.getResponse() == null
                    || request.getResponse().getClientDataJSON() == null
                    || request.getResponse().getAuthenticatorData() == null
                    || request.getResponse().getSignature() == null) {
                return new Result.Error(new Exception("Passkey credential payload is invalid"));
            }

            String url = resolveUrl(BuildConfig.PASSKEY_VERIFY_PATH);
            Response<AuthTokens> response =
                    authApi.verifyPasskeyAssertion(url, request)
                            .execute();
            if (response.isSuccessful() && response.body() != null) {
                tokenManager.saveTokens(response.body());
                tokenManager.saveUserId(response.body().getUserId());
                return new Result.Success<>("passkey");
            } else {
                return new Result.Error(new Exception("Passkey login failed: " + response.code()));
            }
        } catch (IOException e) {
            return new Result.Error(e);
        }
    }

    public Result<String> exchangeOAuthCode(String provider, String code, String redirectUri,
                                            String state) {
        try {
            String url = resolveUrl(BuildConfig.OAUTH_CODE_EXCHANGE_PATH);
            OAuthCodeExchangeRequest request =
                    new OAuthCodeExchangeRequest(provider, code, redirectUri, state);
            Response<AuthTokens> response = authApi.exchangeOAuthCode(url, request).execute();
            if (response.isSuccessful() && response.body() != null) {
                tokenManager.saveTokens(response.body());
                tokenManager.saveUserId(response.body().getUserId());
                return new Result.Success<>(provider);
            } else {
                return new Result.Error(new Exception("OAuth exchange failed: " + response.code()));
            }
        } catch (IOException e) {
            return new Result.Error(e);
        }
    }

    public void saveTokens(String accessToken, String refreshToken) {
        tokenManager.saveTokens(accessToken, refreshToken);
    }

    public void logout() {
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
