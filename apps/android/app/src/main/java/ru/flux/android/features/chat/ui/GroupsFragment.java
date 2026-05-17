package ru.flux.android.features.chat.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ru.flux.android.R;
import ru.flux.android.features.chat.GroupsAdapter;
import ru.flux.android.features.chat.ProfileViewModel;

public class GroupsFragment extends Fragment {

    public GroupsFragment() {
        super(R.layout.fragment_groups);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ProfileViewModel viewModel = new ViewModelProvider(requireParentFragment())
                .get(ProfileViewModel.class);

        GroupsAdapter adapter = new GroupsAdapter(new ArrayList<>(), group -> {
            Bundle args = new Bundle();
            args.putString("chatId", group.id);
            args.putString("chatName", group.name);
            args.putString("chatAvatarUrl", group.avatarUrl);
            args.putBoolean("isGroup", true);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_profile_to_chatFragment, args);
        });

        RecyclerView recyclerView = view.findViewById(R.id.GroupsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        viewModel.getGroups().observe(getViewLifecycleOwner(), adapter::setGroups);
    }
}