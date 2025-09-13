package notepad;

import java.awt.Color;

import components.MyButton;
import components.MyColors;

public class NotepadButton extends MyButton {
    NotepadButton(String text){
        super(text);
        this.setBackground(MyColors.notepadActive);
        this.setForeground(Color.DARK_GRAY);
        this.hoverBg = new Color(0xF0B650);
    }
}
