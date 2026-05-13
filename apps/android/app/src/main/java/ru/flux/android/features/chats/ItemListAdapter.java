package ru.flux.android.features.chats;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import ru.flux.android.R;
import ru.flux.android.core.data.DisplayItem;
import ru.flux.android.databinding.ItemNewMessageContactBinding;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ViewHolder> {

    private List<DisplayItem> items = new ArrayList<>();

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<DisplayItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNewMessageContactBinding binding = ItemNewMessageContactBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DisplayItem item = items.get(position);
        holder.binding.contactName.setText(item.name);
        holder.binding.contactPhone.setText(item.subtitle);
        Glide.with(holder.itemView.getContext())
                .load(item.avatarUrl)
                .circleCrop()
                .placeholder(R.drawable.bg_avatar_placeholder)
                .error(R.drawable.bg_avatar_placeholder)
                .into(holder.binding.avatar);
        holder.itemView.setOnClickListener(v -> item.onClick.run());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemNewMessageContactBinding binding;

        ViewHolder(ItemNewMessageContactBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}