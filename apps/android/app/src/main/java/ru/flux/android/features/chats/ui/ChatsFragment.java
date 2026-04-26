package ru.flux.android.features.chats.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ru.flux.android.R;
import ru.flux.android.core.ui.SegmentTabsView;
import ru.flux.android.databinding.FragmentChatsBinding;
import ru.flux.android.features.chats.ChatAdapter;
import ru.flux.android.features.chats.ChatsViewModel;

public class ChatsFragment extends Fragment {

    private SegmentTabsView segmentTabs;
    private ChatAdapter adapter;
    private FragmentChatsBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentChatsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ChatsViewModel viewModel = new ViewModelProvider(this).get(ChatsViewModel.class);

        binding.newChat.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.newMessageBottomSheet));

        segmentTabs = view.findViewById(R.id.segment_tabs);

        RecyclerView chatsRecycler = view.findViewById(R.id.chatsRecycler);
        chatsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChatAdapter();
        chatsRecycler.setAdapter(adapter);

        viewModel.getChats().observe(getViewLifecycleOwner(), adapter::setChats);

        viewModel.loadChats();

        String[] filters = {"all", "dm", "group"};
        segmentTabs.setOnTabSelectedListener(index -> {
            if (index < filters.length) adapter.setFilter(filters[index]);
        });
        adapter.setFilter("all");
    }
}
