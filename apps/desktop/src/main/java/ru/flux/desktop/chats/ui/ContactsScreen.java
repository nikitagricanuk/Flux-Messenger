package ru.flux.desktop.chats.ui;

import java.awt.FlowLayout;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ContactsScreen {
    
    public final JPanel ContentPanel;
    private final JPanel Header;
    private final JLabel Title;
    private final JButton SearchButton;
   

    public ContactsScreen() {

        Header = new JPanel();
        Header.setLayout(new BoxLayout(Header, BoxLayout.X_AXIS));
        Title = new JLabel("Контакты");
        SearchButton = new JButton("Поиск");

        Header.add(Title);
        Header.add(SearchButton);

        ContentPanel = new JPanel();
        ContentPanel.setLayout(new BoxLayout(ContentPanel, BoxLayout.Y_AXIS));

        ContentPanel.add(Header);

        for (int i = 0; i < 10; i++) {
            ContactElement contactElement = new ContactElement(
                    new JLabel("Avatar " + i),
                    new JLabel("Петров Петр " + i),
                    new JLabel("+79999999999 ")
            );
            ContentPanel.add(contactElement.ContactBox);
        }
        ContentPanel.add(new BottomNavBar());
    }

}
