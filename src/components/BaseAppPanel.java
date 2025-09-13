package components;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public abstract class BaseAppPanel extends JPanel {
    protected JPanel topPanel;
    protected JPanel mainBody;

    public BaseAppPanel(Color backgroundColor) {
        this.setLayout(new BorderLayout());
        this.setBackground(backgroundColor);

        // Rounded border that looks good on green/orange/blue/red
        this.setBorder(new LineBorder(new Color(50, 50, 50), 2, true));

        // --- Top Panel ---
        topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        topPanel.setOpaque(false); // inherit parent bg
        topPanel.setBorder(new MatteBorder(0, 0, 1, 0, new Color(80, 80, 80))); // bottom line

        // --- Main Body ---
        mainBody = new JPanel(new BorderLayout());
        mainBody.setOpaque(false);
        mainBody.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Add to layout
        this.add(topPanel, BorderLayout.NORTH);
        this.add(mainBody, BorderLayout.CENTER);
    }

    /**
     * Child classes must implement this to set up their UI
     */
    protected abstract void buildUI();
}
