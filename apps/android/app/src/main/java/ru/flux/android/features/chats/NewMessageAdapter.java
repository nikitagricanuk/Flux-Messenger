package ru.flux.android.features.chats;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ru.flux.android.core.data.Contact;
import ru.flux.android.R;
import ru.flux.android.databinding.ItemNewMessageContactBinding;

public class NewMessageAdapter extends RecyclerView.Adapter<NewMessageAdapter.ContactViewHolder> {

    private List<Contact> contacts;
    private final OnContactClickListener clickListener;

    public NewMessageAdapter(List<Contact> contacts, OnContactClickListener clickListener) {
        this.contacts = contacts;
        this.clickListener = clickListener;
    }

    public interface OnContactClickListener {
        void onContactClick(Contact contact);
    }

    public void setContacts(List<Contact> contacts) {
        this.contacts = contacts;
        notifyDataSetChanged();
    }
    /**
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return
     */
    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNewMessageContactBinding binding = ItemNewMessageContactBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ContactViewHolder(binding.getRoot());
    }

    /**
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contacts.get(position);
        holder.name.setText(contact.getName());
        holder.phoneOrEmail.setText(contact.getPhoneNumber());
        holder.itemView.setOnClickListener(v -> clickListener.onContactClick(contact));
    }


    /**
     * @return
     */
    @Override
    public int getItemCount() {
        return contacts.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        ImageView avatar;
        TextView name;
        TextView phoneOrEmail;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.contactName);
            phoneOrEmail = itemView.findViewById(R.id.contactPhone);
            avatar = itemView.findViewById(R.id.avatar);
        }
    }
}
