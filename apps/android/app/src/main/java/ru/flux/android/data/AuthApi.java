package ru.flux.android.data;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("api/auth/sign-in")
    Call<AuthTokens> login(@Body LoginRequest request);

    @POST("api/auth/sign-up")
    Call<AuthTokens> signUp(@Body SignUpRequest request);

    @POST("api/auth/refresh")
    Call<AuthTokens> refreshToken(@Body RefreshTokenRequest request);
}
