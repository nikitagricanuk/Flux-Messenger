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
    private UUID editingMessageId = null;

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

        chatAdapter = new ChatMessangerAdapter(messages, isGroup, message -> {
            MessageActionsBottomSheet sheet = MessageActionsBottomSheet.newInstance(message);
            sheet.setMessage(message);
            sheet.setListener(new MessageActionsBottomSheet.MessageActionListener() {

                @Override
                public void onReply(Message message) {
                    // TODO: показать панель ответа
                }

                @Override
                public void onCopy(Message message) {
                    android.content.ClipboardManager clipboard =
                            (android.content.ClipboardManager) requireContext()
                                    .getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                    android.content.ClipData clip =
                            android.content.ClipData.newPlainText("message", message.text);
                    clipboard.setPrimaryClip(clip);
                }

                @Override
                public void onEdit(Message message) {
                    EditText input = view.findViewById(R.id.messageInput);
                    input.setText(message.text);
                    input.requestFocus();
                    editingMessageId = UUID.fromString(message.id);
                }

                @Override
                public void onDelete(Message message) {
                    messagingApi.deleteMessage(UUID.fromString(message.id))
                            .enqueue(new Callback<Void>() {
                                @Override
                                public void onResponse(@NonNull Call<Void> call,
                                                       @NonNull Response<Void> response) {
                                    if (response.isSuccessful()) {
                                        messages.remove(message);
                                        chatAdapter.notifyDataSetChanged();
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                                    Log.e("ChatFragment", "Delete error", t);
                                }
                            });
                }
            });
            sheet.show(getChildFragmentManager(), "messageActions");
        });
        recyclerView.setAdapter(chatAdapter);

        if (chatId != null) {
            loadHistory(recyclerView);
            connectWebSocket();
        }

        EditText input = view.findViewById(R.id.messageInput);
        view.findViewById(R.id.btnSend).setOnClickListener(v -> {
            String text = input.getText().toString().trim();
            if (text.isEmpty() || chatId == null) return;

            if (editingMessageId != null) {
                messagingApi.editMessage(editingMessageId, new SendMessageRequest(chatId, text))
                        .enqueue(new Callback<MessageResponse>() {
                            @Override
                            public void onResponse(@NonNull Call<MessageResponse> call,
                                                   @NonNull Response<MessageResponse> response) {
                                if (response.isSuccessful()) {
                                    input.setText("");
                                    editingMessageId = null;
                                } else {
                                    Log.e("ChatFragment", "Edit failed, code=" + response.code());
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<MessageResponse> call, @NonNull Throwable t) {
                                Log.e("ChatFragment", "Edit error", t);
                            }
                        });
            } else {
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
                int existingIndex = -1;
                for (int i = 0; i < messages.size(); i++) {
                    if (messages.get(i).id.equals(message.id.toString())) {
                        existingIndex = i;
                        break;
                    }
                }
                if (existingIndex != -1) {
                    Message existing = messages.get(existingIndex);
                    existing.text = message.text;
                    chatAdapter.notifyItemChanged(existingIndex);
                } else {
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
                }
            });
        });
        webSocketManager.subscribeToDelete(chatId, messageId -> {
            if (getActivity() == null) {
                return;
            }
            requireActivity().runOnUiThread(() -> {
                for (int i = 0; i < messages.size(); i++) {
                    if (messages.get(i).id.equals(messageId.toString())) {
                        messages.remove(i);
                        chatAdapter.notifyItemRemoved(i);
                        break;
                    }
                }
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
