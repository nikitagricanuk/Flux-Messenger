package ru.flux.android.features.contacts;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.flux.android.R;
import ru.flux.android.core.data.Contact;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    public interface OnContactClickListener {
        void onContactClick(Contact contact);
    }

    private final List<Contact> contacts;
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

        holder.itemView.setOnClickListener(v -> listener.onContactClick(contact));
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name, phone;

        ViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.contactName);
            phone = view.findViewById(R.id.contactPhone);
        }
    }
}