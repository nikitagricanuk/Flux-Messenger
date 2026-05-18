package ru.flux.android.features.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

import ru.flux.android.R;
import ru.flux.android.core.network.UserResponse;

public class MembersAdapter extends RecyclerView.Adapter<MembersAdapter.ViewHolder> {

    private List<UserResponse> members;

    public MembersAdapter(List<UserResponse> members) {
        this.members = members;
    }

    public void setMembers(List<UserResponse> newMembers) {
        members = newMembers;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserResponse user = members.get(position);

        String fullName = user.firstName + (user.lastName != null ? " " + user.lastName : "");
        holder.name.setText(fullName);
        holder.username.setText(user.nickname != null ? "@" + user.nickname : "");

        Glide.with(holder.avatar.getContext())
                .load(user.avatarUrl)
                .circleCrop()
                .placeholder(R.drawable.bg_avatar_placeholder)
                .error(R.drawable.bg_avatar_placeholder)
                .into(holder.avatar);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, username;
        ShapeableImageView avatar;

        ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.memberName);
            username = view.findViewById(R.id.memberUsername);
            avatar = view.findViewById(R.id.memberAvatar);
        }
    }
}