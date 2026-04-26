package ru.flux.android.features.chats;

import android.app.Application;

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
import ru.flux.android.core.network.ApiClient;
import ru.flux.android.core.network.ChatResponse;

public class ChatsViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Chat>> chats = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public ChatsViewModel(@NonNull Application application) {
        super(application);
    }

    public LiveData<List<Chat>> getChats() { return chats; }
    public LiveData<String> getError() { return error; }

    public void loadChats() {
        if (chats.getValue() != null) return;
        executor.execute(() -> {
            try {
                Response<List<ChatResponse>> response = ApiClient.api(getApplication()).getChats().execute();
                if (response.isSuccessful() && response.body() != null) {
                    List<Chat> mapped = new ArrayList<>();
                    for (ChatResponse cr : response.body()) mapped.add(toChat(cr));
                    chats.postValue(mapped);
                } else {
                    error.postValue("Не удалось загрузить чаты");
                }
            } catch (GeneralSecurityException | IOException e) {
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
        return new Chat(cr.id, cr.name, cr.lastMessage, cr.profilePicture, time, type);
    }
}
