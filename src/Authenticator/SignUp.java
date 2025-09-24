package Authenticator;

import java.awt.event.ActionEvent;
import java.awt.Color;
import javax.swing.*;
import java.nio.file.Files;
import components.MyButton;

public class SignUp extends AuthFrame {

    // Static shared encryptor for SignUp and Login
    private static final Encryptor encryptor = new Encryptor();

    public SignUp() {
        setupFrame("Sign Up");

        this.heading = new JLabel("Sign Up");
        this.heading.setForeground(new Color(30, 144, 255));

        this.USER_FIELD = new JTextField();
        this.PASS_FIELD = new JPasswordField();

        this.actionButton = new MyButton("Sign Up");
        this.actionButton.hoverBg = new Color(30, 144, 255);
        this.actionButton.setBackground(new Color(70, 130, 180));
        this.actionButton.setForeground(Color.white);
        this.actionButton.addActionListener(this::signUpBtnAction);

        this.layoutComponents();
        this.show();
    }

    private void signUpBtnAction(ActionEvent e) {
        String username = USER_FIELD.getText().trim();
        String password = new String(PASS_FIELD.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            showDialog("Error", "Username and password cannot be empty!");
            return;
        }

        try {
            // Use the static shared encryptor
            String credentials = encryptor.encrypt(username) + "\n" + encryptor.encrypt(password);

            Files.createDirectories(CREDENTIAL_PATH.getParent());
            Files.writeString(CREDENTIAL_PATH, credentials);

            showDialog("Success", "Account created successfully!");
            frame.dispose();
            new Login(); // Login will also use static encryptor
        } catch (Exception ex) {
            ex.printStackTrace();
            showDialog("Error", "Failed to save credentials!");
        }
    }

    private void showDialog(String title, String messageText) {
        JDialog dialog = new JDialog(frame, title, true);
        JLabel message = new JLabel(messageText, SwingConstants.CENTER);
        message.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        dialog.add(message);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        dialog.setVisible(true);
    }
}
