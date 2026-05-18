package ru.flux.android.features.contacts.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ru.flux.android.R;
import ru.flux.android.features.contacts.ContactAdapter;
import ru.flux.android.features.contacts.ContactsViewModel;

public class ContactPage extends Fragment {

    public ContactPage() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contact_page, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ContactsViewModel viewModel = new ViewModelProvider(this).get(ContactsViewModel.class);

        ContactAdapter adapter = new ContactAdapter(new ArrayList<>(),
                viewModel::openOrCreateChat);

        RecyclerView recyclerView = view.findViewById(R.id.contactsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        View shimmerContacts = view.findViewById(R.id.shimmerContacts);
        viewModel.isLoadingContacts().observe(getViewLifecycleOwner(), loading -> {
            shimmerContacts.setVisibility(loading ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        });
        viewModel.getContacts().observe(getViewLifecycleOwner(), adapter::setContacts);

        viewModel.getNavigateToChat().observe(getViewLifecycleOwner(), event -> {
            if (event == null) return;
            Bundle args = new Bundle();
            args.putString("chatId", event.chatId);
            args.putString("chatName", event.chatName);
            args.putString("peerId", event.peerId);
            args.putString("chatAvatarUrl", event.chatAvatarUrl);
            args.putBoolean("isGroup", false);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_contactPage_to_chatFragment, args);
            viewModel.clearNavigateToChat();
        });

        viewModel.loadContacts();
    }
}