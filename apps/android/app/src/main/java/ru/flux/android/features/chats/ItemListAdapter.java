package ru.flux.android.features.chats;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ru.flux.android.R;
import ru.flux.android.core.data.DisplayItem;
import ru.flux.android.core.ui.AvatarColorHelper;
import ru.flux.android.core.ui.InitialsDrawable;
import ru.flux.android.databinding.ItemNewMessageContactBinding;

public class ItemListAdapter extends RecyclerView.Adapter<ItemListAdapter.ViewHolder> {

    private List<DisplayItem> allItems = new ArrayList<>();
    private List<DisplayItem> filteredItems = new ArrayList<>();
    private String searchQuery = "";

    private boolean selectable = false;
    // Tracks selected items by their position in allItems (stable across filter changes)
    private final Set<DisplayItem> selectedItems = new HashSet<>();

    public void setSelectable(boolean selectable) {
        this.selectable = selectable;
        selectedItems.clear();
        notifyDataSetChanged();
    }

    /** Returns a copy of the currently selected items. */
    public List<DisplayItem> getSelected() {
        return new ArrayList<>(selectedItems);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setItems(List<DisplayItem> allItems) {
        this.allItems = allItems;
        selectedItems.clear();
        applyFilter();
    }

    public void setSearchQuery(String query) {
        searchQuery = query == null ? "" : query.toLowerCase().trim();
        applyFilter();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void applyFilter() {
        filteredItems = new ArrayList<>();
        for (DisplayItem item : allItems) {
            boolean matchesQuery = searchQuery.isEmpty()
                    || item.name.toLowerCase().contains(searchQuery)
                    || (item.subtitle != null && item.subtitle.toLowerCase().contains(searchQuery));
            if (matchesQuery) {
                filteredItems.add(item);
            }
        }
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
        DisplayItem item = filteredItems.get(position);
        holder.binding.contactName.setText(item.name);
        holder.binding.contactPhone.setText(item.subtitle);
        InitialsDrawable placeholder = new InitialsDrawable(
                item.name,
                AvatarColorHelper.colorFor(item.name)
        );
        Glide.with(holder.itemView.getContext())
                .load(item.avatarUrl)
                .circleCrop()
                .placeholder(placeholder)
                .error(placeholder)
                .into(holder.binding.avatar);

        if (selectable) {
            boolean selected = selectedItems.contains(item);
            holder.binding.imageView2.setImageResource(
                    selected ? R.drawable.ic_radio_selected : R.drawable.ic_radio_unselected);
            holder.itemView.setOnClickListener(v -> {
                if (selectedItems.contains(item)) {
                    selectedItems.remove(item);
                } else {
                    selectedItems.add(item);
                }
                notifyItemChanged(holder.getBindingAdapterPosition());
            });
        } else {
            holder.binding.imageView2.setImageResource(R.drawable.fi_rr_angle_small_left);
            holder.itemView.setOnClickListener(v -> item.onClick.run());
        }
    }

    @Override
    public int getItemCount() {
        return filteredItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemNewMessageContactBinding binding;

        ViewHolder(ItemNewMessageContactBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}