package ru.flux.android.features.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import ru.flux.android.R;
import ru.flux.android.core.data.Message;

public class ChatMessangerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_IN = 0;
    private static final int TYPE_OUT = 1;

    private final List<Message> messages;
    private final boolean isGroupChat;

    public interface OnMessageLongClickListener {
        void onLongClick(Message message);
    }

    private final OnMessageLongClickListener longClickListener;

    public ChatMessangerAdapter(List<Message> messages, boolean isGroupChat,
                                OnMessageLongClickListener longClickListener) {
        this.messages = messages;
        this.isGroupChat = isGroupChat;
        this.longClickListener = longClickListener;
    }

    public void updateMessages(List<Message> newMessages) {
        this.messages.clear();
        this.messages.addAll(newMessages);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isOutgoing ? TYPE_OUT : TYPE_IN;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_OUT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_out, parent, false);
            return new OutViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_in, parent, false);
            return new InViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);

        if (holder instanceof OutViewHolder) {
            OutViewHolder h = (OutViewHolder) holder;
            h.text.setText(message.text);
            h.time.setText(message.time);

            if (message.mediaUrl != null) {
                h.mediaImage.setVisibility(View.VISIBLE);
                Glide.with(h.mediaImage.getContext())
                        .load(message.mediaUrl)
                        .placeholder(R.drawable.bg_avatar_placeholder)
                        .error(R.drawable.bg_avatar_placeholder)
                        .into(h.mediaImage);
            } else {
                h.mediaImage.setVisibility(View.GONE);
            }

            h.itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) longClickListener.onLongClick(message);
                return true;
            });

        } else if (holder instanceof InViewHolder) {
            InViewHolder h = (InViewHolder) holder;
            h.text.setText(message.text);
            h.time.setText(message.time);

            if (message.mediaUrl != null) {
                h.mediaImage.setVisibility(View.VISIBLE);
                Glide.with(h.mediaImage.getContext())
                        .load(message.mediaUrl)
                        .placeholder(R.drawable.bg_avatar_placeholder)
                        .error(R.drawable.bg_avatar_placeholder)
                        .into(h.mediaImage);
            } else {
                h.mediaImage.setVisibility(View.GONE);
            }

            if (isGroupChat) {
                h.senderName.setVisibility(View.VISIBLE);
                h.senderName.setText(message.senderName);
                h.avatar.setVisibility(View.VISIBLE);
                Glide.with(h.avatar.getContext())
                        .load(message.senderAvatar)
                        .circleCrop()
                        .placeholder(R.drawable.bg_avatar_placeholder)
                        .error(R.drawable.bg_avatar_placeholder)
                        .into(h.avatar);
            } else {
                h.senderName.setVisibility(View.GONE);
                h.avatar.setVisibility(View.GONE);
            }

            h.itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) longClickListener.onLongClick(message);
                return true;
            });
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class OutViewHolder extends RecyclerView.ViewHolder {
        TextView text, time;
        ImageView mediaImage;

        OutViewHolder(View view) {
            super(view);
            text = view.findViewById(R.id.messageText);
            time = view.findViewById(R.id.messageTime);
            mediaImage = view.findViewById(R.id.messageMedia);
        }
    }

    static class InViewHolder extends RecyclerView.ViewHolder {
        TextView text, time, senderName;
        ShapeableImageView avatar;
        ImageView mediaImage;

        InViewHolder(View view) {
            super(view);
            text = view.findViewById(R.id.messageText);
            time = view.findViewById(R.id.messageTime);
            senderName = view.findViewById(R.id.senderName);
            avatar = view.findViewById(R.id.senderAvatar);
            mediaImage = view.findViewById(R.id.messageMedia);
        }
    }
}