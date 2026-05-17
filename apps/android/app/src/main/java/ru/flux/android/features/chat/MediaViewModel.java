package ru.flux.android.features.chat;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.flux.android.core.network.ApiClient;
import ru.flux.android.core.network.MessageResponse;

public class MediaViewModel extends AndroidViewModel {

    private static final String TAG = "MediaViewModel";
    private final MutableLiveData<List<String>> mediaUrls = new MutableLiveData<>(new ArrayList<>());

    public MediaViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<String>> getMediaUrls() { return mediaUrls; }

    public void loadMedia(UUID chatId) {
        try {
            ApiClient.api(getApplication()).getMediaMessages(chatId)
                    .enqueue(new Callback<List<MessageResponse>>() {
                        @Override
                        public void onResponse(@NonNull Call<List<MessageResponse>> call,
                                               @NonNull Response<List<MessageResponse>> response) {
                            if (!response.isSuccessful() || response.body() == null) return;
                            List<String> urls = new ArrayList<>();
                            for (MessageResponse msg : response.body()) {
                                if (msg.mediaUrl != null) urls.add(msg.mediaUrl);
                            }
                            mediaUrls.postValue(urls);
                        }

                        @Override
                        public void onFailure(@NonNull Call<List<MessageResponse>> call,
                                              @NonNull Throwable t) {
                            Log.e(TAG, "loadMedia error", t);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "ApiClient error", e);
        }
    }
}