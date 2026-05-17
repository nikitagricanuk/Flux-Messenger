package ru.flux.android.features.contacts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import ru.flux.android.R;
import ru.flux.android.core.data.Contact;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    public interface OnContactClickListener {
        void onContactClick(Contact contact);
    }

    private List<Contact> contacts;
    private final OnContactClickListener listener;

    public ContactAdapter(List<Contact> contacts, OnContactClickListener listener) {
        this.contacts = contacts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Contact contact = contacts.get(position);
        holder.name.setText(contact.getName());
        holder.phone.setText(contact.getPhoneNumber());

        Glide.with(holder.itemView.getContext())
                .load(contact.getProfilePicture())
                .circleCrop()
                .placeholder(R.drawable.bg_avatar_placeholder)
                .error(R.drawable.bg_avatar_placeholder)
                .into(holder.avatar);

        holder.itemView.setOnClickListener(v -> listener.onContactClick(contact));
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public void setContacts(List<Contact> newContacts) {
        contacts.clear();
        contacts.addAll(newContacts);
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, phone;
        ImageView avatar;

        ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.contactName);
            phone = view.findViewById(R.id.contactPhone);
            avatar = view.findViewById(R.id.contactAvatar);
        }
    }
}