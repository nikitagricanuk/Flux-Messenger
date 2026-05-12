package ru.flux.android.features.settings;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;
import ru.flux.android.core.network.UpdateUserRequest;
import ru.flux.android.core.network.UserResponse;
import ru.flux.android.core.auth.TokenManager;
import ru.flux.android.core.network.ApiClient;
import ru.flux.android.core.network.ApiService;

public class SettingsViewModel extends AndroidViewModel {

    private static final String TAG = "SettingsViewModel";

    private final MutableLiveData<UserResponse> user = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public SettingsViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<UserResponse> getUser() { return user; }
    public LiveData<String> getError() { return error; }

    public void loadUser() {
        if (user.getValue() != null) return;
        Log.d(TAG, "loadUser");
        executor.execute(() -> {
            try {
                ApiService api = buildApi();
                Response<UserResponse> response = api.getMe().execute();
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "loadUser: success, id=" + response.body().id);
                    user.postValue(response.body());
                } else {
                    Log.e(TAG, "loadUser: failed, code=" + response.code());
                    error.postValue("Не удалось загрузить профиль");
                }
            } catch (GeneralSecurityException | IOException e) {
                Log.e(TAG, "loadUser: exception", e);
                error.postValue(e.getMessage());
            }
        });
    }

    public void saveUser(UpdateUserRequest request) {
        Log.d(TAG, "saveUser");
        executor.execute(() -> {
            try {
                ApiService api = buildApi();
                Response<UserResponse> response = api.updateMe(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "saveUser: success");
                    user.postValue(response.body());
                } else {
                    Log.e(TAG, "saveUser: failed, code=" + response.code());
                    error.postValue("Не удалось сохранить профиль");
                }
            } catch (GeneralSecurityException | IOException e) {
                Log.e(TAG, "saveUser: exception", e);
                error.postValue(e.getMessage());
            }
        });
    }

    public void deleteAccount(Runnable onSuccess) {
        Log.d(TAG, "deleteAccount");
        executor.execute(() -> {
            try {
                TokenManager tm = new TokenManager(getApplication());
                Response<Void> response = ApiClient.api(getApplication()).deleteMe().execute();
                if (response.isSuccessful()) {
                    Log.d(TAG, "deleteAccount: success, tokens cleared");
                    tm.clearTokens();
                    onSuccess.run();
                } else {
                    Log.e(TAG, "deleteAccount: failed, code=" + response.code());
                    error.postValue("Не удалось удалить аккаунт");
                }
            } catch (GeneralSecurityException | IOException e) {
                Log.e(TAG, "deleteAccount: exception", e);
                error.postValue(e.getMessage());
            }
        });
    }

    private ApiService buildApi() throws GeneralSecurityException, IOException {
        return ApiClient.api(getApplication());
    }
}
