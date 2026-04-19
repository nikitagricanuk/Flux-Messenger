package ru.flux.android;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import eightbitlab.com.blurview.BlurView;

public class MainActivity extends AppCompatActivity {

    private FrameLayout activeTab;
    private NavController navController;

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

        // Insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        // NavController из NavHostFragment
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        navController = navHostFragment.getNavController();

        // Табы
        FrameLayout navChats = findViewById(R.id.nav_chats);
        FrameLayout navContacts = findViewById(R.id.nav_contacts);
        FrameLayout navSettings = findViewById(R.id.nav_settings);

        // Активный таб по умолчанию
        setActiveTab(navChats);

        navChats.setOnClickListener(v -> {
            setActiveTab(navChats);
            navController.navigate(R.id.chatsFragment);
        });

        navContacts.setOnClickListener(v -> {
            setActiveTab(navContacts);
            navController.navigate(R.id.contactPage);
        });

//        navSettings.setOnClickListener(v -> {
//            setActiveTab(navSettings);
//            navController.navigate(R.id.settingsFragment);
//        });
    }

    private void setActiveTab(FrameLayout selected) {
        if (activeTab != null) {
            activeTab.setBackgroundResource(0);
        }
        selected.setBackgroundResource(R.drawable.bg_selected_tab);
        activeTab = selected;
    }
}