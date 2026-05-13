package ru.flux.android.features.chats.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import ru.flux.android.core.data.DisplayItem;
import ru.flux.android.databinding.FragmentListChatsBinding;
import ru.flux.android.features.chats.ItemListAdapter;

public class ChatListFragment extends BottomSheetDialogFragment {

    private FragmentListChatsBinding binding;
    private List<DisplayItem> items;

    public static ChatListFragment newInstance(List<DisplayItem> items) {
        ChatListFragment fragment = new ChatListFragment();
        fragment.items = items;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentListChatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.cancelButton.setOnClickListener(v -> dismiss());

        ItemListAdapter adapter = new ItemListAdapter();
        binding.contactsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.contactsRecycler.setNestedScrollingEnabled(true);
        binding.contactsRecycler.setAdapter(adapter);

        if (items != null) {
            List<DisplayItem> wrapped = new ArrayList<>(); // wrapper since original list is immutable
            for (DisplayItem item : items) {
                wrapped.add(new DisplayItem(item.name, item.subtitle, item.avatarUrl, () -> {
                    item.onClick.run();
                    dismiss();
                }));
            }
            adapter.setItems(wrapped);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}