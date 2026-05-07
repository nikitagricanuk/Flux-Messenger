package ru.flux.android.features.chats.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import ru.flux.android.core.data.Contact;
import ru.flux.android.core.network.ChatResponse;
import ru.flux.android.core.network.ContactResponse;
import ru.flux.android.core.network.CreateChatRequest;
import ru.flux.android.databinding.BottomSheetNewMessageBinding;
import ru.flux.android.features.chats.NewMessageAdapter;
import ru.flux.android.R;
import ru.flux.android.core.network.ApiClient;

public class NewMessageBottomSheet extends BottomSheetDialogFragment {
    BottomSheetNewMessageBinding binding;

    @Override
    public int getTheme() {
        return R.style.BottomSheetStyle;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomSheetNewMessageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        NavController navController = NavHostFragment.findNavController(this);

        binding.cancelButton.setOnClickListener(v -> dismiss());

        binding.newGroupButton.setOnClickListener(v -> {
            // TODO: open new group flow
        });

        binding.newContactButton.setOnClickListener(v -> {
            navController.navigate(R.id.newContactFragment);
        });

        RecyclerView recycler = binding.contactsRecycler;
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setNestedScrollingEnabled(true);

        NewMessageAdapter adapter = new NewMessageAdapter(new ArrayList<>(), contact -> {
            try {
                ApiClient.api(requireContext()).createChat(new CreateChatRequest(
                        "DIRECT",
                        new String[]{ contact.getId().toString() }
                )).enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<ChatResponse> call,
                                           @NonNull Response<ChatResponse> response) {
                        if (!response.isSuccessful()) {
                            Log.e(TAG, "createChat failed: " + response.code());
                            return;
                        }
                        dismiss();
                    }

                    @Override
                    public void onFailure(@NonNull Call<ChatResponse> call, @NonNull Throwable t) {
                        Log.e(TAG, "createChat error: " + t.getMessage());
                    }
                });
            } catch (GeneralSecurityException | IOException e) {
                Log.e(TAG, "TokenManager init failed: " + e.getMessage());
            }
        });
        recycler.setAdapter(adapter);
        loadContacts(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Expand the sheet to ~90 % of screen height immediately, skip the collapsed state.
        BottomSheetDialog dialog = (BottomSheetDialog) getDialog();
        if (dialog == null) return;

        FrameLayout bottomSheet = dialog.findViewById(
                com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet == null) return;

        int screenHeight = requireActivity().getWindow().getDecorView().getHeight();
        ViewGroup.LayoutParams lp = bottomSheet.getLayoutParams();
        lp.height = (int) (screenHeight * 0.92);
        bottomSheet.setLayoutParams(lp);

        BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setSkipCollapsed(true);
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
    }

    private static final String TAG = "NewMessageBottomSheet";

    private void loadContacts(NewMessageAdapter adapter) {
        try {
            ApiClient.api(requireContext()).getMyContacts().enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<List<ContactResponse>> call,
                                       @NonNull Response<List<ContactResponse>> response) {
                    if (!response.isSuccessful() || response.body() == null) {
                        Log.e(TAG, "getMyContacts failed: " + response.code());
                        return;
                    }
                    List<Contact> contacts = new ArrayList<>();
                    for (ContactResponse cr : response.body()) {
                        contacts.add(new Contact(UUID.fromString(cr.id), cr.name, cr.avatarUrl, cr.contact, null));
                    }
                    adapter.setContacts(contacts);
                }

                @Override
                public void onFailure(@NonNull Call<List<ContactResponse>> call, @NonNull Throwable t) {
                    Log.e(TAG, "getMyContacts error: " + t.getMessage());
                }
            });
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "TokenManager init failed: " + e.getMessage());
        }
    }
}
