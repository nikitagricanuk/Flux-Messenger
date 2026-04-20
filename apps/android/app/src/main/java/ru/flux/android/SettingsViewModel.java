package ru.flux.android;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;
import ru.flux.android.data.TokenManager;

public class SettingsViewModel extends AndroidViewModel {

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
        executor.execute(() -> {
            try {
                ApiService api = buildApi();
                Response<UserResponse> response = api.getMe().execute();
                if (response.isSuccessful() && response.body() != null) {
                    user.postValue(response.body());
                } else {
                    error.postValue("Не удалось загрузить профиль");
                }
            } catch (GeneralSecurityException | IOException e) {
                error.postValue(e.getMessage());
            }
        });
    }

    public void saveUser(UpdateUserRequest request) {
        executor.execute(() -> {
            try {
                ApiService api = buildApi();
                Response<UserResponse> response = api.updateMe(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    user.postValue(response.body());
                } else {
                    error.postValue("Не удалось сохранить профиль");
                }
            } catch (GeneralSecurityException | IOException e) {
                error.postValue(e.getMessage());
            }
        });
    }

    public void deleteAccount(Runnable onSuccess) {
        executor.execute(() -> {
            try {
                TokenManager tm = new TokenManager(getApplication());
                ApiService api = ApiClient.getInstance(tm).create(ApiService.class);
                Response<Void> response = api.deleteMe().execute();
                if (response.isSuccessful()) {
                    tm.clearTokens();
                    onSuccess.run();
                } else {
                    error.postValue("Не удалось удалить аккаунт");
                }
            } catch (GeneralSecurityException | IOException e) {
                error.postValue(e.getMessage());
            }
        });
    }

    private ApiService buildApi() throws GeneralSecurityException, IOException {
        TokenManager tm = new TokenManager(getApplication());
        return ApiClient.getInstance(tm).create(ApiService.class);
    }
}