package ru.flux.android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class GroupsAdapter extends RecyclerView.Adapter<GroupsAdapter.ViewHolder> {

    public interface OnGroupClickListener {
        void onGroupClick(Group group);
    }

    private final List<Group> groups;
    private final OnGroupClickListener listener;

    public GroupsAdapter(List<Group> groups, OnGroupClickListener listener) {
        this.groups = groups;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_groups, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Group group = groups.get(position);
        holder.name.setText(group.name);
        holder.members.setText(group.membersCount + " участников");
        holder.date.setText("С " + group.date);

        Glide.with(holder.avatar.getContext())
                .load(group.avatarUrl)
                .centerCrop()
                .into(holder.avatar);

        holder.itemView.setOnClickListener(v -> listener.onGroupClick(group));
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, members, date;
        ShapeableImageView avatar;

        ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.groupName);
            members = view.findViewById(R.id.groupMembers);
            date = view.findViewById(R.id.groupDate);
            avatar = view.findViewById(R.id.groupAvatar);
        }
    }
}