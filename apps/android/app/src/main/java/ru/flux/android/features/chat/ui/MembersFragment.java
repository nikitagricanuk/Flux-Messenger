package ru.flux.android.features.chat.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ru.flux.android.R;
import ru.flux.android.features.chat.MembersAdapter;
import ru.flux.android.features.chat.ProfileViewModel;

public class MembersFragment extends Fragment {

    public MembersFragment() {
        super(R.layout.fragment_members);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ProfileViewModel viewModel = new ViewModelProvider(requireParentFragment())
                .get(ProfileViewModel.class);

        MembersAdapter adapter = new MembersAdapter(new ArrayList<>());

        RecyclerView recyclerView = view.findViewById(R.id.membersRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        viewModel.getMembers().observe(getViewLifecycleOwner(), adapter::setMembers);
    }
}