package ru.flux.android;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NewMessageAdapter extends RecyclerView.Adapter<NewMessageAdapter.ContactViewHolder> {

    private List<Contact> contacts;

    public NewMessageAdapter(List<Contact> contacts) {
        this.contacts = contacts;
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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_new_message_contact, parent, false);
        return new ContactViewHolder(view);
    }

    /**
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contacts.get(position);
        holder.name.setText(contact.name);
        holder.phoneOrEmail.setText(contact.phoneNumber);
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
