package ru.flux.android.features.chat;

import android.app.Application;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
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
    private final MutableLiveData<Boolean> isUploading = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> loadingMessages = new MutableLiveData<>(false);
    public LiveData<Boolean> getIsUploading() { return isUploading; }
    public LiveData<Boolean> isLoadingMessages() { return loadingMessages; }
    private ApiService messagingApi;
    private TokenManager tokenManager;
    private WebSocketManager webSocketManager;
    private UUID chatId;
    private String currentUserId = null;
    private UUID editingMessageId = null;
    private Uri pendingMediaUri = null;
    private String pendingMimeType = null;

    public ChatViewModel(@NonNull Application application) {
        super(application);
        try {
            tokenManager = new TokenManager(application.getApplicationContext());
            messagingApi = ApiClient.api(application.getApplicationContext());
        } catch (Exception e) {
            Log.e(TAG, "Init failed", e);
        }
    }

    public LiveData<List<Message>> getMessages() { return messagesLiveData; }
    public LiveData<String> getEditingMessageText() { return editingMessageTextLiveData; }
    public LiveData<Boolean> getClearInput() { return clearInput; }

    public void initChat(UUID chatId) {
        if (this.chatId != null) return;
        this.chatId = chatId;
        loadingMessages.setValue(true);
        loadCurrentUser();
    }

    public void onSendClicked(String text) {
        if (chatId == null) return;
        if (pendingMediaUri != null) {
            sendMedia(pendingMediaUri, text);
        } else if (!text.isEmpty()) {
            if (editingMessageId != null) editMessage(text);
            else sendMessage(text);
        }
    }

    public void setEditingMessage(Message message) {
        editingMessageId = UUID.fromString(message.id);
        editingMessageTextLiveData.setValue(message.text);
    }

    public void setMediaAttachment(Uri uri) {
        pendingMediaUri = uri;
        try {
            pendingMimeType = getApplication().getContentResolver().getType(uri);
        } catch (Exception e) {
            Log.e(TAG, "getType error", e);
        }
    }

    public boolean hasMediaAttachment() {
        return pendingMediaUri != null;
    }

    public void clearMediaAttachment() {
        pendingMediaUri = null;
        pendingMimeType = null;
    }

    public void cancelEditing() {
        editingMessageId = null;
        editingMessageTextLiveData.setValue("");
        clearInput.postValue(true);
    }

    public void deleteMessage(Message message) {
        messagingApi.deleteMessage(UUID.fromString(message.id)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    List<Message> current = new ArrayList<>(messagesLiveData.getValue());
                    current.remove(message);
                    messagesLiveData.postValue(current);
                }
            }
            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "Delete error", t);
            }
        });
    }

    public void sendMedia(Uri fileUri, String caption) {
        Log.d(TAG, "sendMedia called, uri=" + fileUri + " caption=" + caption);
        isUploading.postValue(true);
        try {
            android.content.ContentResolver resolver =
                    getApplication().getApplicationContext().getContentResolver();
            InputStream inputStream = resolver.openInputStream(fileUri);
            String mimeType = resolver.getType(fileUri);
            String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            String fileName = "media_" + UUID.randomUUID() + "." + ext;

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] chunk = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(chunk)) != -1) {
                buffer.write(chunk, 0, bytesRead);
            }
            byte[] bytes = buffer.toByteArray();
            inputStream.close();

            RequestBody requestBody = RequestBody.create(
                    bytes,
                    MediaType.parse(mimeType != null ? mimeType : "image/*")
            );
            MultipartBody.Part filePart = MultipartBody.Part
                    .createFormData("file", fileName, requestBody);

            messagingApi.uploadMedia(filePart).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call,
                                       @NonNull Response<ResponseBody> response) {
                    isUploading.postValue(false);
                    if (response.isSuccessful() && response.body() != null) {
                        try {
                            String mediaUrl = response.body().string();
                            String mediaType = mimeType != null && mimeType.startsWith("video")
                                    ? "VIDEO" : "IMAGE";
                            sendMessageWithMedia(mediaUrl, mediaType, caption);
                            clearMediaAttachment();
                        } catch (Exception e) {
                            Log.e(TAG, "Read response error", e);
                        }
                    } else {
                        Log.e(TAG, "Upload failed: " + response.code());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    isUploading.postValue(false);
                    Log.e(TAG, "Upload error", t);
                }
            });
        } catch (Exception e) {
            isUploading.postValue(false);
            Log.e(TAG, "sendMedia error", e);
        }
    }

    private void sendMessageWithMedia(String mediaUrl, String mediaType, String caption) {
        messagingApi.sendMessage(
                        new SendMessageRequest(chatId, caption != null ? caption : "", mediaUrl, mediaType))
                .enqueue(new Callback<MessageResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MessageResponse> call,
                                           @NonNull Response<MessageResponse> response) {
                        if (response.isSuccessful()) {
                            clearInput.postValue(true);
                        } else {
                            Log.e(TAG, "Send media failed: " + response.code());
                        }
                    }
                    @Override
                    public void onFailure(@NonNull Call<MessageResponse> call, @NonNull Throwable t) {
                        Log.e(TAG, "Send media error", t);
                    }
                });
    }
    private void loadCurrentUser() {
        messagingApi.getMe().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(@NonNull Call<UserResponse> call,
                                   @NonNull Response<UserResponse> response) {
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
            public void onResponse(@NonNull Call<List<MessageResponse>> call,
                                   @NonNull Response<List<MessageResponse>> response) {
                loadingMessages.postValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Message> newMessages = new ArrayList<>();
                    for (MessageResponse msg : response.body()) {
                        newMessages.add(toMessage(msg));
                    }
                    messagesLiveData.postValue(newMessages);
                }
            }
            @Override
            public void onFailure(@NonNull Call<List<MessageResponse>> call, @NonNull Throwable t) {
                loadingMessages.postValue(false);
                Log.e(TAG, "loadHistory error", t);
            }
        });
    }

    private void sendMessage(String text) {
        messagingApi.sendMessage(new SendMessageRequest(chatId, text, null, null))
                .enqueue(new Callback<MessageResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MessageResponse> call,
                                           @NonNull Response<MessageResponse> response) {
                        if (response.isSuccessful()) cancelEditing();
                        else Log.e(TAG, "Send failed: " + response.code());
                    }
                    @Override
                    public void onFailure(@NonNull Call<MessageResponse> call, @NonNull Throwable t) {
                        Log.e(TAG, "Send error", t);
                    }
                });
    }

    private void editMessage(String text) {
        messagingApi.editMessage(editingMessageId, new SendMessageRequest(chatId, text, null, null))
                .enqueue(new Callback<MessageResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MessageResponse> call,
                                           @NonNull Response<MessageResponse> response) {
                        if (response.isSuccessful()) cancelEditing();
                        else Log.e(TAG, "Edit failed: " + response.code());
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
                if (!found) current.add(toMessage(message));
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

    private Message toMessage(MessageResponse msg) {
        boolean isOutgoing = currentUserId != null
                && currentUserId.equals(msg.senderId.toString());
        return new Message(
                msg.id.toString(), msg.text, msg.senderId.toString(),
                msg.senderName, msg.senderAvatar, msg.createdAt,
                isOutgoing, msg.mediaUrl, msg.mediaType
        );
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (webSocketManager != null) webSocketManager.disconnect();
    }
}