package ru.flux.android;

import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
        recycler.setAdapter(new NewMessageAdapter(buildPlaceholderItems()));
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

    // ── Placeholder data (replace with real contacts API when available) ──────

    private List<Contact> buildPlaceholderItems() {
        List<Contact> items = new ArrayList<>();

        items.add(new Contact(UUID.randomUUID(), "Анатолий Крутой",  null, "+7-888-888-80-88", null));
        items.add(new Contact(UUID.randomUUID(), "Борис Сафонов",  null, "+7-888-888-80-89", null));
        items.add(new Contact(UUID.randomUUID(), "Володя Сафонов", null, "+7-888-888-80-90", null));
        items.add(new Contact(UUID.randomUUID(), "Евгений Сафонов", null, "+7-888-888-80-91", null));

        return items;
    }
}
