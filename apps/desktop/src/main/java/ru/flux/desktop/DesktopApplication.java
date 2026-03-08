package ru.flux.desktop;

import javax.swing.SwingUtilities;
import ru.flux.desktop.app.FluxDesktopApp;

public class DesktopApplication {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FluxDesktopApp().start());
    }
}
