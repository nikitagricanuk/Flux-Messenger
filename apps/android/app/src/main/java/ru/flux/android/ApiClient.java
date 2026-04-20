package ru.flux.android;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import ru.flux.android.data.AuthApi;
import ru.flux.android.data.AuthInterceptor;
import ru.flux.android.data.TokenManager;

public class ApiClient {

    private static Retrofit retrofit;

    public static Retrofit getInstance(TokenManager tokenManager) {
        if (retrofit == null) {
            AuthApi bareAuthApi = new Retrofit.Builder()
                    .baseUrl(BuildConfig.BACKEND_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(AuthApi.class);

            OkHttpClient client = new OkHttpClient.Builder()
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
