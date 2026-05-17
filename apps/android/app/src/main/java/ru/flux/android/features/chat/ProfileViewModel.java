package ru.flux.android.features.chat;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.flux.android.core.network.ApiClient;
import ru.flux.android.core.network.UserResponse;

public class ProfileViewModel extends AndroidViewModel {

    private static final String TAG = "ProfileViewModel";
    private final MutableLiveData<UserResponse> user = new MutableLiveData<>();

    public ProfileViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<UserResponse> getUser() { return user; }

    public void loadUser(UUID userId) {
        try {
            ApiClient.api(getApplication()).getUserById(userId).enqueue(new Callback<UserResponse>() {
                @Override
                public void onResponse(@NonNull Call<UserResponse> call,
                                       @NonNull Response<UserResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        user.postValue(response.body());
                    } else {
                        Log.e(TAG, "getUserById failed: " + response.code());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                    Log.e(TAG, "getUserById error", t);
                }
            });
        } catch (Exception e) {
            Log.e(TAG, "ApiClient error", e);
        }
    }
}