package ru.flux.android.features.chats.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import ru.flux.android.core.data.Contact;
import ru.flux.android.core.data.DisplayItem;
import ru.flux.android.databinding.BottomSheetNewGroupSelectMembersBinding;
import ru.flux.android.features.chats.ContactsViewModel;
import ru.flux.android.features.chats.ItemListAdapter;

public class NewGroupSelectMembersFragment extends Fragment {
    private BottomSheetNewGroupSelectMembersBinding binding;
    private ItemListAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetNewGroupSelectMembersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ContactsViewModel viewModel = new ViewModelProvider(requireActivity()).get(ContactsViewModel.class);

        binding.cancelButton.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        binding.okButton.setOnClickListener(v -> onConfirm());

        adapter = new ItemListAdapter();
        adapter.setSelectable(true);
        binding.contactsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.contactsRecycler.setAdapter(adapter);

        binding.searchBar.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.setSearchQuery(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        viewModel.getContacts().observe(getViewLifecycleOwner(), contacts -> {
            List<DisplayItem> items = new ArrayList<>();
            for (var contact : contacts) {
                items.add(new DisplayItem(contact.getName(), contact.getPhoneNumber(), contact.getProfilePicture(), null, contact));
            }
            adapter.setItems(items);
        });

        viewModel.loadContacts();
    }

    private void onConfirm() {
        List<Contact> selected = new ArrayList<>();
        for (DisplayItem item : adapter.getSelected()) {
            if (item.payload instanceof Contact) {
                selected.add((Contact) item.payload);
            }
        }
        // TODO: pass selected contacts to next step (e.g. set group name screen)
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}