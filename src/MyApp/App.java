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

        mainFrame.setSize(800, 600);
        mainFrame.setLayout(new BorderLayout());

    // tabs: { iconGlyph, inactiveColor, panel, labelWithNewline }
    Object[][] tabs = { { "\ue70b", MyColors.notepadInactive, new NotepadPanel(mainFrame), "Notepad" },
        { "\ueb1d", MyColors.toDoInactive, new TodoPanel(mainFrame), "Todo\nList" },
        { "\uee7e", MyColors.passwordInactive, new PasswordPanel(mainFrame), "Password\nManager" },
        { "\ue8c7", MyColors.financeInactive, new FinancePanel(mainFrame), "Finance\nManager" } };


        ImageIcon titleIcon = new ImageIcon("src/resources/icon.png");
        mainFrame.setIconImage(titleIcon.getImage());


        JTabbedPane mainPanel = new JTabbedPane(JTabbedPane.LEFT);
        mainPanel.setUI(new components.MyTabbedPaneUI());
        mainPanel.setSize(90, 90);
        mainPanel.setFont(components.MyFonts.ICON_FONT_EXTRA_LARGE);

        for (int i = 0; i < tabs.length; i++) {
            String icon = (String) tabs[i][0];
            Color Inactive = (Color) tabs[i][1];
            JPanel panel = (JPanel) tabs[i][2];
            String label = (String) tabs[i][3];
            String[] labelLines = label.split("\\n");

            mainPanel.addTab(icon, panel);
            mainPanel.setBackgroundAt(i, Inactive);

            // Create a custom tab component: icon on top, two-line label underneath
            JPanel tabComp = new JPanel();
            tabComp.setOpaque(false);
            tabComp.setLayout(new BoxLayout(tabComp, BoxLayout.Y_AXIS));
            // increase left/right padding so tabs appear more square
            // tabComp.setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
            // give a preferred size to encourage square-ish tabs
            tabComp.setPreferredSize(new Dimension(70, 70));

            JLabel iconLabel = new JLabel(icon);
            iconLabel.setFont(components.MyFonts.ICON_FONT_EXTRA_LARGE);
            iconLabel.setForeground(Color.DARK_GRAY);
            iconLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel line1 = new JLabel(labelLines.length > 0 ? labelLines[0] : "");
            line1.setFont(components.MyFonts.TEXT_FONT_BOLD);
            line1.setForeground(Color.DARK_GRAY);
            line1.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel line2 = new JLabel(labelLines.length > 1 ? labelLines[1] : "");
            line2.setFont(components.MyFonts.TEXT_FONT);
            line2.setForeground(Color.DARK_GRAY);
            line2.setAlignmentX(Component.CENTER_ALIGNMENT);

            tabComp.add(iconLabel);
            tabComp.add(line1);
            tabComp.add(line2);

            mainPanel.setTabComponentAt(i, tabComp);
        }
        mainPanel.setBorder(null);

        mainFrame.add(mainPanel, BorderLayout.CENTER);
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setLocationRelativeTo(null);
        mainFrame.getContentPane().setBackground(Color.white);
        mainFrame.setVisible(true);

    }
}
