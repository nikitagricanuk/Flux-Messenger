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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.flux.android.core.data.Chat;
import ru.flux.android.core.data.Group;
import ru.flux.android.core.data.Link;
import ru.flux.android.core.network.ApiClient;
import ru.flux.android.core.network.MessageResponse;
import ru.flux.android.core.network.UserResponse;

public class ProfileViewModel extends AndroidViewModel {

    private static final String TAG = "ProfileViewModel";
    private static final Pattern URL_PATTERN = Pattern.compile(
            "https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+");

    private final MutableLiveData<UserResponse> user = new MutableLiveData<>();
    private final MutableLiveData<List<String>> mediaUrls = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Link>> links = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Group>> groups = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<UserResponse>> members = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<UserResponse>> getMembers() { return members; }

    public ProfileViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<UserResponse> getUser() { return user; }
    public LiveData<List<String>> getMediaUrls() { return mediaUrls; }
    public LiveData<List<Link>> getLinks() { return links; }
    public LiveData<List<Group>> getGroups() { return groups; }

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

    public void loadLinks(UUID chatId) {
        try {
            ApiClient.api(getApplication()).getMessages(chatId, 0, 200)
                    .enqueue(new Callback<List<MessageResponse>>() {
                        @Override
                        public void onResponse(@NonNull Call<List<MessageResponse>> call,
                                               @NonNull Response<List<MessageResponse>> response) {
                            if (!response.isSuccessful() || response.body() == null) return;
                            List<Link> result = new ArrayList<>();
                            for (MessageResponse msg : response.body()) {
                                if (msg.text == null) continue;
                                Matcher matcher = URL_PATTERN.matcher(msg.text);
                                while (matcher.find()) {
                                    String url = matcher.group();
                                    result.add(new Link(url, url, null));
                                }
                            }
                            links.postValue(result);
                        }
                        @Override
                        public void onFailure(@NonNull Call<List<MessageResponse>> call,
                                              @NonNull Throwable t) {
                            Log.e(TAG, "loadLinks error", t);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "ApiClient error", e);
        }
    }

    public void loadMembers(List<String> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) return;
        try {
            List<UUID> ids = memberIds.stream().map(UUID::fromString).collect(Collectors.toList());
            ApiClient.api(getApplication()).getUsersByIds(ids)
                    .enqueue(new Callback<List<UserResponse>>() {
                        @Override
                        public void onResponse(@NonNull Call<List<UserResponse>> call,
                                               @NonNull Response<List<UserResponse>> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                members.postValue(response.body());
                            } else {
                                Log.e(TAG, "getUsersByIds failed: " + response.code());
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<List<UserResponse>> call,
                                              @NonNull Throwable t) {
                            Log.e(TAG, "loadMembers error", t);
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "ApiClient error", e);
        }
    }

    public void loadGroups(List<Chat> allChats) {
        List<Group> result = new ArrayList<>();
        for (Chat chat : allChats) {
            if ("group".equals(chat.type)) {
                result.add(new Group(chat.id, chat.name, chat.avatarUrl, chat.memberIds));
            }
        }
        groups.setValue(result);
    }
}