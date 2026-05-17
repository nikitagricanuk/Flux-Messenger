package ru.flux.android.features.chat.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import ru.flux.android.MainActivity;
import ru.flux.android.R;
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