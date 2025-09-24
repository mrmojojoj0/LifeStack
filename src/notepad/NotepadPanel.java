package notepad;

import components.BaseAppPanel;
import components.MyColors;
import components.MyFonts;

import java.awt.*;
import java.io.*;
import javax.swing.*;
import javax.swing.border.LineBorder;
import components.MyButton;

class NotepadButton extends MyButton {
    NotepadButton(String text) {
        super(text);
        this.setBackground(MyColors.notepadActive);
        this.hoverBg = new Color(0xF0B650);
    }
}

public class NotepadPanel extends BaseAppPanel {
    private JTextArea textArea;
    private JFrame parentFrame;
    private JLabel logLabel;
    private JLabel fileNameLabel;
    private File currentFile;
    private int lastFindIndex = 0;
    private JTextPane findText;
    private JTextPane replaceText;

    public NotepadPanel(JFrame parentFrame) {
        super(MyColors.notepadInactive);
        this.parentFrame = parentFrame;
        buildUI();
    }

    @Override
    protected void buildUI() {
        // === Toolbar Buttons ===
        NotepadButton newBtn = new NotepadButton("New");
        NotepadButton openBtn = new NotepadButton("Open");
        NotepadButton saveBtn = new NotepadButton("Save");
        NotepadButton saveAsBtn = new NotepadButton("Save As");
        NotepadButton findBtn = new NotepadButton("Find");
        NotepadButton replaceBtn = new NotepadButton("Replace");
        NotepadButton replaceAllBtn = new NotepadButton("Replace All");
        findText = new JTextPane();
        replaceText = new JTextPane();

        findText.setPreferredSize(new Dimension(100, 25));
        findText.setFont(MyFonts.TEXT_FONT_BOLD);
        findText.setBorder(new LineBorder(Color.GRAY, 1));

        replaceText.setPreferredSize(new Dimension(100, 25));
        replaceText.setFont(MyFonts.TEXT_FONT_BOLD);
        replaceText.setBorder(new LineBorder(Color.GRAY, 1));

        // Add actions
        addNewAction(newBtn);
        addOpenAction(openBtn);
        addSaveAction(saveBtn);
        addSaveAsAction(saveAsBtn);
        addFindAction(findBtn);
        addReplaceAction(replaceBtn);
        addReplaceAllAction(replaceAllBtn);

        // Top panel layout
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(newBtn);
        buttonsPanel.add(openBtn);
        buttonsPanel.add(saveBtn);
        buttonsPanel.add(saveAsBtn);
        buttonsPanel.add(findText);
        buttonsPanel.add(findBtn);
        buttonsPanel.add(replaceText);
        buttonsPanel.add(replaceBtn);
        buttonsPanel.add(replaceAllBtn);

        topPanel.setLayout(new BorderLayout());
        topPanel.add(buttonsPanel, BorderLayout.NORTH);

        // Info panel (filename + log messages)
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setOpaque(false);
        fileNameLabel = new JLabel("No file opened");
        fileNameLabel.setFont(MyFonts.TEXT_FONT_BOLD);
        logLabel = new JLabel(" ");
        logLabel.setFont(MyFonts.TEXT_FONT_BOLD);
        logLabel.setForeground(Color.GRAY);

        infoPanel.add(fileNameLabel, BorderLayout.WEST);
        infoPanel.add(logLabel, BorderLayout.EAST);

        topPanel.add(infoPanel, BorderLayout.SOUTH);

        // === Text area ===
        textArea = new JTextArea("A Quick Brown Fox Jumps Over The Lazy Dog.");
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(MyFonts.TEXT_FONT_LARGE);

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setBorder(new LineBorder(new Color(80, 80, 80), 1, true));

        mainBody.add(scrollPane, BorderLayout.CENTER);
    }

    // === ACTIONS ===

    private void addNewAction(NotepadButton newBtn) {
        newBtn.addActionListener(e -> {
            textArea.setText("");
            currentFile = null;
            fileNameLabel.setText("New File");
            logLabel.setText("New file created");

        });
    }

    private void addOpenAction(NotepadButton openBtn) {
        openBtn.addActionListener(e -> {

            JFileChooser fileChooser = new JFileChooser();
            int option = fileChooser.showOpenDialog(parentFrame);
            if (option == JFileChooser.APPROVE_OPTION) {
                currentFile = fileChooser.getSelectedFile();
                try (BufferedReader reader = new BufferedReader(new FileReader(currentFile))) {
                    textArea.setText("");
                    String line;
                    while ((line = reader.readLine()) != null) {
                        textArea.append(line + "\n");
                    }
                    fileNameLabel.setText(currentFile.getName());
                    logLabel.setText("Opened " + currentFile.getName());
                } catch (IOException ex) {
                    logLabel.setText("Error opening file!");
                    JOptionPane.showMessageDialog(parentFrame, "Error opening file!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void addSaveAction(NotepadButton saveBtn) {
        saveBtn.addActionListener(e -> {

            if (currentFile == null) {
                saveAs();
            } else {
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
                    writer.write(textArea.getText());
                    logLabel.setText("Saved " + currentFile.getName());
                } catch (IOException ex) {
                    logLabel.setText("Error saving file!");
                    JOptionPane.showMessageDialog(parentFrame, "Error saving file!", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void addSaveAsAction(NotepadButton saveAsBtn) {
        saveAsBtn.addActionListener(e -> {
            saveAs();

        });
    }

    private void saveAs() {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showSaveDialog(parentFrame);
        if (option == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
                writer.write(textArea.getText());
                fileNameLabel.setText(currentFile.getName());
                logLabel.setText("Saved As " + currentFile.getName());
            } catch (IOException ex) {
                logLabel.setText("Error saving file!");
                JOptionPane.showMessageDialog(parentFrame, "Error saving file!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addFindAction(NotepadButton findBtn) {
        findBtn.addActionListener(e -> {


            String query = findText.getText();
            textArea.requestFocusInWindow();
            if (query != null && !query.isEmpty()) {
                String content = textArea.getText();
                lastFindIndex = content.indexOf(query, lastFindIndex);
                if (lastFindIndex != -1) {
                    textArea.select(lastFindIndex, lastFindIndex + query.length());
                    logLabel.setText("Found: " + query);
                    lastFindIndex += query.length();
                } else {
                    logLabel.setText("Not found: " + query);
                    lastFindIndex = 0; // reset search
                }
            }
        });
    }

    private void addReplaceAction(NotepadButton replaceBtn) {
        replaceBtn.addActionListener(e -> {

            String find = findText.getText();
            String replace = replaceText.getText();
            if (!find.isEmpty()) {
                String content = textArea.getText();
                if (content.contains(find)) {
                    String selected = textArea.getSelectedText();
                    if (selected != null) {
                        String replaced = selected.replace(find, replace);
                        textArea.replaceSelection(replaced);
                        logLabel.setText("Replaced '" + find + "' with '" + replace + "'");
                    } else {

                        logLabel.setText("No text selected.");
                    }
                } else {
                    logLabel.setText("No matches for '" + find + "'");
                }
            }

        });
    }

    private void addReplaceAllAction(NotepadButton replaceBtn) {
        replaceBtn.addActionListener(e -> {

            String find = findText.getText();
            String replace = replaceText.getText();
            if (!find.isEmpty()) {
                String content = textArea.getText();
                if (content.contains(find)) {
                    textArea.setText(content.replace(find, replace));
                    logLabel.setText("Replaced all '" + find + "' with '" + replace + "'");
                } else {
                    logLabel.setText("No matches for '" + find + "'");
                }
            }

        });
    }
}
