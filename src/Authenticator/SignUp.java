package Authenticator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Color;
import components.MyButton;
import javax.swing.*;

public class SignUp extends AuthFrame {
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
        this.actionButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                signUpBtnAction();
            }
        });
        this.layoutComponents();
        this.show();

    }

    private void signUpBtnAction() {
        String username = USER_FIELD.getText().trim();
        String password = new String(PASS_FIELD.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {

            JDialog dialog = new JDialog(frame, "Error", true);
            JLabel message = new JLabel("Username and password cannot be empty!", SwingConstants.CENTER);
            message.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            dialog.add(message);
            dialog.pack();
            dialog.setLocationRelativeTo(frame);
            dialog.setVisible(true);
            return;
        }

        try {

            String credentials = username + "\n" + password;

            java.nio.file.Files.createDirectories(CREDENTIAL_PATH.getParent());

            java.nio.file.Files.writeString(CREDENTIAL_PATH, credentials);

            JDialog dialog = new JDialog(frame, "Success", true);
            JLabel message = new JLabel("Account created successfully!", SwingConstants.CENTER);
            message.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            dialog.add(message);
            dialog.pack();
            dialog.setLocationRelativeTo(frame);
            dialog.setVisible(true);
            frame.dispose();
            new Login();

        } catch (Exception e) {
            e.printStackTrace();
            JDialog dialog = new JDialog(frame, "Error", true);
            JLabel message = new JLabel("Failed to save credentials!", SwingConstants.CENTER);
            message.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
            dialog.add(message);
            dialog.pack();
            dialog.setLocationRelativeTo(frame);
            dialog.setVisible(true);
        }
    }

}
