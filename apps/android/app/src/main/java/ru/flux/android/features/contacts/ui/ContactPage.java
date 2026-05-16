package ru.flux.android.features.contacts.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ru.flux.android.R;
import ru.flux.android.core.data.Contact;
import ru.flux.android.features.contacts.ContactAdapter;

public class ContactPage extends Fragment {

    public ContactPage() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contact_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<Contact> contacts = new ArrayList<>();
        contacts.add(new Contact(UUID.randomUUID(), "Евгений Сафонов", "", "+89149149144", "email"));
        contacts.add(new Contact(UUID.randomUUID(), "Евгений Сафонов", "", "+89149149144", "email"));
        contacts.add(new Contact(UUID.randomUUID(), "Евгений Сафонов", "", "+89149149144", "email"));
        contacts.add(new Contact(UUID.randomUUID(), "Евгений Сафонов", "", "+89149149144", "email"));

        RecyclerView recyclerView = view.findViewById(R.id.contactsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        recyclerView.setAdapter(new ContactAdapter(contacts, contact -> {
            Bundle args = new Bundle();
            args.putString("chatName", contact.getName());
            args.putBoolean("isGroup", false);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_contactPage_to_profileFragment, args);
        }));
    }
}