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
import ru.flux.android.core.auth.TokenManager;
import ru.flux.android.core.data.Message;
import ru.flux.android.core.network.ApiClient;
import ru.flux.android.core.network.ApiService;
import ru.flux.android.core.network.MessageResponse;
import ru.flux.android.core.network.SendMessageRequest;
import ru.flux.android.core.network.UserResponse;
import ru.flux.android.core.network.WebSocketManager;

public class ChatViewModel extends AndroidViewModel {

    private static final String TAG = "ChatViewModel";
    private final MutableLiveData<List<Message>> messagesLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> editingMessageTextLiveData = new MutableLiveData<>();
    private final MutableLiveData<Boolean> clearInput = new MutableLiveData<>();
    public LiveData<Boolean> getClearInput() { return clearInput; }
    private ApiService messagingApi;
    private TokenManager tokenManager;
    private WebSocketManager webSocketManager;
    private UUID chatId;
    private String currentUserId = null;
    private UUID editingMessageId = null;


    public ChatViewModel(@NonNull Application application) {
        super(application);
        try{
            tokenManager = new TokenManager(application.getApplicationContext());
            messagingApi = ApiClient.api(application.getApplicationContext());
        }
        catch (Exception e){
            Log.e(TAG, "Init failed", e);
        }
    }

    public void initChat(UUID chatId) {
        if (this.chatId != null) return;
        this.chatId = chatId;
        if (this.chatId != null) {
            loadCurrentUser();
        }
    }

    public LiveData<List<Message>> getMessages() {
        return messagesLiveData;
    }
    public LiveData<String> getEditingMessageText() {
        return editingMessageTextLiveData;
    }

    public void onSendClicked(String text) {
        if (text.isEmpty() || chatId == null) return;

        if (editingMessageId != null) {
            editMessage(text);
        } else {
            sendMessage(text);
        }
    }

    public void setEditingMessage(Message message) {
        editingMessageId = UUID.fromString(message.id);
        editingMessageTextLiveData.setValue(message.text);
    }

    public void cancelEditing() {
        editingMessageId = null;
        editingMessageTextLiveData.setValue("");
    }

    public void deleteMessage(Message message) {
        messagingApi.deleteMessage(UUID.fromString(message.id)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    List<Message> current = new ArrayList<>(messagesLiveData.getValue());
                    current.remove(message);
                    messagesLiveData.setValue(current);
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "Delete error", t);
            }
        });
    }

    private void loadCurrentUser() {
        messagingApi.getMe().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call, @NonNull Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUserId = response.body().id.toString();
                    loadHistory();
                    connectWebSocket();
                }
            }
            @Override
            public void onFailure(@NonNull Call<UserResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "getMe error", t);
            }
        });
    }

    private void loadHistory() {
        messagingApi.getMessages(chatId, 0, 50).enqueue(new Callback<List<MessageResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<MessageResponse>> call, @NonNull Response<List<MessageResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Message> newMessages = new ArrayList<>();
                    for (MessageResponse msg : response.body()) {
                        boolean isOutgoing = currentUserId != null && currentUserId.equals(msg.senderId.toString());
                        newMessages.add(new Message(
                                msg.id.toString(), msg.text, msg.senderId.toString(),
                                msg.senderName, msg.senderAvatar, msg.createdAt, isOutgoing
                        ));
                    }
                    messagesLiveData.setValue(newMessages);
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<MessageResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "loadHistory error", t);
            }
        });
    }

    private void sendMessage(String text) {
        messagingApi.sendMessage(new SendMessageRequest(chatId, text)).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(@NonNull Call<MessageResponse> call, @NonNull Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    cancelEditing();
                }
            }
            @Override
            public void onFailure(@NonNull Call<MessageResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Send error", t);
            }
        });
    }

    private void editMessage(String text) {
        messagingApi.editMessage(editingMessageId, new SendMessageRequest(chatId, text)).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(@NonNull Call<MessageResponse> call, @NonNull Response<MessageResponse> response) {
                if (response.isSuccessful()) {
                    cancelEditing();
                }
            }
            @Override
            public void onFailure(@NonNull Call<MessageResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Edit error", t);
            }
        });
    }

    private void connectWebSocket() {
        String token = tokenManager.getAccessToken();
        if (token == null) return;

        webSocketManager = new WebSocketManager();
        webSocketManager.connect(token, () -> {
            webSocketManager.subscribeTo(chatId, message -> {
                List<Message> current = messagesLiveData.getValue() != null
                        ? new ArrayList<>(messagesLiveData.getValue()) : new ArrayList<>();

                boolean found = false;
                for (int i = 0; i < current.size(); i++) {
                    if (current.get(i).id.equals(message.id.toString())) {
                        current.get(i).text = message.text;
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    boolean isOutgoing = currentUserId != null && currentUserId.equals(message.senderId.toString());
                    current.add(new Message(
                            message.id.toString(), message.text, message.senderId.toString(),
                            message.senderName, message.senderAvatar, message.createdAt, isOutgoing
                    ));
                }
                messagesLiveData.postValue(current);
            });

            webSocketManager.subscribeToDelete(chatId, messageId -> {
                List<Message> current = messagesLiveData.getValue() != null
                        ? new ArrayList<>(messagesLiveData.getValue()) : new ArrayList<>();
                current.removeIf(m -> m.id.equals(messageId.toString()));
                messagesLiveData.postValue(current);
            });
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (webSocketManager != null) {
            webSocketManager.disconnect();
        }
    }
}
