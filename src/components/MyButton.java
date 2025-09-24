
// Inheritance:  gives the ability to extend JButton. 
// Also gives the abilty to downcast graphics object 
// to its child class Graphics2D.
// Polymorphism: is applied when the mthods are overriden to change its behavior.

package components;

import javax.swing.JButton;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.Graphics2D;
import java.awt.Insets;

public class MyButton extends JButton {
    public Color hoverBg;
    public Color bordeColor;
    public Color hoverBorder;

    public MyButton(String text) {
        super(text);
        this.setLayout(null);
        this.setFont(MyFonts.TEXT_FONT_BOLD);
        this.setFocusPainted(false);
        this.setFocusable(false);
        this.setMargin(new Insets(0, 0, 0, 0));
        this.setForeground(Color.white);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBorderPainted(false);
                Color temp = getBackground();
                setBackground(hoverBg);
                hoverBg = temp;
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBorderPainted(false);

                Color temp = getBackground();
                setBackground(hoverBg);
                hoverBg = temp;
            }
        });

    }

    @Override
    public void paintBorder(Graphics g) {
        Graphics2D g2d = (Graphics2D) (g); // Downcasting: Superclass to Subclass (explicit cast needed)
        setBorderPainted(false);

        super.paintBorder(g2d);

        
    }
    
}
