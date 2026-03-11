package ru.flux.desktop.chats.ui;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ContactElement {
    final JPanel ContactBox;
    private final JPanel ContentPanel;
    private final JLabel Avatar;
    private final JLabel Name;
    private final JLabel PhoneNumber;

    public ContactElement(JLabel avatar, JLabel name, JLabel phoneNumber) {
        ContactBox = new JPanel();                            
        ContactBox.setLayout(new BoxLayout(ContactBox, BoxLayout.X_AXIS));
        ContentPanel = new JPanel();
        ContentPanel.setLayout(new BoxLayout(ContentPanel, BoxLayout.Y_AXIS));

        this.Avatar = avatar;
        this.Name = name;
        this.PhoneNumber = phoneNumber;

        ContentPanel.add(Name);
        ContentPanel.add(PhoneNumber);

        ContactBox.add(Avatar);
        ContactBox.add(ContentPanel);
    }
    
}
