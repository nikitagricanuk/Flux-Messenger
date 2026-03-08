package ru.flux.desktop.chats.ui;

import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import ru.flux.desktop.common.ui.FluxTheme;

public class BottomNavBar extends JPanel {
    public BottomNavBar() {
        setBackground(FluxTheme.APP_BACKGROUND);
        setLayout(new FlowLayout(FlowLayout.CENTER, 12, 0));
        add(new JButton("Contacts"));
        add(new JButton("Chats"));
        add(new JButton("Settings"));
    }
}
