package ru.flux.android.features.chat.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.flux.android.R;
import ru.flux.android.core.auth.TokenManager;
import ru.flux.android.core.data.Message;
import ru.flux.android.core.network.ApiClient;
import ru.flux.android.core.network.ApiService;
import ru.flux.android.core.network.MessageResponse;
import ru.flux.android.core.network.SendMessageRequest;
import ru.flux.android.core.network.UserResponse;
import ru.flux.android.core.network.WebSocketManager;
import ru.flux.android.features.chat.ChatMessangerAdapter;

public class ChatFragment extends Fragment {

    private static final String TAG = "ChatFragment";

    private WebSocketManager webSocketManager;
    private ChatMessangerAdapter chatAdapter;
    private final List<Message> messages = new ArrayList<>();
    private UUID chatId;
    private TokenManager tokenManager;
    private ApiService messagingApi;
    private UUID editingMessageId = null;
    private String currentUserId = null;
    private RecyclerView recyclerView;
    private String chatAvatarUrl = null;

    public ChatFragment() {
        super(R.layout.fragment_chat);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        messages.clear();

        try {
            tokenManager = new TokenManager(requireContext());
            messagingApi = ApiClient.api(requireContext());
        } catch (Exception e) {
            Log.e(TAG, "Init failed", e);
            return;
        }

        String chatName = getArguments() != null
                ? getArguments().getString("chatName", "Чат") : "Чат";
        boolean isGroup = getArguments() != null
                && getArguments().getBoolean("isGroup", false);
        String chatIdStr = getArguments() != null
                ? getArguments().getString("chatId") : null;
        String peerId = getArguments() != null
                ? getArguments().getString("peerId") : null;
        chatAvatarUrl = getArguments() != null
                ? getArguments().getString("chatAvatarUrl") : null;

        ShapeableImageView chatAvatar = view.findViewById(R.id.chatAvatar);
        if (chatAvatarUrl != null) {
            Glide.with(this)
                    .load(chatAvatarUrl)
                    .placeholder(R.drawable.bg_avatar_placeholder)
                    .error(R.drawable.bg_avatar_placeholder)
                    .into(chatAvatar);
        }

        if (chatIdStr != null) {
            chatId = UUID.fromString(chatIdStr);
        }

        ((TextView) view.findViewById(R.id.chatName)).setText(chatName);


        recyclerView = view.findViewById(R.id.messagesRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        chatAdapter = new ChatMessangerAdapter(messages, isGroup, message -> {
            MessageActionsBottomSheet sheet = MessageActionsBottomSheet.newInstance(message);
            sheet.setMessage(message);
            sheet.setListener(new MessageActionsBottomSheet.MessageActionListener() {
                @Override
                public void onReply(Message message) {}

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
                                public void onFailure(@NonNull Call<Void> call,
                                                      @NonNull Throwable t) {
                                    Log.e(TAG, "Delete error", t);
                                }
                            });
                }
            });
            sheet.show(getChildFragmentManager(), "messageActions");
        });
        recyclerView.setAdapter(chatAdapter);

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
                                    Log.e(TAG, "Edit failed: " + response.code());
                                }
                            }

                            @Override
                            public void onFailure(@NonNull Call<MessageResponse> call,
                                                  @NonNull Throwable t) {
                                Log.e(TAG, "Edit error", t);
                            }
                        });
            } else {
                sendMessageViaRest(text, input);
            }
        });

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        view.findViewById(R.id.chatHeader).setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("contactId", peerId);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_chatFragment_to_profileFragment, args);
        });

        if (chatId != null) {
            loadCurrentUser();
        }
    }

    private void loadCurrentUser() {
        try {
            ApiClient.api(requireContext()).getMe().enqueue(new Callback<UserResponse>() {
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
        } catch (Exception e) {
            Log.e(TAG, "ApiClient error", e);
        }
    }

    private void loadHistory() {
        messagingApi.getMessages(chatId, 0, 50).enqueue(new Callback<List<MessageResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<MessageResponse>> call,
                                   @NonNull Response<List<MessageResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (MessageResponse msg : response.body()) {
                        boolean isOutgoing = currentUserId != null
                                && currentUserId.equals(msg.senderId.toString());
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
            public void onFailure(@NonNull Call<List<MessageResponse>> call, @NonNull Throwable t) {
                Log.e(TAG, "loadHistory error", t);
            }
        });
    }

    private void connectWebSocket() {
        String token = tokenManager.getAccessToken();
        if (token == null) return;

        webSocketManager = new WebSocketManager();
        webSocketManager.connect(token, () -> {
            webSocketManager.subscribeTo(chatId, message -> {
                if (getActivity() == null) return;
                requireActivity().runOnUiThread(() -> {
                    int existingIndex = -1;
                    for (int i = 0; i < messages.size(); i++) {
                        if (messages.get(i).id.equals(message.id.toString())) {
                            existingIndex = i;
                            break;
                        }
                    }
                    if (existingIndex != -1) {
                        messages.get(existingIndex).text = message.text;
                        chatAdapter.notifyItemChanged(existingIndex);
                    } else {
                        boolean isOutgoing = currentUserId != null
                                && currentUserId.equals(message.senderId.toString());
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
                        recyclerView.scrollToPosition(messages.size() - 1);
                    }
                });
            });

            webSocketManager.subscribeToDelete(chatId, messageId -> {
                if (getActivity() == null) return;
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
        });
    }

    private void sendMessageViaRest(String text, EditText input) {
        messagingApi.sendMessage(new SendMessageRequest(chatId, text))
                .enqueue(new Callback<MessageResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MessageResponse> call,
                                           @NonNull Response<MessageResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Log.d(TAG, "Message sent: " + response.body().id);
                            input.setText("");
                        } else {
                            Log.e(TAG, "Send failed: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<MessageResponse> call, @NonNull Throwable t) {
                        Log.e(TAG, "Send error", t);
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