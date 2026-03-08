package ru.flux.desktop.chats.ui;

import java.awt.FlowLayout;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import ru.flux.desktop.chats.model.ChatListItemViewModel;
import ru.flux.desktop.common.ui.FluxTheme;

public class FavoritesStrip extends JPanel {
    public FavoritesStrip(List<ChatListItemViewModel> chats) {
        setBackground(FluxTheme.APP_BACKGROUND);
        setLayout(new FlowLayout(FlowLayout.LEFT, 8, 0));

        add(createTag("Add"));
        chats.stream().limit(4).forEach(chat -> add(createTag(chat.title())));
    }

    private JLabel createTag(String text) {
        String value = text.length() > 14 ? text.substring(0, 14) + "..." : text;
        JLabel label = new JLabel(value);
        label.setFont(FluxTheme.SMALL_FONT);
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(FluxTheme.BORDER),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return label;
    }
}
