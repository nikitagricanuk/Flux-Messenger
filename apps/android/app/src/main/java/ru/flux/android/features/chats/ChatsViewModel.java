package ru.flux.android.features.chats;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;
import ru.flux.android.core.data.Chat;
import ru.flux.android.core.data.Contact;
import ru.flux.android.core.network.AddFavoriteRequest;
import ru.flux.android.core.network.ApiClient;
import ru.flux.android.core.network.ChatResponse;
import ru.flux.android.core.network.CreateChatRequest;
import ru.flux.android.core.network.FavoriteResponse;
import ru.flux.android.core.network.UserResponse;

public class ChatsViewModel extends AndroidViewModel {
    private static final String TAG = "ChatsViewModel";
    private final MutableLiveData<List<Chat>> chats = new MutableLiveData<>();
    private final MutableLiveData<List<Chat>> favorites = new MutableLiveData<>();
    private final MutableLiveData<List<Contact>> contacts = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> chatCreated = new MutableLiveData<>();
    private final MutableLiveData<String> currentUserId = new MutableLiveData<>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ChatsViewModel(@NonNull Application application) {
        super(application);
        loadCurrentUser();
    }

    public LiveData<List<Chat>> getChats() { return chats; }
    public LiveData<List<Chat>> getFavorites() { return favorites; }
    public LiveData<List<Contact>> getContacts() { return contacts; }
    public LiveData<String> getError() { return error; }
    public LiveData<Boolean> getChatCreated() { return chatCreated; }
    public LiveData<String> getCurrentUserId() { return currentUserId; }
    public void clearError() { error.setValue(null); }

    private void loadCurrentUser() {
        executor.execute(() -> {
            try {
                Response<UserResponse> response = ApiClient.api(getApplication()).getMe().execute();
                if (response.isSuccessful() && response.body() != null) {
                    currentUserId.postValue(response.body().id);
                }
            } catch (GeneralSecurityException | IOException e) {
                Log.e(TAG, "loadCurrentUser: exception", e);
            }
        });
    }

    public void createChat(String name, String type, String[] memberIds) {
        Log.d(TAG, "createChat: name=" + name + ", type=" + type);
        executor.execute(() -> {
            try {
                Response<ChatResponse> response = ApiClient.api(getApplication()).createChat(
                        new CreateChatRequest(type, memberIds)).execute();
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "createChat: success");
                    fetchChats();
                    chatCreated.postValue(true);
                } else {
                    Log.e(TAG, "createChat: failed, code=" + response.code() + ", body=" + response.body());
                    error.postValue("Не удалось создать чат");
                }
            } catch (GeneralSecurityException | IOException e) {
                Log.e(TAG, "createChat: exception", e);
                error.postValue(e.getMessage());
            }
        });
    }

    public void loadChats() {
        if (chats.getValue() != null) return;
        fetchChats();
    }

    public void loadFavorites() {
        if (favorites.getValue() != null) return;
        fetchFavorites();
    }

    private void fetchFavorites() {
        executor.execute(() -> {
            try {
                Response<List<FavoriteResponse>> response = ApiClient.api(getApplication()).getFavorites().execute();
                if (response.isSuccessful() && response.body() != null) {
                    List<Chat> mapped = new ArrayList<>();
                    for (FavoriteResponse fr : response.body())
                        mapped.add(new Chat(fr.id, fr.name, null, fr.profilePicture, null, null));
                    favorites.postValue(mapped);
                } else {
                    error.postValue("Не удалось загрузить избранное");
                }
            } catch (GeneralSecurityException | IOException e) {
                Log.e(TAG, "fetchFavorites: exception", e);
                error.postValue(e.getMessage());
            }
        });
    }

    public void addFavorite(Chat chat) {
        String myId = currentUserId.getValue();
        String targetId = null;
        for (String memberId : chat.memberIds) {
            if (!memberId.equals(myId)) { targetId = memberId; break; }
        }
        if (targetId == null) {
            error.postValue("Не удалось определить пользователя для добавления в избранное");
            return;
        }
        Log.d(TAG, "addFavorite: targetUserId=" + targetId);
        String finalTargetId = targetId;
        executor.execute(() -> {
            try {
                Response<FavoriteResponse> response = ApiClient.api(getApplication())
                        .addFavorite(new AddFavoriteRequest(finalTargetId)).execute();
                if (response.isSuccessful()) {
                    fetchFavorites();
                } else {
                    Log.e(TAG, "addFavorite: failed, code=" + response.code());
                    error.postValue("Не удалось добавить в избранное");
                }
            } catch (GeneralSecurityException | IOException e) {
                Log.e(TAG, "addFavorite: exception", e);
                error.postValue(e.getMessage());
            }
        });
    }

    private void fetchChats() {
        executor.execute(() -> {
            try {
                Response<List<ChatResponse>> response = ApiClient.api(getApplication()).getChats().execute();
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "loadChats: success, count=" + response.body().size());
                    List<Chat> mapped = new ArrayList<>();
                    for (ChatResponse cr : response.body()) mapped.add(toChat(cr));
                    chats.postValue(mapped);
                } else {
                    Log.e(TAG, "loadChats: failed, code=" + response.code());
                    error.postValue("Не удалось загрузить чаты");
                }
            } catch (GeneralSecurityException | IOException e) {
                Log.e(TAG, "loadChats: exception", e);
                error.postValue(e.getMessage());
            }
        });
    }

    public void deleteChat(Chat chat) {
        Log.d(TAG, "deleteChat: id=" + chat.id);
        executor.execute(() -> {
            try {
                Response<Void> response = ApiClient.api(getApplication()).deleteChat(chat.id).execute();
                if (response.isSuccessful()) {
                    Log.d(TAG, "deleteChat: success");
                    List<Chat> current = chats.getValue();
                    if (current != null) {
                        List<Chat> updated = new ArrayList<>(current);
                        updated.remove(chat);
                        chats.postValue(updated);
                    }
                } else {
                    Log.e(TAG, "deleteChat: failed, code=" + response.code());
                    error.postValue("Не удалось удалить чат");
                }
            } catch (GeneralSecurityException | IOException e) {
                Log.e(TAG, "deleteChat: exception", e);
                error.postValue(e.getMessage());
            }
        });
    }

    private Chat toChat(ChatResponse cr) {
        String type = "DIRECT".equals(cr.type) ? "dm" : "group";
        String time = "";
        if (cr.lastMessageAt != null) {
            int t = cr.lastMessageAt.indexOf('T');
            if (t >= 0 && cr.lastMessageAt.length() >= t + 6)
                time = cr.lastMessageAt.substring(t + 1, t + 6);
        }
        Chat chat = new Chat(cr.id, cr.name, cr.lastMessage, cr.profilePicture, time, type);
        if (cr.memberIds != null) chat.memberIds = cr.memberIds;
        return chat;
    }
}
