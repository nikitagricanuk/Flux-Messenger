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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ru.flux.android.R;
import ru.flux.android.core.data.DisplayItem;
import ru.flux.android.core.ui.ErrorDialog;
import ru.flux.android.databinding.FragmentChatsBinding;
import ru.flux.android.core.data.Chat;
import ru.flux.android.features.chats.ChatAdapter;
import ru.flux.android.features.chats.ChatsViewModel;
import ru.flux.android.features.chats.FavoriteAdapter;

public class ChatsFragment extends Fragment {

    private ChatAdapter adapter;
    private FavoriteAdapter favoriteAdapter;
    private FragmentChatsBinding binding;
    private boolean isSearchOpen = false;
    private String currentUserId = null;

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

        favoriteAdapter = new FavoriteAdapter();

        favoriteAdapter.setOnFavoriteClickListener(chat -> {
            Bundle args = new Bundle();
            args.putString("chatId", chat.id);
            args.putString("chatName", chat.name);
            args.putBoolean("isGroup", "group".equals(chat.type));
            args.putString("chatAvatarUrl", chat.avatarUrl);

            if (chat.memberIds != null && currentUserId != null) {
                for (String memberId : chat.memberIds) {
                    if (!memberId.equals(currentUserId)) {
                        args.putString("peerId", memberId);
                        break;
                    }
                }
            }

            Navigation.findNavController(requireView())
                    .navigate(R.id.action_chatsFragment_to_chatFragment, args);
        });

        binding.favoritesRecycler.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.favoritesRecycler.setAdapter(favoriteAdapter);
        viewModel.getFavorites().observe(getViewLifecycleOwner(), favoriteAdapter::setFavorites);
        viewModel.loadFavorites();

        viewModel.getCurrentUserId().observe(getViewLifecycleOwner(), id -> {
            currentUserId = id;
        });

        binding.chatsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new ChatAdapter();
        adapter.setOnChatActionListener(new ChatAdapter.OnChatActionListener() {
            @Override public void onDeleteChat(Chat chat) { viewModel.deleteChat(chat); }
            @Override public void onAddFavorite(Chat chat) { viewModel.addFavorite(chat); }
            @Override public void onChatClick(Chat chat) {
                Bundle args = new Bundle();
                args.putString("chatId", chat.id);
                args.putString("chatName", chat.name);
                args.putBoolean("isGroup", chat.type.equals("group"));
                args.putString("chatAvatarUrl", chat.avatarUrl);

                if (chat.memberIds != null && currentUserId != null) {
                    for (String memberId : chat.memberIds) {
                        if (!memberId.equals(currentUserId)) {
                            args.putString("peerId", memberId);
                            break;
                        }
                    }
                }

                Navigation.findNavController(requireView())
                        .navigate(R.id.action_chatsFragment_to_chatFragment, args);
            }
        });
        binding.chatsRecycler.setAdapter(adapter);

        viewModel.isLoadingChats().observe(getViewLifecycleOwner(), loading -> {
            binding.shimmerChats.setVisibility(loading ? View.VISIBLE : View.GONE);
            binding.chatsRecycler.setVisibility(loading ? View.INVISIBLE : View.VISIBLE);
        });
        viewModel.getChats().observe(getViewLifecycleOwner(), adapter::setChats);
        viewModel.getError().observe(getViewLifecycleOwner(), msg -> {
            if (msg != null) {
                ErrorDialog.display(getChildFragmentManager(), msg);
                viewModel.clearError();
            }
        });
        viewModel.loadChats();

        String[] filters = {"all", "dm", "group"};
        binding.segmentTabs.setOnTabSelectedListener(index -> {
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

        binding.addFavoriteBtn.setOnClickListener(v -> {
            List<DisplayItem> items = new ArrayList<>();
            for (Chat chat : Objects.requireNonNull(viewModel.getChats().getValue())) {
                items.add(
                        new DisplayItem(chat.name, chat.lastMessage, chat.avatarUrl, () ->
                            viewModel.addFavorite(chat))
                );
            }
            ChatListFragment.newInstance(items).show(getChildFragmentManager(), null);
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
