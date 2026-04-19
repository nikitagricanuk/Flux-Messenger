package ru.flux.android.data;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface AuthApi {

    @POST("api/auth/sign-in")
    Call<AuthTokens> login(@Body LoginRequest request);

    // TODO: add @POST("api/auth/refresh") once backend implements the endpoint
    @POST("api/auth/refresh")
    Call<AuthTokens> refreshToken(@Body String refreshToken);
}
