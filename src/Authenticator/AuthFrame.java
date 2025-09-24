package Authenticator;

import components.MyButton;
import components.MyFonts;

import javax.swing.*;

import java.awt.*;

public abstract class AuthFrame {
    protected static final java.nio.file.Path CREDENTIAL_PATH = java.nio.file.Paths.get(System.getProperty("user.home"),
            "/lifestack", ".lifestack_credentials");

    protected JFrame frame;

    protected ImageIcon logoIcon = new ImageIcon("src/resources/logo-48.png");
    protected ImageIcon icon = new ImageIcon("src/resources/icon.png");

    protected static final JLabel USER_LABEL = new JLabel("\uea8c");
    protected JTextField USER_FIELD;
    protected static final JLabel PASS_LABEL = new JLabel("\ue72e");
    protected JPasswordField PASS_FIELD;
    protected MyButton actionButton;
    protected JLabel heading;

    protected void setupFrame(String frameTitle) {
        USER_LABEL.setForeground(new Color(30, 144, 255));
        PASS_LABEL.setForeground(new Color(30, 144, 255));
        frame = new JFrame(frameTitle);
        frame.getContentPane().setBackground(new Color(245, 248, 255));
        frame.setMinimumSize(new Dimension(500, 300));
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new GridBagLayout());
        frame.setIconImage(icon.getImage());
    }

    protected void styleButton(MyButton btn, Color bg, Color fg, Font font) {
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFont(font);
    }

    protected void layoutComponents() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);

        heading.setHorizontalTextPosition(SwingConstants.CENTER);
        heading.setVerticalTextPosition(SwingConstants.BOTTOM);
        heading.setIcon(logoIcon);
        heading.setFont(MyFonts.TEXT_FONT_EXTRA_LARGE_BOLD);
        USER_LABEL.setFont(MyFonts.ICON_FONT_EXTRA_LARGE);
        PASS_LABEL.setFont(MyFonts.ICON_FONT_EXTRA_LARGE);
        USER_FIELD.setPreferredSize(new Dimension(200, 35));
        PASS_FIELD.setPreferredSize(new Dimension(200, 35));

        USER_FIELD.setFont(MyFonts.TEXT_FONT_LARGE_BOLD);
        USER_FIELD.setFont(MyFonts.TEXT_FONT_LARGE_BOLD);

        gbc.gridx = 1;
        gbc.gridy = 0;
        frame.add(heading, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        frame.add(USER_LABEL, gbc);
        gbc.gridx = 1;
        frame.add(USER_FIELD, gbc);
        gbc.gridx = 0;
        gbc.gridy = 2;
        frame.add(PASS_LABEL, gbc);
        gbc.gridx = 1;
        frame.add(PASS_FIELD, gbc);
        gbc.gridx = 1;
        gbc.gridy = 3;
        frame.add(actionButton, gbc);
    }

    public void show() {
        frame.setVisible(true);
    }
}
