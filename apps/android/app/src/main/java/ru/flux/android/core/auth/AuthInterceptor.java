package ru.flux.android.core.auth;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import ru.flux.android.core.network.AuthApi;
import ru.flux.android.core.network.RefreshTokenRequest;

public class AuthInterceptor implements Interceptor {

    private static final String TAG = "AuthInterceptor";

    private final TokenManager tokenManager;
    private final AuthApi      authApi;

    public AuthInterceptor(TokenManager tokenManager, AuthApi authApi) {
        this.tokenManager = tokenManager;
        this.authApi      = authApi;
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        if (isPublicEndpoint(original.url().encodedPath())) {
            Response response = chain.proceed(original);
            Log.v(TAG, "intercept: " + original.method() + " " + original.url() + " -> " + response.code() + " (public)");
            return response;
        }

        String token = getValidToken();

        Request request = original;
        if (token != null) {
            Log.d(TAG, "intercept: attaching token ..." + token.substring(Math.max(0, token.length() - 8)) + " for " + request.method() + " " + request.url());
            request = request.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
        } else {
            Log.w(TAG, "intercept: no valid token for " + request.url());
        }

        Response response = chain.proceed(request);
        Log.v(TAG, "intercept: " + request.method() + " " + request.url() + " -> " + response.code());

        if (response.code() == 401 || response.code() == 403) {
            Log.w(TAG, "intercept: " + response.code() + " received, attempting token refresh");
            response.close();
            String refreshed = refreshTokens();
            if (refreshed == null) {
                Log.e(TAG, "intercept: token refresh failed, retrying without auth");
                return chain.proceed(original);
            }
            Log.d(TAG, "intercept: token refreshed, retrying request");
            Request retry = original.newBuilder()
                    .header("Authorization", "Bearer " + refreshed)
                    .build();
            return chain.proceed(retry);
        }

        return response;
    }

    private static boolean isPublicEndpoint(String path) {
        return path.startsWith("/api/auth/sign-in")
                || path.startsWith("/api/auth/sign-up")
                || path.startsWith("/api/auth/refresh")
                || path.startsWith("/api/auth/passkey/");
    }

    @Nullable
    private String getValidToken() {
        if (tokenManager.isAccessTokenExpired()) {
            String refreshed = refreshTokens();
            if (refreshed != null) return refreshed;
        }
        return tokenManager.getAccessToken();
    }

    @Nullable
    private synchronized String refreshTokens() {
        String refreshToken = tokenManager.getRefreshToken();
        if (refreshToken == null) {
            Log.w(TAG, "refreshTokens: no refresh token stored");
            return null;
        }

        try {
            Log.d(TAG, "refreshTokens: requesting new tokens");
            retrofit2.Response<AuthTokens> response = authApi.refreshToken(new RefreshTokenRequest(refreshToken)).execute();
            if (response.isSuccessful() && response.body() != null) {
                Log.d(TAG, "refreshTokens: success");
                tokenManager.saveTokens(response.body());
                return response.body().getAccessToken();
            } else {
                Log.e(TAG, "refreshTokens: failed, code=" + response.code() + " — clearing tokens");
                tokenManager.clearTokens();
                return null;
            }
        } catch (IOException e) {
            Log.e(TAG, "refreshTokens: network error", e);
            return null;
        }
    }
}
