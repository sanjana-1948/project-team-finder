import db.DBConnection;
import gui.LoginFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Main {

    public static void main(String[] args) {
        // Use the normal look of the operating system (optional, looks nicer)
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // If it fails, Java's default look is used. Nothing to do.
        }

        // Check the database first, so the user gets a clear message if MySQL is not ready
        if (!DBConnection.testConnection()) {
            JOptionPane.showMessageDialog(null,
                    "Cannot connect to the MySQL database.\n\n"
                  + "Please check:\n"
                  + "1. MySQL server is running\n"
                  + "2. database.sql has been executed\n"
                  + "3. Username/password in src/db/DBConnection.java are correct\n"
                  + "4. The MySQL connector .jar is inside the lib folder\n\n"
                  + "See the console for the exact error.",
                    "Database Connection Failed", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Swing windows should be created on the Event Dispatch Thread
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}