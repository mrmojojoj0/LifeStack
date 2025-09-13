// package components;

// import javax.swing.*;
// import java.awt.*;
// import java.awt.event.ActionEvent;
// import java.nio.file.Files;
// import java.nio.file.Path;
// import java.nio.file.Paths;
// import java.io.File;
// import java.io.IOException;

// import MyApp.App;

// public class Login {

//     // private static final Path CREDENTIAL_FILE = Paths.get(System.getProperty("user.home"), ".lifestack_credentials");

//     // private JFrame loginFrame;
//     // private ImageIcon icon;
//     // private JLabel title, userLabel, passLabel;
//     // private JTextField userField;
//     // private JPasswordField passField;
//     // private MyButton loginButton;

//     // public Login() {
//     //     loginFrame = new JFrame("LifeStack - Login");
//     //     icon = new ImageIcon("src/resources/icon48.png");

//     //     title = new JLabel("Login");
//     //     userLabel = new JLabel("\uea8c"); // user icon
//     //     passLabel = new JLabel("\ue72e"); // password icon

//     //     userField = new JTextField(15);
//     //     passField = new JPasswordField(15);

//     //     loginButton = new MyButton("Login");
//     // }

//     // public void showLogin() {
//     //     createFrame();
//     //     createComponents();
//     //     layoutComponents();
//     //     addListeners();

//     //     loginFrame.setVisible(true);
//     // }

//     // private void createFrame() {
//     //     loginFrame.getContentPane().setBackground(MyColors.background);
//     //     loginFrame.setMinimumSize(new Dimension(500, 300));
//     //     loginFrame.setResizable(false);
//     //     loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//     //     loginFrame.setLocationRelativeTo(null);
//     //     loginFrame.setLayout(new GridBagLayout());
//     // }

//     // private void createComponents() {
//     //     // Title styling
//     //     title.setFont(MyFonts.TEXT_FONT_EXTRA_LARGE_BOLD);
//     //     title.setForeground(MyColors.financeActive);
//     //     title.setIcon(icon);
//     //     title.setHorizontalTextPosition(JLabel.CENTER);
//     //     title.setVerticalTextPosition(JLabel.BOTTOM);

//     //     // Labels styling
//     //     userLabel.setFont(MyFonts.ICON_FONT_LARGE);
//     //     passLabel.setFont(MyFonts.ICON_FONT_LARGE);
//     //     userLabel.setForeground(MyColors.financeActive);
//     //     passLabel.setForeground(MyColors.financeActive);

//     //     // Fields styling
//     //     userField.setFont(MyFonts.TEXT_FONT_LARGE_BOLD);
//     //     passField.setFont(MyFonts.TEXT_FONT_LARGE_BOLD);

//     //     // Button styling
//     //     styleButton(loginButton, MyColors.financeActive, Color.WHITE, MyFonts.TEXT_FONT_LARGE_BOLD);
//     // }

//     // private void layoutComponents() {
//     //     GridBagConstraints gbc = new GridBagConstraints();
//     //     gbc.insets = new Insets(5, 5, 5, 5);

//     //     gbc.gridx = 1; gbc.gridy = 0;
//     //     loginFrame.add(title, gbc);

//     //     gbc.gridy = 1; gbc.gridx = 0;
//     //     loginFrame.add(userLabel, gbc);
//     //     gbc.gridx = 1;
//     //     loginFrame.add(userField, gbc);

//     //     gbc.gridx = 0; gbc.gridy = 2;
//     //     loginFrame.add(passLabel, gbc);
//     //     gbc.gridx = 1;
//     //     loginFrame.add(passField, gbc);

//     //     gbc.gridx = 1; gbc.gridy = 3;
//     //     loginFrame.add(loginButton, gbc);
//     // }

//     // private void addListeners() {
//     //     // Enter key navigation
//     //     userField.addActionListener(e -> passField.requestFocus());
//     //     passField.addActionListener(e -> loginButton.doClick());

//     //     // Optional: Up/Down navigation with arrows
//     //     userField.addKeyListener(new java.awt.event.KeyAdapter() {
//     //         public void keyPressed(java.awt.event.KeyEvent evt) {
//     //             if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_DOWN)
//     //                 passField.requestFocus();
//     //         }
//     //     });
//     //     passField.addKeyListener(new java.awt.event.KeyAdapter() {
//     //         public void keyPressed(java.awt.event.KeyEvent evt) {
//     //             if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_UP)
//     //                 userField.requestFocus();
//     //         }
//     //     });

//     //     // Button click
//     //     loginButton.addActionListener(this::loginButtonAction);
//     // }

//     // private void styleButton(MyButton btn, Color bg, Color fg, Font font) {
//     //     btn.setBackground(bg);
//     //     btn.setForeground(fg);
//     //     btn.setFont(font);
//     // }

//     // private void loginButtonAction(ActionEvent e) {
//     //     String username = userField.getText();
//     //     String password = new String(passField.getPassword());

//     //     if (checkCredentials(username, password)) {
//     //         loginFrame.dispose();
//     //         App.start();
//     //     } else {
//     //         showErrorDialog();
//     //     }
//     // }

//     // private boolean checkCredentials(String user, String pass) {
//     //     try {
//     //         if (!Files.exists(CREDENTIAL_FILE)) return false;
//     //         String stored = Files.readString(CREDENTIAL_FILE).trim();
//     //         String[] parts = stored.split(":", 2);
//     //         return parts.length == 2 && parts[0].equals(user) && parts[1].equals(pass);
//     //     } catch (IOException e) {
//     //         e.printStackTrace();
//     //         return false;
//     //     }
//     // }

//     // private void showErrorDialog() {
//     //     JDialog dialog = new JDialog(loginFrame, "Error", true);
//     //     dialog.setLayout(new BorderLayout());

//     //     JLabel message = new JLabel("Invalid credentials!", SwingConstants.CENTER);
//     //     message.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
//     //     dialog.add(message, BorderLayout.CENTER);

//     //     JPanel buttonPanel = new JPanel();
//     //     MyButton retryButton = new MyButton("Retry");
//     //     MyButton cancelButton = new MyButton("Cancel");
//     //     styleButton(retryButton, MyColors.notepadActive, Color.WHITE, MyFonts.TEXT_FONT_LARGE_BOLD);
//     //     styleButton(cancelButton, MyColors.notepadActive, Color.WHITE, MyFonts.TEXT_FONT_LARGE_BOLD);

//     //     retryButton.addActionListener(ev -> dialog.dispose());
//     //     cancelButton.addActionListener(ev -> System.exit(0));

//     //     buttonPanel.add(retryButton);
//     //     buttonPanel.add(cancelButton);
//     //     dialog.add(buttonPanel, BorderLayout.SOUTH);

//     //     dialog.pack();
//     //     dialog.setLocationRelativeTo(loginFrame);
//     //     dialog.setVisible(true);
//     // }
//     public static void main(String[] args) {
//         AuthFrame loginFrame = new AuthFrame() {

//         };
//     }

// }