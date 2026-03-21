package ru.flux.desktop.chats.ui;

import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ru.flux.desktop.chats.api.ContactsApiClient;
import ru.flux.desktop.common.ui.FluxTheme;

public class ContactsScreen {
    
    public final JPanel ContentPanel;
    private final JPanel Header;
    private final JLabel Title;
    private final JButton SearchButton;
    private final FlowLayout HeaderLayout;

    public ContactsScreen() {

        Header = new JPanel();
        Header.setLayout(new BoxLayout(Header, BoxLayout.X_AXIS));
        Title = new JLabel("Контакты");
        Title.setFont(FluxTheme.TITLE_FONT);
        SearchButton = new JButton(new ImageIcon("apps\\desktop\\src\\main\\java\\ru\\flux\\desktop\\images\\SearchIcon.png"));

        HeaderLayout = new FlowLayout(FlowLayout.CENTER, 110, 0);
        Header.setLayout(HeaderLayout);

        Header.add(Title);
        Header.add(SearchButton);

        ContentPanel = new JPanel();
        ContentPanel.setLayout(new BoxLayout(ContentPanel, BoxLayout.Y_AXIS));

        ContentPanel.add(Header);

        //TODO переделать на получение данных от API
        ContactsApiClient contactsApiClient = new ContactsApiClient();
        contactsApiClient.getContacts();
        for (int i = 0; i < 5; i++) {
            ContactElement contactElement = new ContactElement(
                    new JLabel("Avatar " + i),
                    new JLabel("Петров Петр " + i),
                    new JLabel("+79999999999 "),
                    new JLabel("Icon1"),
                    new JLabel("Icon2")
            );
            ContentPanel.add(contactElement.ContactBox);
        }
        ContentPanel.add(new BottomNavBar());
    }

}
