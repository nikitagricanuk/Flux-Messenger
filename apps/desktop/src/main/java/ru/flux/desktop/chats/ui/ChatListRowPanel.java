package ru.flux.desktop.chats.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import ru.flux.desktop.chats.model.ChatListItemViewModel;
import ru.flux.desktop.common.ui.FluxTheme;

public class ChatListRowPanel extends JPanel {
    public ChatListRowPanel(ChatListItemViewModel chat) {
        setLayout(new BorderLayout(12, 0));
        setBackground(FluxTheme.SURFACE);
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FluxTheme.BORDER),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        add(createBadge(chat.badgeText()), BorderLayout.WEST);
        add(createContent(chat), BorderLayout.CENTER);
        add(createMeta(chat), BorderLayout.EAST);
    }

    private JLabel createBadge(String text) {
        JLabel label = new JLabel(text, JLabel.CENTER);
        label.setPreferredSize(new Dimension(44, 44));
        label.setBorder(BorderFactory.createLineBorder(FluxTheme.BORDER));
        label.setFont(FluxTheme.SMALL_FONT.deriveFont(java.awt.Font.BOLD));
        return label;
    }

    private JPanel createContent(ChatListItemViewModel chat) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 4));
        panel.setBackground(FluxTheme.SURFACE);

        JLabel title = new JLabel(chat.title());
        title.setFont(FluxTheme.BODY_FONT.deriveFont(java.awt.Font.BOLD));

        JLabel subtitle = new JLabel(chat.subtitle());
        subtitle.setFont(FluxTheme.BODY_FONT);
        subtitle.setForeground(FluxTheme.MUTED);

        JLabel preview = new JLabel("Last message preview will appear here");
        preview.setFont(FluxTheme.SMALL_FONT);
        preview.setForeground(FluxTheme.MUTED);

        panel.add(title);
        panel.add(subtitle);
        panel.add(preview);
        return panel;
    }

    private JPanel createMeta(ChatListItemViewModel chat) {
        JPanel panel = new JPanel(new GridLayout(0, 1, 0, 4));
        panel.setBackground(FluxTheme.SURFACE);

        JLabel time = new JLabel("18:05", JLabel.RIGHT);
        time.setFont(FluxTheme.SMALL_FONT);
        time.setForeground(FluxTheme.MUTED);

        JLabel tag = new JLabel(chat.meta(), JLabel.RIGHT);
        tag.setFont(FluxTheme.SMALL_FONT);
        tag.setForeground(FluxTheme.MUTED);

        panel.add(time);
        panel.add(new JLabel(""));
        panel.add(tag);
        return panel;
    }
}
