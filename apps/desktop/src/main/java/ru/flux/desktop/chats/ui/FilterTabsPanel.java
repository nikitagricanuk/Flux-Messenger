package ru.flux.desktop.chats.ui;

import java.awt.FlowLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import ru.flux.desktop.common.ui.FluxTheme;

public class FilterTabsPanel extends JPanel {
    public FilterTabsPanel() {
        setBackground(FluxTheme.APP_BACKGROUND);
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));
        add(new JButton("All"));
        add(new JButton("Chats"));
        add(new JButton("Groups"));
    }
}
