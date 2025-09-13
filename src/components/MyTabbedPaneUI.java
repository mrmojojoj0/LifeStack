package components;

import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;

public class MyTabbedPaneUI extends BasicTabbedPaneUI {

    @Override
    protected void installDefaults() {
        super.installDefaults();
        tabInsets = new Insets(15, 15, 15, 15); // padding inside tabs
        selectedTabPadInsets = new Insets(0, 0, 0, 0);
        tabAreaInsets = new Insets(10, 0, 15, 0); // spacing around tabs
        contentBorderInsets = new Insets(2, 0, 5, 5); // spacing around content
    }

    @Override
    protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Example: custom border color
        g2.setColor(Color.white);

        // Get bounds of the content area (below the tabs)
        Rectangle bounds = tabPane.getBounds();
        Insets insets = getContentBorderInsets(tabPlacement);

        int x = insets.left;
        int y = insets.top;
        int w = bounds.width - insets.left - insets.right;
        int h = bounds.height - insets.top - insets.bottom;

        // Draw rectangle border
        g2.drawRect(x, y, w - 1, h - 1);

        g2.dispose();
    }

    @Override
    protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title,
            Rectangle textRect, boolean isSelected) {
        Graphics2D g2 = (Graphics2D) g.create();

        if (isSelected) {
            g2.setColor(Color.WHITE);
        } else {
            g2.setColor(Color.DARK_GRAY);
        }

        // Enable anti-aliasing for smooth text
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2.setFont(font);

        // Get actual tab rectangle
        Rectangle tabRect = rects[tabIndex];

        // Center horizontally
        int textWidth = metrics.stringWidth(title);
        int textX = tabRect.x + (tabRect.width - textWidth) / 2;

        // Center vertically
        int textY = tabRect.y + ((tabRect.height - metrics.getHeight()) / 2) + metrics.getAscent();

        g2.drawString(title, textX, textY);

        g2.dispose();
    }

    @Override
    protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h,
            boolean isSelected) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (isSelected) {
            Color[] activeColors = { MyColors.notepadActive, MyColors.toDoActive, MyColors.passwordActive,
                    MyColors.financeActive };
            g2.setColor(activeColors[tabIndex % activeColors.length]);

        } else {
            Color[] inactiveColors = { MyColors.notepadInactive, MyColors.toDoInactive, MyColors.passwordInactive,
                    MyColors.financeInactive };
            g2.setColor(inactiveColors[tabIndex % inactiveColors.length]);
        }

        g2.fillRoundRect(x + 2, y + 2, w - 4, h - 4, 15, 15); // rounded rectangle
        g2.dispose();
    }

    @Override
    protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h,
            boolean isSelected) {
    }

    @Override
    protected void paintFocusIndicator(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex,
            Rectangle iconRect, Rectangle textRect, boolean isSelected) {
    }
}
