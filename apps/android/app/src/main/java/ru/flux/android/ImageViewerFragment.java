package ru.flux.android;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;

public class ImageViewerFragment extends Fragment {

    public ImageViewerFragment() {
        super(R.layout.fragment_image_viewer);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity) requireActivity()).setNavBarVisible(false);

        String imageUrl = getArguments() != null
                ? getArguments().getString("imageUrl") : "";

        ImageView fullImage = view.findViewById(R.id.fullImage);

        Glide.with(this)
                .load(imageUrl)
                .into(fullImage);

        view.findViewById(R.id.btnClose).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );

        view.findViewById(R.id.fullImage).setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack()
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        ((MainActivity) requireActivity()).setNavBarVisible(true);
    }
}