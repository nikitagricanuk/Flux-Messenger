package ru.flux.android.core.network;

import android.util.Log;

import com.google.gson.Gson;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import io.reactivex.disposables.CompositeDisposable;
import ru.flux.android.BuildConfig;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.StompHeader;

public class WebSocketManager {

    private static final String TAG = "WebSocketManager";
    private static final String WS_URL =
            BuildConfig.BACKEND_BASE_URL.replaceFirst("^http", "ws").replaceAll("/$", "") + "/ws";
    private StompClient stompClient;
    private final CompositeDisposable disposables = new CompositeDisposable();
    private final Gson gson = new Gson();

    public interface MessageListener {
        void onMessage(MessageResponse message);
    }

    public interface DeleteListener {
        void onDelete(UUID messageId);
    }

    public void connect(String jwtToken, Runnable onConnected) {
        List<StompHeader> headers = Arrays.asList(
                new StompHeader("Authorization", "Bearer " + jwtToken)
        );

        stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, WS_URL);
        stompClient.connect(headers);

        disposables.add(
                stompClient.lifecycle().subscribe(event -> {
                    switch (event.getType()) {
                        case OPENED:
                            Log.d(TAG, "WebSocket connected");
                            if (onConnected != null) onConnected.run();
                            break;
                        case ERROR:
                            Log.e(TAG, "WebSocket error", event.getException());
                            break;
                        case CLOSED:
                            Log.d(TAG, "WebSocket closed");
                            break;
                    }
                })
        );
    }

    public interface SendCallback {
        void onSuccess();

        void onError(Throwable throwable);
    }

    public void subscribeTo(UUID chatId, MessageListener listener) {
        disposables.add(
                stompClient.topic("/topic/user/" + chatId)
                        .subscribe(stompMessage -> {
                            MessageResponse message = gson.fromJson(
                                    stompMessage.getPayload(),
                                    MessageResponse.class
                            );
                            listener.onMessage(message);
                        }, throwable -> Log.e(TAG, "Subscribe error", throwable))
        );
    }

    public void subscribeToDelete(UUID chatId, DeleteListener listener) {
        disposables.add(
                stompClient.topic("/topic/user/" + chatId + "/delete")
                        .subscribe(stompMessage -> {
                            MessageStatusUpdate update = gson.fromJson(
                                    stompMessage.getPayload(),
                                    MessageStatusUpdate.class
                            );
                            if (update.getMessageId() != null) {
                                listener.onDelete(update.getMessageId());
                            }
                        }, throwable -> Log.e(TAG, "Subscribe delete error", throwable))
        );
    }

    public void sendMessage(UUID chatId, String text, SendCallback callback) {
        String payload = gson.toJson(new SendMessageRequest(chatId, text));
        Log.d(TAG, "Sending payload: " + payload);

        stompClient.send("/app/chat.send", payload)
                .subscribe(
                        () -> {
                            Log.d(TAG, "SEND completed");
                            if (callback != null) callback.onSuccess();
                        },
                        throwable -> {
                            Log.e(TAG, "SEND failed", throwable);
                            if (callback != null) callback.onError(throwable);
                        }
                );
    }

    public void disconnect() {
        disposables.clear();
        if (stompClient != null) {
            stompClient.disconnect();
        }
    }
}
