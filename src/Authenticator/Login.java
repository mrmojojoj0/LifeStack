package Authenticator;

import java.io.IOException;
import java.nio.file.Files;
import javax.swing.*;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;

import MyApp.App;
import components.MyButton;

public class Login extends AuthFrame {

    // Use Encryptor static methods (no instance required)

    public Login() {
        if (!Files.exists(CREDENTIAL_PATH)) {
            SignUp s = new SignUp();
            s.show();
        } else {
            setupFrame("Login");

            this.heading = new JLabel("Login");
            this.heading.setForeground(new Color(30, 144, 255));

            this.USER_FIELD = new JTextField();
            this.PASS_FIELD = new JPasswordField();

            this.actionButton = new MyButton("Login");
            this.actionButton.hoverBg = new Color(30, 144, 255);
            this.actionButton.setBackground(new Color(70, 130, 180));
            this.actionButton.setForeground(Color.white);
            this.actionButton.addActionListener(this::loginAction);

            this.layoutComponents();
            this.show();
        }
    }

    private void loginAction(ActionEvent e) {
        String username = USER_FIELD.getText().trim();
        String password = new String(PASS_FIELD.getPassword()).trim();

        if (checkCredentials(username, password)) {
            frame.dispose();
            App.start();
        } else {
            showErrorDialog();
        }
    }

    private boolean checkCredentials(String user, String pass) {
        try {
            String stored = Files.readString(CREDENTIAL_PATH).trim();
            String[] parts = stored.split("\\R", 2);

            if (parts.length == 2) {
                // Use Encryptor static methods to decrypt
                String storedUser = Encryptor.decrypt(parts[0]);
                String storedPass = Encryptor.decrypt(parts[1]);

                return storedUser.equals(user) && storedPass.equals(pass);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame,
                    "I/O error while reading credentials:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE));
        } catch (Exception ex) {
            // Jasypt decryption or other exception
            ex.printStackTrace();
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame,
                    "Error while decrypting credentials:\n" + ex.toString(), "Error", JOptionPane.ERROR_MESSAGE));
        }
        return false;
    }

    private void showErrorDialog() {
        JDialog dialog = new JDialog(frame, "Error", true);
        dialog.setLayout(new BorderLayout());

        JLabel message = new JLabel("Invalid credentials!", SwingConstants.CENTER);
        message.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        dialog.add(message, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        MyButton retryButton = new MyButton("Retry");
        retryButton.hoverBg = new Color(30, 144, 255);
        retryButton.setBackground(new Color(70, 130, 180));
        retryButton.setForeground(Color.white);

        MyButton cancelButton = new MyButton("Cancel");
        cancelButton.hoverBg = new Color(200, 35, 51);
        cancelButton.setBackground(new Color(220, 53, 69));
        cancelButton.setForeground(Color.white);

        retryButton.addActionListener(e -> dialog.dispose());
        cancelButton.addActionListener(e -> System.exit(0));

        buttonPanel.add(retryButton);
        buttonPanel.add(cancelButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }
}
