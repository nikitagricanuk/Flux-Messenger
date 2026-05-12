package ru.flux.android.features.chats.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import ru.flux.android.databinding.BottomSheetNewContactBinding;
import ru.flux.android.features.chats.ContactsViewModel;

public class NewContactFragment extends Fragment {
    private static final String TAG = "NewContactFragment";
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

        ContactsViewModel viewModel = new ViewModelProvider(this).get(ContactsViewModel.class);
        // Handle the back/cancel button within the sheet
        binding.cancelButton.setOnClickListener(v -> {
            getParentFragmentManager().popBackStack();
        });
        binding.okButton.setOnClickListener(v -> {
            String phone = binding.phoneInput.getPhone();
            String firstName = binding.nameInput.getFirstText().toString();
            String lastName = binding.nameInput.getSecondText().toString();
            Log.d(TAG, "okButton: addContact phone=" + phone + ", name=" + firstName + " " + lastName);
            viewModel.addContact(phone, firstName, lastName);
            getParentFragmentManager().popBackStack();
        });
    }
}
