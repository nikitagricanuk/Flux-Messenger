package ru.flux.android.features.chats.ui;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import ru.flux.android.R;
import ru.flux.android.core.data.Contact;
import ru.flux.android.core.data.DisplayItem;
import ru.flux.android.databinding.FragmentNewGroupSetupBinding;
import ru.flux.android.features.chats.ChatsViewModel;
import ru.flux.android.features.chats.ItemListAdapter;

public class NewGroupSetupFragment extends Fragment {

    private FragmentNewGroupSetupBinding binding;
    private ChatsViewModel viewModel;
    private Uri selectedAvatarUri;

    private ActivityResultLauncher<PickVisualMediaRequest> pickMedia;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pickMedia = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri == null) return;
            selectedAvatarUri = uri;
            Glide.with(this).load(uri).circleCrop().into(binding.groupInput.getAvatar());
        });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentNewGroupSetupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(ChatsViewModel.class);

        binding.cancelButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this).popBackStack());

        binding.groupInput.getAvatar().setOnClickListener(v ->
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build()));

        ItemListAdapter adapter = new ItemListAdapter();
        binding.membersRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.membersRecycler.setAdapter(adapter);

        viewModel.getSelectedGroupMembers().observe(getViewLifecycleOwner(), members -> {
            if (members == null) return;
            List<DisplayItem> items = new ArrayList<>();
            for (Contact c : members) {
                items.add(new DisplayItem(c.getName(), c.getPhoneNumber(), c.getProfilePicture(), null));
            }
            adapter.setItems(items);
        });

        binding.confirmButton.setOnClickListener(v -> createGroup());
    }

    private void createGroup() {
        String name = binding.groupInput.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Введите название группы", Toast.LENGTH_SHORT).show();
            return;
        }

        List<Contact> members = viewModel.getSelectedGroupMembers().getValue();
        if (members == null || members.isEmpty()) {
            Toast.makeText(requireContext(), "Выберите участников", Toast.LENGTH_SHORT).show();
            return;
        }

        String myId = viewModel.getCurrentUserId().getValue();
        if (myId == null) return;

        // Build member ID array: current user + all selected contacts
        String[] memberIds = new String[members.size() + 1];
        memberIds[0] = myId;
        for (int i = 0; i < members.size(); i++) {
            memberIds[i + 1] = members.get(i).getId().toString();
        }

        viewModel.createGroupChat(name, memberIds, selectedAvatarUri);
        NavHostFragment.findNavController(this).popBackStack(R.id.chatsFragment, false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}