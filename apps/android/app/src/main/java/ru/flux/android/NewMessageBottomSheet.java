package ru.flux.android;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import ru.flux.android.data.TokenManager;

public class NewMessageBottomSheet extends BottomSheetDialogFragment {

    @Override
    public int getTheme() {
        return R.style.BottomSheetStyle;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_new_message, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.cancelButton).setOnClickListener(v -> dismiss());

        view.findViewById(R.id.newGroupButton).setOnClickListener(v -> {
            // TODO: open new group flow
        });

        view.findViewById(R.id.newContactButton).setOnClickListener(v -> {
            // TODO: open new contact flow
        });

        RecyclerView recycler = view.findViewById(R.id.contactsRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        recycler.setNestedScrollingEnabled(true);

        NewMessageAdapter adapter = new NewMessageAdapter(new ArrayList<>());
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
            TokenManager tokenManager = new TokenManager(requireContext());
            ApiService apiService = ApiClient.getInstance(tokenManager).create(ApiService.class);
            apiService.getMyContacts().enqueue(new Callback<>() {
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
