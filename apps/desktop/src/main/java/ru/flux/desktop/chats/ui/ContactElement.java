package ru.flux.desktop.chats.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ContactElement {

    public final JPanel ContactBox;

    private final JLabel Avatar;
    private final JLabel Name;
    private final JLabel PhoneNumber;

    private final JPanel TextPanel;
    private final JPanel RightPanel;

    public ContactElement(JLabel avatar, JLabel name, JLabel phoneNumber, JLabel icon1, JLabel icon2) {

        ContactBox = new JPanel(new BorderLayout());

        this.Avatar = avatar;
        this.Name = name;
        this.PhoneNumber = phoneNumber;

        // панель текста (вертикально)
        TextPanel = new JPanel();
        TextPanel.setLayout(new BoxLayout(TextPanel, BoxLayout.Y_AXIS));
        TextPanel.add(Name);
        TextPanel.add(PhoneNumber);

        // панель справа (иконки)
        RightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        RightPanel.add(icon1);
        RightPanel.add(icon2);

        // собираем всё
        ContactBox.add(Avatar, BorderLayout.PAGE_START);
        ContactBox.add(TextPanel, BorderLayout.CENTER);
        ContactBox.add(RightPanel, BorderLayout.PAGE_END);
    }
}