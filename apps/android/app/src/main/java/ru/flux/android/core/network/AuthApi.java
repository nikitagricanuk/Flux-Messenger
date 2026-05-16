package ru.flux.android.core.network;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
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
    Call<ResponseBody> getPasskeyOptions(@Url String url, @Body PasskeyRegistrationRequest request);

    @POST
    Call<AuthTokens> completePasskey(
            @Url String url,
            @Header("X-Challenge-Nonce") String nonce,
            @Body RequestBody credential
    );

    @POST
    Call<ResponseBody> startPasskeyAuthentication(@Url String url);

    @POST
    Call<AuthTokens> finishPasskeyAuthentication(
            @Url String url,
            @Header("X-Challenge-Nonce") String nonce,
            @Body RequestBody credential
    );
}