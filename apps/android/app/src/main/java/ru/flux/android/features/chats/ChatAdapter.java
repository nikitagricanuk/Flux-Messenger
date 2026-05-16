package ru.flux.android.features.chats;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import ru.flux.android.R;
import ru.flux.android.core.data.Chat;
import ru.flux.android.databinding.ItemChatBinding;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    private List<Chat> allChats = new ArrayList<>();
    private List<Chat> filteredChats = new ArrayList<>();
    private String activeFilter = "all";
    private String searchQuery = "";

    public interface OnChatActionListener {
        void onDeleteChat(Chat chat);
        void onAddFavorite(Chat chat);
        void onChatClick(Chat chat);
    }

    private OnChatActionListener listener;

    public void setOnChatActionListener(OnChatActionListener listener) {
        this.listener = listener;
    }

    public void setChats(List<Chat> chats) {
        allChats = chats;
        applyFilter();
    }

    public void setFilter(String filter) {
        activeFilter = filter;
        applyFilter();
    }

    public void setSearchQuery(String query) {
        searchQuery = query == null ? "" : query.toLowerCase().trim();
        applyFilter();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void applyFilter() {
        filteredChats = new ArrayList<>();
        for (Chat chat : allChats) {
            boolean matchesType = activeFilter.equals("all") || chat.type.equals(activeFilter);
            boolean matchesQuery = searchQuery.isEmpty()
                    || chat.name.toLowerCase().contains(searchQuery)
                    || (chat.lastMessage != null && chat.lastMessage.toLowerCase().contains(searchQuery));
            if (matchesType && matchesQuery) {
                filteredChats.add(chat);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChatBinding binding = ItemChatBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ChatViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = filteredChats.get(position);
        holder.binding.name.setText(chat.name);
        holder.binding.lastMessage.setText(chat.lastMessage);
        holder.binding.time.setText(chat.time);
        Glide.with(holder.itemView.getContext())
                .load(chat.avatarUrl)
                .circleCrop()
                .placeholder(R.drawable.bg_avatar_placeholder)
                .error(R.drawable.bg_avatar_placeholder)
                .into(holder.binding.avatar);

        holder.itemView.setOnLongClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add(0, 0, 0, "Удалить");
            popup.getMenu().add(0, 1, 1, "В избранное");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0 && listener != null) {
                    listener.onDeleteChat(chat);
                } else if (item.getItemId() == 1 && listener != null) {
                    listener.onAddFavorite(chat);
                }
                return true;
            });
            popup.show();
            return true;
        });
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onChatClick(chat);
        });
    }

    @Override
    public int getItemCount() {
        return filteredChats.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        final ItemChatBinding binding;

        ChatViewHolder(ItemChatBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}