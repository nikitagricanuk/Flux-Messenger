package ru.flux.desktop.chats.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import ru.flux.desktop.chats.api.ChatApiClient;
import ru.flux.desktop.chats.model.ChatListItemMapper;
import ru.flux.desktop.chats.model.ChatListItemViewModel;
import ru.flux.desktop.common.ui.FluxTheme;

public class AllChatsScreen extends JPanel {
    private final ChatApiClient chatApiClient;
    private final JPanel contentPanel;

    public AllChatsScreen(ChatApiClient chatApiClient) {
        this.chatApiClient = chatApiClient;
        this.contentPanel = new JPanel();

        setLayout(new BorderLayout());
        setBackground(FluxTheme.APP_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JScrollPane scrollPane = new JScrollPane(buildContent());
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(FluxTheme.APP_BACKGROUND);
        add(scrollPane, BorderLayout.CENTER);

        refreshChats();
    }

    private JPanel buildContent() {
        JPanel root = new JPanel();
        root.setBackground(FluxTheme.APP_BACKGROUND);
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));

        JPanel header = buildHeader();
        stretchToFullWidth(header);
        root.add(header);
        root.add(Box.createVerticalStrut(16));

        contentPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        root.add(contentPanel);
        root.add(Box.createVerticalStrut(16));

        BottomNavBar bottomNavBar = new BottomNavBar();
        stretchToFullWidth(bottomNavBar);
        root.add(bottomNavBar);

        contentPanel.setBackground(FluxTheme.APP_BACKGROUND);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        return root;
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(FluxTheme.APP_BACKGROUND);

        JLabel title = new JLabel("Flux");
        title.setFont(FluxTheme.TITLE_FONT);
        title.setForeground(FluxTheme.TEXT);

        JButton searchButton = new JButton("Search");

        header.add(title, BorderLayout.WEST);
        header.add(searchButton, BorderLayout.EAST);
        return header;
    }

    private void refreshChats() {
        setLoadingState();
        new SwingWorker<List<ChatListItemViewModel>, Void>() {
            @Override
            protected List<ChatListItemViewModel> doInBackground() throws Exception {
                return ChatListItemMapper.map(chatApiClient.fetchChats());
            }

            @Override
            protected void done() {
                try {
                    showChats(get());
                } catch (Exception exception) {
                    showError(exception.getMessage());
                }
            }
        }.execute();
    }

    private void setLoadingState() {
        contentPanel.removeAll();
        contentPanel.add(createStateLabel("Loading chats..."));
        revalidate();
        repaint();
    }

    private void showChats(List<ChatListItemViewModel> chats) {
        contentPanel.removeAll();
        contentPanel.add(buildSectionTitle("Favorites"));
        contentPanel.add(Box.createVerticalStrut(8));
        FavoritesStrip favoritesStrip = new FavoritesStrip(chats);
        stretchToFullWidth(favoritesStrip);
        contentPanel.add(favoritesStrip);
        contentPanel.add(Box.createVerticalStrut(16));

        FilterTabsPanel tabs = new FilterTabsPanel();
        stretchToFullWidth(tabs);
        contentPanel.add(tabs);
        contentPanel.add(Box.createVerticalStrut(16));

        if (chats.isEmpty()) {
            contentPanel.add(createStateLabel("No chats returned by the API."));
        } else {
            chats.forEach(chat -> {
                ChatListRowPanel row = new ChatListRowPanel(chat);
                row.setAlignmentX(Component.LEFT_ALIGNMENT);
                contentPanel.add(row);
                contentPanel.add(Box.createVerticalStrut(8));
            });
        }
        revalidate();
        repaint();
    }

    private void showError(String message) {
        contentPanel.removeAll();
        contentPanel.add(createStateLabel("Unable to load chats: " + message));
        contentPanel.add(Box.createVerticalStrut(8));
        JButton retryButton = new JButton("Retry");
        retryButton.addActionListener(event -> refreshChats());
        retryButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        contentPanel.add(retryButton);
        revalidate();
        repaint();
    }

    private JLabel buildSectionTitle(String title) {
        JLabel label = new JLabel(title);
        label.setFont(FluxTheme.BODY_FONT.deriveFont(java.awt.Font.BOLD));
        label.setForeground(FluxTheme.TEXT);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private JLabel createStateLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.LEFT);
        label.setFont(FluxTheme.BODY_FONT);
        label.setForeground(FluxTheme.MUTED);
        label.setAlignmentX(Component.LEFT_ALIGNMENT);
        return label;
    }

    private void stretchToFullWidth(JPanel panel) {
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        Dimension preferredSize = panel.getPreferredSize();
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, preferredSize.height));
    }
}
