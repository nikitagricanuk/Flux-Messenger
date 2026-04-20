package ru.flux.android;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class GroupsFragment extends Fragment {

    public GroupsFragment() {
        super(R.layout.fragment_groups);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<Group> groups = new ArrayList<>();
        groups.add(new Group("Звёздочки политеха", "7", "января 2024", "https://external-content.duckduckgo.com/iu/?u=http%3A%2F%2Fd.ibtimes.co.uk%2Fen%2Ffull%2F1579833%2Fnasa-discovery-mission.jpg&f=1&nofb=1&ipt=55cb85dbae74225bbccccd522a897a232cc4abd909816decd732b95ab9783a61"));
        groups.add(new Group("ДримТим ИрНИТУ ± ИГУ", "13", "февраля 2023", "https://external-content.duckduckgo.com/iu/?u=http%3A%2F%2Fd.ibtimes.co.uk%2Fen%2Ffull%2F1579833%2Fnasa-discovery-mission.jpg&f=1&nofb=1&ipt=55cb85dbae74225bbccccd522a897a232cc4abd909816decd732b95ab9783a61"));

        RecyclerView recyclerView = view.findViewById(R.id.GroupsRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new GroupsAdapter(groups, group -> {
            // TODO: переход в группу
        }));
    }
}