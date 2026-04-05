package ru.flux.android;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import eightbitlab.com.blurview.BlurView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Blur setup
        BlurView blurNav = findViewById(R.id.blurNav);
        BlurView selectedTabBlur = findViewById(R.id.selectedTabBlur);
        ViewGroup rootView = findViewById(android.R.id.content);
        Drawable windowBackground = getWindow().getDecorView().getBackground();

        blurNav.setupWith(rootView)
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(15f);

        selectedTabBlur.setupWith(rootView)
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(12f);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load ChatsFragment on first launch only (not after rotation)
        if (savedInstanceState == null) {
            navigateTo(new ChatsFragment());
        }

        // Navbar click listeners
        findViewById(R.id.nav_chats).setOnClickListener(v -> navigateTo(new ChatsFragment()));

        // TODO: replace with real fragments when you create them
        // findViewById(R.id.nav_contacts).setOnClickListener(v -> navigateTo(new ContactsFragment()));
        // findViewById(R.id.nav_settings).setOnClickListener(v -> navigateTo(new SettingsFragment()));
    }

    private void navigateTo(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}
