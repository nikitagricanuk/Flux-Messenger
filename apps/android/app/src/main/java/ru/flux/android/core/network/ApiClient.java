package ru.flux.android.core.network;

import android.content.Context;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.TimeUnit;

import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.flux.android.BuildConfig;
import ru.flux.android.core.auth.AuthInterceptor;
import ru.flux.android.core.auth.TokenManager;

public class ApiClient {

    private static Retrofit retrofit;

    public static ApiService api(Context context) throws GeneralSecurityException, IOException {
        return getInstance(new TokenManager(context)).create(ApiService.class);
    }

    public static Retrofit getInstance(TokenManager tokenManager) {
        if (retrofit == null) {
            AuthApi bareAuthApi = new Retrofit.Builder()
                    .baseUrl(BuildConfig.BACKEND_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(AuthApi.class);

            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .connectionPool(new ConnectionPool(5, 5, TimeUnit.MINUTES))
                    .addInterceptor(new AuthInterceptor(tokenManager, bareAuthApi))
                    .build();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BuildConfig.BACKEND_BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }
}
