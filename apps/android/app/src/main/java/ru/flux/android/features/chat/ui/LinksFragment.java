package ru.flux.android.features.chat.ui;

import android.content.Intent;
import android.net.Uri;
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
import ru.flux.android.features.chat.LinksAdapter;
import ru.flux.android.features.chat.ProfileViewModel;

public class LinksFragment extends Fragment {

    public LinksFragment() {
        super(R.layout.fragment_links);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ProfileViewModel viewModel = new ViewModelProvider(requireParentFragment())
                .get(ProfileViewModel.class);

        LinksAdapter adapter = new LinksAdapter(new ArrayList<>(), link -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link.url));
            startActivity(intent);
        });

        RecyclerView recyclerView = view.findViewById(R.id.linksRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        viewModel.getLinks().observe(getViewLifecycleOwner(), adapter::setLinks);
    }
}