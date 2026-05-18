package ru.flux.android.features.chats;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import ru.flux.android.R;
import ru.flux.android.core.data.Chat;
import ru.flux.android.core.ui.AvatarColorHelper;
import ru.flux.android.core.ui.InitialsDrawable;
import ru.flux.android.databinding.ItemFavoriteBinding;

public class FavoriteAdapter extends RecyclerView.Adapter<FavoriteAdapter.FavoriteViewHolder> {
    private List<Chat> favorites = new ArrayList<>();

    public interface OnFavoriteClickListener {
        void onFavoriteClick(Chat chat);
    }

    private OnFavoriteClickListener clickListener;

    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.clickListener = listener;
    }

    /**
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return
     */
    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFavoriteBinding binding = ItemFavoriteBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new FavoriteViewHolder(binding);
    }

    /**
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder holder, int position) {
        Chat chat = favorites.get(position);
        FavoriteViewHolder vh = holder;
        vh.binding.name.setText(chat.name);
        InitialsDrawable placeholder = new InitialsDrawable(
                chat.name,
                AvatarColorHelper.colorFor(chat.name)
        );
        Glide.with(vh.itemView.getContext()).load(chat.avatarUrl)
                .circleCrop()
                .placeholder(placeholder)
                .error(placeholder)
                .into(vh.binding.avatar);

        vh.itemView.setOnClickListener(v -> {
            if (clickListener != null) clickListener.onFavoriteClick(chat);
        });
    }

    /**
     * @return
     */
    @Override
    public int getItemCount() {
        return favorites.size();
    }

    public void setFavorites(List<Chat> items) {
        favorites = items;
        notifyDataSetChanged();
    }

    static class FavoriteViewHolder extends RecyclerView.ViewHolder {
        ItemFavoriteBinding binding;
        public FavoriteViewHolder(ItemFavoriteBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
