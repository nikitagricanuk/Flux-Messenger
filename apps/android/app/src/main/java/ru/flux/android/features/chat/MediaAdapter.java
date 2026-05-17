package ru.flux.android.features.chat;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import ru.flux.android.R;

public class MediaAdapter extends RecyclerView.Adapter<MediaAdapter.ViewHolder> {

    public interface OnImageClickListener {
        void onImageClick(String imageUrl);
    }

    private List<String> imageUrls;
    private final OnImageClickListener listener;

    public MediaAdapter(List<String> imageUrls, OnImageClickListener listener) {
        this.imageUrls = imageUrls;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_media, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String url = imageUrls.get(position);

        Glide.with(holder.image.getContext())
                .load(url)
                .placeholder(R.drawable.bg_avatar_placeholder)
                .error(R.drawable.ic_exclamation)
                .centerCrop()
                .into(holder.image);
        holder.itemView.setOnClickListener(v -> listener.onImageClick(url));
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView image;

        ViewHolder(View view) {
            super(view);
            image = view.findViewById(R.id.mediaImage);
        }
    }

    public void updateImages(List<String> newUrls) {
        imageUrls.clear();
        imageUrls.addAll(newUrls);
        notifyDataSetChanged();
    }
}