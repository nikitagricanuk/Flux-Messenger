package ru.flux.android;

import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

public class MainActivity extends AppCompatActivity {

    private FrameLayout activeTab;
    private NavController navController;

    public void setNavBarVisible(boolean visible) {
        findViewById(R.id.blurNav).setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_main);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        controller.hide(WindowInsetsCompat.Type.navigationBars());
        controller.setSystemBarsBehavior(
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        );

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        navController = navHostFragment.getNavController();

        navController.addOnDestinationChangedListener((ctrl, destination, arguments) -> {
            if (destination.getId() == R.id.chatFragment
                    || destination.getId() == R.id.imageViewerFragment
                    || destination.getId() == R.id.profileFragment) {
                setNavBarVisible(false);
            } else {
                setNavBarVisible(true);
            }
        });

        FrameLayout navChats = findViewById(R.id.nav_chats);
        FrameLayout navContacts = findViewById(R.id.nav_contacts);
        FrameLayout navSettings = findViewById(R.id.nav_settings);

        setActiveTab(navChats);

        navChats.setOnClickListener(v -> {
            setActiveTab(navChats);
            navController.navigate(R.id.chatsFragment);
        });

        navContacts.setOnClickListener(v -> {
            setActiveTab(navContacts);
            navController.navigate(R.id.contactPage);
        });

        navSettings.setOnClickListener(v -> {
            setActiveTab(navSettings);
            navController.navigate(R.id.settingsFragment);
        });
    }

    private void setActiveTab(FrameLayout selected) {
        if (activeTab != null) {
            activeTab.setBackgroundResource(0);
        }
        selected.setBackgroundResource(R.drawable.bg_selected_tab);
        activeTab = selected;
    }
}