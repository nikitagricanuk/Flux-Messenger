package ru.flux.android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class LinksAdapter extends RecyclerView.Adapter<LinksAdapter.ViewHolder> {

    public interface OnLinkClickListener {
        void onLinkClick(Link link);
    }

    private final List<Link> links;
    private final OnLinkClickListener listener;

    public LinksAdapter(List<Link> links, OnLinkClickListener listener) {
        this.links = links;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_links, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Link link = links.get(position);
        holder.name.setText(link.name);
        holder.url.setText(link.url);

        Glide.with(holder.avatar.getContext())
                .load(link.pictureLink)
                .centerCrop()
                .into(holder.avatar);

        holder.itemView.setOnClickListener(v -> listener.onLinkClick(link));
    }

    @Override
    public int getItemCount() {
        return links.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, url;
        ImageView avatar;

        ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.LinksName);
            url = view.findViewById(R.id.LinksText);
            avatar = view.findViewById(R.id.LinksAvatar);
        }
    }
}