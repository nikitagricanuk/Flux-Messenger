package ru.flux.android.features.chat.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import android.net.Uri;

import java.util.ArrayList;
import java.util.UUID;

import ru.flux.android.R;
import ru.flux.android.core.data.Message;
import ru.flux.android.features.chat.ChatMessangerAdapter;
import ru.flux.android.features.chat.ChatViewModel;

public class ChatFragment extends Fragment {

    private ChatViewModel viewModel;
    private ChatMessangerAdapter chatAdapter;
    private RecyclerView recyclerView;
    private EditText input;
    private UUID chatId;

    public ChatFragment() {
        super(R.layout.fragment_chat);
    }

    private final ActivityResultLauncher<String> pickMedia = registerForActivityResult(
            new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    viewModel.setMediaAttachment(uri);
                    showAttachmentPreview(uri);
                }
            });

    private void showAttachmentPreview(Uri uri) {
        View preview = requireView().findViewById(R.id.attachmentPreview);
        ShapeableImageView thumbnail = requireView().findViewById(R.id.attachmentThumbnail);
        preview.setVisibility(View.VISIBLE);
        Glide.with(this).load(uri).centerCrop().into(thumbnail);

        requireView().findViewById(R.id.btnRemoveAttachment).setOnClickListener(v -> {
            viewModel.clearMediaAttachment();
            preview.setVisibility(View.GONE);
        });
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        String chatName = getArguments() != null
                ? getArguments().getString("chatName", "Чат") : "Чат";
        boolean isGroup = getArguments() != null
                && getArguments().getBoolean("isGroup", false);
        String chatIdStr = getArguments() != null
                ? getArguments().getString("chatId") : null;
        String peerId = getArguments() != null
                ? getArguments().getString("peerId") : null;
        String chatAvatarUrl = getArguments() != null
                ? getArguments().getString("chatAvatarUrl") : null;

        if (chatIdStr != null) {
            chatId = UUID.fromString(chatIdStr);
            viewModel.initChat(chatId);
        }

        setupUI(view, chatName, chatAvatarUrl, peerId);
        setupRecyclerView(view, isGroup);
        observeViewModel();
    }

    private void setupUI(View view, String chatName, String chatAvatarUrl, String peerId) {
        ShapeableImageView chatAvatar = view.findViewById(R.id.chatAvatar);
        ((TextView) view.findViewById(R.id.chatName)).setText(chatName);
        input = view.findViewById(R.id.messageInput);

        if (chatAvatarUrl != null) {
            Glide.with(this)
                    .load(chatAvatarUrl)
                    .placeholder(R.drawable.bg_avatar_placeholder)
                    .error(R.drawable.bg_avatar_placeholder)
                    .into(chatAvatar);
        }

        view.findViewById(R.id.btnSend).setOnClickListener(v -> {
            String text = input.getText().toString().trim();
            viewModel.onSendClicked(text);
        });

        view.findViewById(R.id.btnBack).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());

        view.findViewById(R.id.chatHeader).setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("contactId", peerId);
            if (chatId != null) args.putString("chatId", chatId.toString());
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_chatFragment_to_profileFragment, args);
        });

        view.findViewById(R.id.btnAttach).setOnClickListener(v ->
                pickMedia.launch("image/*"));
    }

    private void setupRecyclerView(View view, boolean isGroup) {
        recyclerView = view.findViewById(R.id.messagesRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);

        chatAdapter = new ChatMessangerAdapter(new ArrayList<>(), isGroup,
                this::showBottomSheet);
        recyclerView.setAdapter(chatAdapter);
    }

    private void observeViewModel() {
        viewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            chatAdapter.updateMessages(messages);
            if (!messages.isEmpty()) {
                recyclerView.scrollToPosition(messages.size() - 1);
            }
        });

        viewModel.getEditingMessageText().observe(getViewLifecycleOwner(), text -> {
            input.setText(text);
            if (text != null && !text.isEmpty()) {
                input.requestFocus();
                input.setSelection(text.length());
            }
        });

        viewModel.getClearInput().observe(getViewLifecycleOwner(), clear -> {
            if (clear != null && clear) {
                input.setText("");
                View preview = requireView().findViewById(R.id.attachmentPreview);
                preview.setVisibility(View.GONE);
            }
        });

        viewModel.getIsUploading().observe(getViewLifecycleOwner(), uploading -> {
            View btnSend = requireView().findViewById(R.id.btnSend);
            btnSend.setEnabled(!uploading);
            btnSend.setAlpha(uploading ? 0.2f : 1f);
        });

        recyclerView.addOnLayoutChangeListener((v, left, top, right, bottom,
                                                oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                if (!chatAdapter.isEmpty()) {
                    recyclerView.scrollToPosition(chatAdapter.getItemCount() - 1);
                }
            }
        });
    }

    private void showBottomSheet(Message message) {
        MessageActionsBottomSheet sheet = MessageActionsBottomSheet.newInstance(message);
        sheet.setMessage(message);
        sheet.setListener(new MessageActionsBottomSheet.MessageActionListener() {
            @Override public void onReply(Message message) {}

            @Override
            public void onCopy(Message message) {
                android.content.ClipboardManager clipboard =
                        (android.content.ClipboardManager) requireContext()
                                .getSystemService(android.content.Context.CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(
                        android.content.ClipData.newPlainText("message", message.text));
            }

            @Override
            public void onEdit(Message message) {
                viewModel.setEditingMessage(message);
            }

            @Override
            public void onDelete(Message message) {
                viewModel.deleteMessage(message);
            }
        });
        sheet.show(getChildFragmentManager(), "messageActions");
    }
}