package ru.flux.android.features.chat.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ru.flux.android.R;
import ru.flux.android.core.data.Link;
import ru.flux.android.features.chat.LinksAdapter;

public class LinksFragment extends Fragment {

    public LinksFragment() {
        super(R.layout.fragment_links);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<Link> links = new ArrayList<>();
        links.add(new Link("ChatGPT – Cats Discussion", "https://chatgpt.com/share/69959fbda", "https://external-content.duckduckgo.com/iu/?u=http%3A%2F%2Fd.ibtimes.co.uk%2Fen%2Ffull%2F1579833%2Fnasa-discovery-mission.jpg&f=1&nofb=1&ipt=55cb85dbae74225bbccccd522a897a232cc4abd909816decd732b95ab9783a61"));
        links.add(new Link("ChatGPT – Cats Discussion", "https://chatgpt.com/share/69959fbda", "https://external-content.duckduckgo.com/iu/?u=http%3A%2F%2Fd.ibtimes.co.uk%2Fen%2Ffull%2F1579833%2Fnasa-discovery-mission.jpg&f=1&nofb=1&ipt=55cb85dbae74225bbccccd522a897a232cc4abd909816decd732b95ab9783a61"));
        links.add(new Link("ChatGPT – Cats Discussion", "https://chatgpt.com/share/69959fbda", "https://external-content.duckduckgo.com/iu/?u=http%3A%2F%2Fd.ibtimes.co.uk%2Fen%2Ffull%2F1579833%2Fnasa-discovery-mission.jpg&f=1&nofb=1&ipt=55cb85dbae74225bbccccd522a897a232cc4abd909816decd732b95ab9783a61"));
        links.add(new Link("ChatGPT – Cats Discussion", "https://chatgpt.com/share/69959fbda", "https://external-content.duckduckgo.com/iu/?u=http%3A%2F%2Fd.ibtimes.co.uk%2Fen%2Ffull%2F1579833%2Fnasa-discovery-mission.jpg&f=1&nofb=1&ipt=55cb85dbae74225bbccccd522a897a232cc4abd909816decd732b95ab9783a61"));
        links.add(new Link("ChatGPT – Cats Discussion", "https://chatgpt.com/share/69959fbda", "https://external-content.duckduckgo.com/iu/?u=http%3A%2F%2Fd.ibtimes.co.uk%2Fen%2Ffull%2F1579833%2Fnasa-discovery-mission.jpg&f=1&nofb=1&ipt=55cb85dbae74225bbccccd522a897a232cc4abd909816decd732b95ab9783a61"));

        RecyclerView recyclerView = view.findViewById(R.id.linksRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new LinksAdapter(links, link -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link.url));
            startActivity(intent);
        }));
    }
}