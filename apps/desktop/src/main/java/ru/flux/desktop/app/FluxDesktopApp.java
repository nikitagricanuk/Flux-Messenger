package ru.flux.desktop.app;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.JFrame;
import javax.swing.JPanel;
import ru.flux.desktop.chats.api.ChatApiClient;
import ru.flux.desktop.chats.ui.AllChatsScreen;

public class FluxDesktopApp {
    private final JFrame frame;
    private final JPanel screenContainer;
    private final CardLayout screenLayout;
    private final Map<String, JPanel> screens;

    public FluxDesktopApp() {
        this.frame = new JFrame("Flux Desktop");
        this.screenLayout = new CardLayout();
        this.screenContainer = new JPanel(screenLayout);
        this.screens = new LinkedHashMap<>();
    }

    public void start() {
        configureFrame();
        registerScreens();
        showScreen(ScreenId.ALL_CHATS.id());
        frame.setVisible(true);
    }

    private void configureFrame() {
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(420, 860);
        frame.setMinimumSize(frame.getSize());
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());
        frame.setContentPane(screenContainer);
    }

    private void registerScreens() {
        registerScreen(ScreenId.ALL_CHATS.id(), new AllChatsScreen(new ChatApiClient()));
    }

    private void registerScreen(String id, JPanel screen) {
        screens.put(id, screen);
        screenContainer.add(screen, id);
    }

    private void showScreen(String id) {
        screenLayout.show(screenContainer, id);
    }
}
