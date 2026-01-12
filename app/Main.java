package app;
import ui.*;

/**
 * Hlavní třída pro spuštění hlavního okna
 */
public class Main {
    /**
     * main metoda pro spuštění aplikace
     * @param args defaultní argument
     */
    public static void main(String[] args) {
        javax.swing.SwingUtilities.invokeLater(MainFrame::new);
    }
}