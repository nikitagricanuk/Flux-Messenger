package ru.flux.android.features.chats.ui;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import ru.flux.android.R;
import ru.flux.android.core.ui.ErrorDialog;
import ru.flux.android.core.ui.SegmentTabsView;
import ru.flux.android.databinding.FragmentChatsBinding;
import ru.flux.android.features.chats.ChatAdapter;
import ru.flux.android.features.chats.ChatsViewModel;

public class ChatsFragment extends Fragment {

    private SegmentTabsView segmentTabs;
    private ChatAdapter adapter;
    private FragmentChatsBinding binding;
    private boolean isSearchOpen = false;

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

        ChatsViewModel viewModel = new ViewModelProvider(requireActivity()).get(ChatsViewModel.class);

        binding.newChat.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.newMessageBottomSheet));

        segmentTabs = view.findViewById(R.id.segment_tabs);

        RecyclerView chatsRecycler = view.findViewById(R.id.chatsRecycler);
        chatsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChatAdapter();
        adapter.setOnChatActionListener(viewModel::deleteChat);
        chatsRecycler.setAdapter(adapter);

        viewModel.getChats().observe(getViewLifecycleOwner(), adapter::setChats);
        viewModel.getError().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                ErrorDialog.display(getChildFragmentManager(), msg);
                viewModel.clearError();
            }
        });
        viewModel.loadChats();

        String[] filters = {"all", "dm", "group"};
        segmentTabs.setOnTabSelectedListener(index -> {
            if (index < filters.length) adapter.setFilter(filters[index]);
        });
        adapter.setFilter("all");

        binding.searchBar.setScaleX(0f);

        binding.searchIcon.setOnClickListener(v -> openSearch());
        binding.closeSearch.setOnClickListener(v -> closeSearch());

        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.setSearchQuery(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void openSearch() {
        if (isSearchOpen) return;
        isSearchOpen = true;

        binding.searchBar.setPivotX(binding.searchBar.getWidth());
        binding.searchBar.setVisibility(View.VISIBLE);

        binding.searchBar.animate()
                .scaleX(1f)
                .setDuration(300)
                .setInterpolator(new DecelerateInterpolator())
                .start();

        binding.flux.animate()
                .alpha(0f)
                .setDuration(200)
                .start();

        binding.searchIcon.animate()
                .alpha(0f)
                .setDuration(200)
                .withEndAction(() -> binding.searchIcon.setVisibility(View.INVISIBLE))
                .start();

        binding.searchInput.requestFocus();
        InputMethodManager imm = (InputMethodManager)
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(binding.searchInput, InputMethodManager.SHOW_IMPLICIT);
    }

    private void closeSearch() {
        if (!isSearchOpen) return;
        isSearchOpen = false;

        binding.searchBar.animate()
                .scaleX(0f)
                .setDuration(250)
                .setInterpolator(new AccelerateInterpolator())
                .withEndAction(() -> {
                    binding.searchBar.setVisibility(View.INVISIBLE);
                    binding.searchInput.setText("");
                    adapter.setSearchQuery("");
                })
                .start();

        binding.searchIcon.setVisibility(View.VISIBLE);
        binding.searchIcon.animate()
                .alpha(1f)
                .setDuration(200)
                .start();

        binding.flux.animate()
                .alpha(1f)
                .setDuration(200)
                .start();

        InputMethodManager imm = (InputMethodManager)
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(binding.searchInput.getWindowToken(), 0);
    }
}
