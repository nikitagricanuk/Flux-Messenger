package ru.flux.android.features.chats.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;

import ru.flux.android.R;
import ru.flux.android.core.data.DisplayItem;
import ru.flux.android.databinding.BottomSheetNewMessageBinding;
import ru.flux.android.features.chats.ChatsViewModel;
import ru.flux.android.features.chats.ContactsViewModel;
import ru.flux.android.features.chats.ItemListAdapter;

public class NewMessageBottomSheet extends BottomSheetDialogFragment {
    private BottomSheetNewMessageBinding binding;

    @Override
    public int getTheme() {
        return R.style.BottomSheetStyle;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomSheetNewMessageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = NavHostFragment.findNavController(this);
        ChatsViewModel chatsViewModel = new ViewModelProvider(requireActivity()).get(ChatsViewModel.class);
        ContactsViewModel contactsViewModel = new ViewModelProvider(requireActivity()).get(ContactsViewModel.class);

        binding.cancelButton.setOnClickListener(v -> dismiss());
        binding.newGroupButton.setOnClickListener(v -> { /* TODO: open new group flow */ });
        binding.newContactButton.setOnClickListener(v -> navController.navigate(R.id.newContactFragment));

        ItemListAdapter adapter = new ItemListAdapter();
        binding.contactsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.contactsRecycler.setNestedScrollingEnabled(true);
        binding.contactsRecycler.setAdapter(adapter);

        contactsViewModel.getContacts().observe(getViewLifecycleOwner(), contacts -> {
            List<DisplayItem> items = new ArrayList<>();
            for (var contact : contacts) {
                items.add(new DisplayItem(contact.getName(), contact.getPhoneNumber(), contact.getProfilePicture(), () -> {
                    String myId = chatsViewModel.getCurrentUserId().getValue();
                    if (myId == null) return;
                    chatsViewModel.createChat(null, "DIRECT", new String[]{myId, contact.getId().toString()});
                    dismiss();
                }));
            }
            adapter.setItems(items);
        });

        contactsViewModel.loadContacts();
    }

    @Override
    public void onStart() {
        super.onStart();
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog == null) return;

        FrameLayout bottomSheet = dialog.findViewById(
                com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet == null) return;

        int screenHeight = requireActivity().getWindow().getDecorView().getHeight();
        ViewGroup.LayoutParams lp = bottomSheet.getLayoutParams();
        lp.height = (int) (screenHeight * 0.92);
        bottomSheet.setLayoutParams(lp);

        BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setSkipCollapsed(true);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}