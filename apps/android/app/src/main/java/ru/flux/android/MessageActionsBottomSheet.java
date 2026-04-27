package ru.flux.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class MessageActionsBottomSheet extends BottomSheetDialogFragment {

    public interface MessageActionListener {
        void onReply(Message message);

        void onCopy(Message message);

        void onEdit(Message message);

        void onDelete(Message message);
    }

    private static final String ARG_MESSAGE_ID = "messageId";
    private static final String ARG_IS_OUTGOING = "isOutgoing";

    private Message message;
    private MessageActionListener listener;

    public static MessageActionsBottomSheet newInstance(Message message) {
        MessageActionsBottomSheet sheet = new MessageActionsBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE_ID, message.id);
        args.putBoolean(ARG_IS_OUTGOING, message.isOutgoing);
        sheet.setArguments(args);
        return sheet;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

    public void setListener(MessageActionListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_message_actions, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        boolean isOutgoing = getArguments() != null
                && getArguments().getBoolean(ARG_IS_OUTGOING, false);

        view.findViewById(R.id.actionReply).setOnClickListener(v -> {
            if (listener != null) listener.onReply(message);
            dismiss();
        });

        view.findViewById(R.id.actionCopy).setOnClickListener(v -> {
            if (listener != null) listener.onCopy(message);
            dismiss();
        });

        view.findViewById(R.id.actionEdit).setVisibility(isOutgoing ? View.VISIBLE : View.GONE);
        view.findViewById(R.id.actionDelete).setVisibility(isOutgoing ? View.VISIBLE : View.GONE);

        view.findViewById(R.id.actionEdit).setOnClickListener(v -> {
            if (listener != null) listener.onEdit(message);
            dismiss();
        });

        view.findViewById(R.id.actionDelete).setOnClickListener(v -> {
            if (listener != null) listener.onDelete(message);
            dismiss();
        });
    }
}