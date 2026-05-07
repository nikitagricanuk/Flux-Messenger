package ru.flux.android.core.network;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Url;

import ru.flux.android.core.auth.AuthTokens;

public interface AuthApi {

    @POST("api/auth/sign-in")
    Call<AuthTokens> login(@Body LoginRequest request);

    @POST("api/auth/sign-up")
    Call<AuthTokens> signUp(@Body SignUpRequest request);

    @POST("api/auth/refresh")
    Call<AuthTokens> refreshToken(@Body RefreshTokenRequest request);

    @POST
    Call<PasskeyAssertionOptionsResponse> getPasskeyAssertionOptions(@Url String url);

    @POST
    Call<AuthTokens> verifyPasskeyAssertion(@Url String url, @Body PasskeyAssertionRequest request);

    @POST
    Call<AuthTokens> exchangeOAuthCode(@Url String url, @Body OAuthCodeExchangeRequest request);
}
