package ru.flux.android.features.chats.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import ru.flux.android.databinding.BottomSheetNewContactBinding;

public class NewContactFragment extends Fragment {
    BottomSheetNewContactBinding binding;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetNewContactBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Handle the back/cancel button within the sheet
        binding.cancelButton.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });
    }
}
