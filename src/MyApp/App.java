package MyApp;

import java.awt.*;

import javax.swing.*;

import components.MyColors;
import notepad.NotepadPanel;
import todo.TodoPanel;
import passwordManager.PasswordPanel;
import financeManager.FinancePanel;

public class App {
    public static void start() {
        JFrame mainFrame = new JFrame("");
        // mainFrame.setUndecorated(true);
        mainFrame.setSize(800, 600);
        mainFrame.setLayout(new BorderLayout());

        Object[][] tabs = { { "\ue70b", MyColors.notepadInactive, new NotepadPanel(mainFrame) },
                { "\ueb1d", MyColors.toDoInactive, new TodoPanel(mainFrame) },
                { "\uee7e", MyColors.passwordInactive, new PasswordPanel(mainFrame) },
                { "\ue8c7", MyColors.financeInactive, new FinancePanel(mainFrame) } };

        // JPanel titleBar = new JPanel();
        // JLabel title = new JLabel("");
        ImageIcon titleIcon = new ImageIcon("src/resources/icon48.png");
        mainFrame.setIconImage(titleIcon.getImage());

        // title.setIcon(titleIcon);
        // titleBar.setPreferredSize(new Dimension(800, 48));
        // titleBar.setLayout(null);
        // title.setBounds(5, 5, 500, 48);
        // titleBar.add(title);

        // titleBar.setFont(components.MyFonts.ICON_FONT_LARGE);
        // mainFrame.add(titleBar, BorderLayout.NORTH);

        JTabbedPane mainPanel = new JTabbedPane(JTabbedPane.LEFT);
        mainPanel.setUI(new components.MyTabbedPaneUI());
        mainPanel.setSize(90, 90);
        mainPanel.setFont(components.MyFonts.ICON_FONT_EXTRA_LARGE);

        for (int i = 0; i < tabs.length; i++) {
            String icon = (String) tabs[i][0];
            Color Inactive = (Color) tabs[i][1];

            mainPanel.addTab(icon, (JPanel) tabs[i][2]);
            mainPanel.setBackgroundAt(i, Inactive);
        }
        mainPanel.setBorder(null);

        mainFrame.add(mainPanel, BorderLayout.CENTER);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.getContentPane().setBackground(Color.white);
        mainFrame.setVisible(true);

    }
}
