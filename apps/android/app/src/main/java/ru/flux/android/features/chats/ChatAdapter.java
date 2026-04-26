package ru.flux.android.features.chats;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.flux.android.R;
import ru.flux.android.core.auth.TokenManager;
import ru.flux.android.core.data.Chat;
import ru.flux.android.core.network.ApiClient;
import ru.flux.android.core.network.ApiService;
import ru.flux.android.core.network.ChatResponse;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {

    // allChats is the full unfiltered list from the API
    // filteredChats is what the RecyclerView actually displays
    private List<Chat> allChats = new ArrayList<>();
    private List<Chat> filteredChats = new ArrayList<>();
    private String activeFilter = "all";

    public interface OnChatActionListener {
        void onDeleteChat(Chat chat);
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

    private void applyFilter() {
        filteredChats = new ArrayList<>();
        for (Chat chat : allChats) {
            if (activeFilter.equals("all") || chat.type.equals(activeFilter)) {
                filteredChats.add(chat);
            }
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        Chat chat = filteredChats.get(position);
        holder.name.setText(chat.name);
        holder.lastMessage.setText(chat.lastMessage);
        holder.time.setText(chat.time);

        holder.itemView.setOnLongClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add(0, 0, 0, "Delete");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 0 && listener != null) {
                    listener.onDeleteChat(chat);
                }
                return true;
            });
            popup.show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return filteredChats.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView avatar;
        TextView name, lastMessage, time;

        ChatViewHolder(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.name);
            lastMessage = itemView.findViewById(R.id.lastMessage);
            time = itemView.findViewById(R.id.time);
        }
    }
}
