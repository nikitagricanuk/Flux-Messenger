package ru.flux.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ChatsFragment extends Fragment {

    private TextView allTab, dmsTab, groupsTab;
    private RecyclerView chatsRecycler;

    // Currently active filter: "all", "dm", or "group"
    private String activeFilter = "all";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        allTab    = view.findViewById(R.id.all_tab);
        dmsTab    = view.findViewById(R.id.dms_tab);
        groupsTab = view.findViewById(R.id.groups_tab);
        chatsRecycler = view.findViewById(R.id.chatsRecycler);

        chatsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        // Tab click listeners — each just changes the active filter
        allTab.setOnClickListener(v -> setActiveTab("all"));
        dmsTab.setOnClickListener(v -> setActiveTab("dm"));
        groupsTab.setOnClickListener(v -> setActiveTab("group"));

        // Start with "all" selected
        setActiveTab("all");
    }

    /**
     * Updates the tab highlight and triggers a list filter.
     * When you have a real adapter, call adapter.setFilter(filter) here.
     */
    private void setActiveTab(String filter) {
        activeFilter = filter;

        // Reset all tabs to no background, then highlight the selected one
        allTab.setBackgroundResource(0);
        dmsTab.setBackgroundResource(0);
        groupsTab.setBackgroundResource(0);

        switch (filter) {
            case "all":   allTab.setBackgroundResource(R.drawable.bg_segment_selected);   break;
            case "dm":    dmsTab.setBackgroundResource(R.drawable.bg_segment_selected);    break;
            case "group": groupsTab.setBackgroundResource(R.drawable.bg_segment_selected); break;
        }

        // TODO: when you add ChatAdapter → adapter.setFilter(filter);
    }
}
