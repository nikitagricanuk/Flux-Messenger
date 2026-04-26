package ru.flux.android;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.flux.android.data.MessagingApi;
import ru.flux.android.data.SendMessageRequest;
import ru.flux.android.data.TokenManager;
import ru.flux.android.data.WebSocketManager;

public class ChatFragment extends Fragment {

    private WebSocketManager webSocketManager;
    private ChatMessangerAdapter chatAdapter;
    private final List<Message> messages = new ArrayList<>();
    private UUID chatId;
    private TokenManager tokenManager;
    private MessagingApi messagingApi;

    public ChatFragment() {
        super(R.layout.fragment_chat);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        try {
            tokenManager = new TokenManager(requireContext());
            messagingApi = ApiClient.getMessagingApi(tokenManager);
        } catch (Exception e) {
            Log.e("ChatFragment", "TokenManager init failed", e);
            return;
        }

        String chatName = getArguments() != null
                ? getArguments().getString("chatName", "Чат") : "Чат";
        boolean isGroup = getArguments() != null
                && getArguments().getBoolean("isGroup", false);
        String chatIdStr = getArguments() != null
                ? getArguments().getString("chatId") : null;

        if (chatIdStr != null) {
            chatId = UUID.fromString(chatIdStr);
        }

        ((TextView) view.findViewById(R.id.chatName)).setText(chatName);

        RecyclerView recyclerView = view.findViewById(R.id.messagesRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        chatAdapter = new ChatMessangerAdapter(messages, isGroup);
        recyclerView.setAdapter(chatAdapter);

        if (chatId != null) {
            loadHistory(recyclerView);
            connectWebSocket();
        }

        EditText input = view.findViewById(R.id.messageInput);
        view.findViewById(R.id.btnSend).setOnClickListener(v -> {
            String text = input.getText().toString().trim();
            if (!text.isEmpty() && chatId != null && messagingApi != null) {
                sendMessageViaRest(text, input);
            }
        });

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        view.findViewById(R.id.chatHeader).setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_chatFragment_to_profileFragment)
        );
    }

    private void loadHistory(RecyclerView recyclerView) {
        messagingApi.getMessages(chatId, 0, 50).enqueue(new Callback<List<MessageResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<MessageResponse>> call,
                                   @NonNull Response<List<MessageResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String myId = tokenManager.getUserId();
                    for (MessageResponse msg : response.body()) {
                        boolean isOutgoing = myId != null
                                && myId.equals(msg.senderId.toString());
                        messages.add(new Message(
                                msg.id.toString(),
                                msg.text,
                                msg.senderId.toString(),
                                msg.senderName,
                                msg.senderAvatar,
                                msg.createdAt,
                                isOutgoing
                        ));
                    }
                    chatAdapter.notifyDataSetChanged();
                    if (!messages.isEmpty()) {
                        recyclerView.scrollToPosition(messages.size() - 1);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<MessageResponse>> call,
                                  @NonNull Throwable t) {
                Log.e("ChatFragment", "Failed to load messages", t);
            }
        });
    }

    private void connectWebSocket() {
        String token = tokenManager.getAccessToken();
        if (token == null) {
            return;
        }

        webSocketManager = new WebSocketManager();
        webSocketManager.connect(token);
        webSocketManager.subscribeTo(chatId, message -> {
            if (getActivity() == null) {
                return;
            }
            requireActivity().runOnUiThread(() -> {
                String myId = tokenManager.getUserId();
                boolean isOutgoing = myId != null
                        && myId.equals(message.senderId.toString());
                messages.add(new Message(
                        message.id.toString(),
                        message.text,
                        message.senderId.toString(),
                        message.senderName,
                        message.senderAvatar,
                        message.createdAt,
                        isOutgoing
                ));
                chatAdapter.notifyItemInserted(messages.size() - 1);
            });
        });
    }

    private void sendMessageViaRest(String text, EditText input) {
        messagingApi.sendMessage(new SendMessageRequest(chatId, text))
                .enqueue(new Callback<MessageResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MessageResponse> call,
                                           @NonNull Response<MessageResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d("ChatFragment", "Message sent via REST: " + response.body().id);
                            input.setText("");
                        } else {
                            Log.e("ChatFragment", "REST send failed, code=" + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<MessageResponse> call,
                                          @NonNull Throwable t) {
                        Log.e("ChatFragment", "REST send error", t);
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (webSocketManager != null) {
            webSocketManager.disconnect();
        }
    }
}
