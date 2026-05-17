package ru.flux.android.features.chat.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import ru.flux.android.R;
import ru.flux.android.features.chat.MediaAdapter;
import ru.flux.android.features.chat.ProfileViewModel;

public class MediaFragment extends Fragment {

    public MediaFragment() {
        super(R.layout.fragment_media);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ProfileViewModel viewModel = new ViewModelProvider(requireParentFragment())
                .get(ProfileViewModel.class);

        MediaAdapter adapter = new MediaAdapter(new ArrayList<>(), imageUrl -> {
            Bundle args = new Bundle();
            args.putString("imageUrl", imageUrl);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_profile_to_imageViewer, args);
        });

        RecyclerView recyclerView = view.findViewById(R.id.mediaRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setAdapter(adapter);

        viewModel.getMediaUrls().observe(getViewLifecycleOwner(), adapter::updateImages);
    }
}