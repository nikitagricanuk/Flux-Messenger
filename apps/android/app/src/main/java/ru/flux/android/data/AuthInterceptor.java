package ru.flux.android.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private final TokenManager tokenManager;
    private final AuthApi      authApi;

    public AuthInterceptor(TokenManager tokenManager, AuthApi authApi) {
        this.tokenManager = tokenManager;
        this.authApi      = authApi;
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        String token = getValidToken();

        Request request = chain.request();
        if (token != null) {
            request = request.newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
        }

        Response response = chain.proceed(request);

        if (response.code() == 401) {
            response.close();
            String refreshed = refreshTokens();
            if (refreshed == null) return chain.proceed(chain.request());
            Request retry = chain.request().newBuilder()
                    .header("Authorization", "Bearer " + refreshed)
                    .build();
            return chain.proceed(retry);
        }

        return response;
    }

    @Nullable
    private String getValidToken() {
        if (tokenManager.isAccessTokenExpired()) return refreshTokens();
        return tokenManager.getAccessToken();
    }

    @Nullable
    private synchronized String refreshTokens() {
        String refreshToken = tokenManager.getRefreshToken();
        if (refreshToken == null) return null;

        try {
            retrofit2.Response<AuthTokens> response = authApi.refreshToken(new RefreshTokenRequest(refreshToken)).execute();
            if (response.isSuccessful() && response.body() != null) {
                tokenManager.saveTokens(response.body());
                return response.body().getAccessToken();
            } else {
                tokenManager.clearTokens();
                return null;
            }
        } catch (IOException e) {
            return null;
        }
    }
}
