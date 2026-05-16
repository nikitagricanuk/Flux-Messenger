package ru.flux.android.features.chat.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ru.flux.android.R;
import ru.flux.android.features.chat.MediaAdapter;

public class MediaFragment extends Fragment {

    public MediaFragment() {
        super(R.layout.fragment_media);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        List<String> imageUrls = new ArrayList<>();
        imageUrls.add("https://external-content.duckduckgo.com/iu/?u=http%3A%2F%2Fd.ibtimes.co.uk%2Fen%2Ffull%2F1579833%2Fnasa-discovery-mission.jpg&f=1&nofb=1&ipt=55cb85dbae74225bbccccd522a897a232cc4abd909816decd732b95ab9783a61");
        imageUrls.add("https://external-content.duckduckgo.com/iu/?u=http%3A%2F%2Fd.ibtimes.co.uk%2Fen%2Ffull%2F1579833%2Fnasa-discovery-mission.jpg&f=1&nofb=1&ipt=55cb85dbae74225bbccccd522a897a232cc4abd909816decd732b95ab9783a61");
        imageUrls.add("https://external-content.duckduckgo.com/iu/?u=http%3A%2F%2Fd.ibtimes.co.uk%2Fen%2Ffull%2F1579833%2Fnasa-discovery-mission.jpg&f=1&nofb=1&ipt=55cb85dbae74225bbccccd522a897a232cc4abd909816decd732b95ab9783a61");
        imageUrls.add("https://external-content.duckduckgo.com/iu/?u=http%3A%2F%2Fd.ibtimes.co.uk%2Fen%2Ffull%2F1579833%2Fnasa-discovery-mission.jpg&f=1&nofb=1&ipt=55cb85dbae74225bbccccd522a897a232cc4abd909816decd732b95ab9783a61");
        imageUrls.add("https://external-content.duckduckgo.com/iu/?u=http%3A%2F%2Fd.ibtimes.co.uk%2Fen%2Ffull%2F1579833%2Fnasa-discovery-mission.jpg&f=1&nofb=1&ipt=55cb85dbae74225bbccccd522a897a232cc4abd909816decd732b95ab9783a61");
        imageUrls.add("https://external-content.duckduckgo.com/iu/?u=http%3A%2F%2Fd.ibtimes.co.uk%2Fen%2Ffull%2F1579833%2Fnasa-discovery-mission.jpg&f=1&nofb=1&ipt=55cb85dbae74225bbccccd522a897a232cc4abd909816decd732b95ab9783a61");

        RecyclerView recyclerView = view.findViewById(R.id.mediaRecyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        recyclerView.setAdapter(new MediaAdapter(imageUrls, imageUrl -> {
            Bundle args = new Bundle();
            args.putString("imageUrl", imageUrl);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.action_profile_to_imageViewer, args);
        }));
    }
}