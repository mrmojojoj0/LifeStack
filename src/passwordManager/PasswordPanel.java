package passwordManager;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;
import components.BaseAppPanel;
import components.MyButton;
import components.MyColors;
import components.MyFonts;
import java.awt.datatransfer.*;

class PMButton extends MyButton {
    PMButton(String text) {
        super(text);
        this.setBackground(MyColors.passwordActive);
        this.hoverBg = new Color(0x4682b4);
    }
}

public class PasswordPanel extends BaseAppPanel {
    private final File currentFile;
    private JFrame parentFrame;
    private JTable table;
    private DefaultTableModel model;
    private JLabel logLabel;
    private TableRowSorter<DefaultTableModel> sorter;

    private boolean passwordsVisible = false; // toggle state

    public PasswordPanel(JFrame parentFrame) {
        super(MyColors.passwordInactive);
        this.parentFrame = parentFrame;
        currentFile = new File(System.getProperty("user.home"), "passwords.csv");
        buildUI();
        loadPasswords();
    }

    @Override
    protected void buildUI() {
        // Toolbar Buttons
        PMButton addBtn = new PMButton("New");
        PMButton deleteBtn = new PMButton("Delete");
        PMButton saveBtn = new PMButton("Save");
        PMButton filterBtn = new PMButton("Filter");
        PMButton clearFilterBtn = new PMButton("Clear Filter");
        PMButton showHideBtn = new PMButton("Show");
        PMButton copyButton = new PMButton("Copy");

        addAddAction(addBtn);
        addDeleteAction(deleteBtn);
        addSaveAction(saveBtn);
        addFilterAction(filterBtn);
        addShowHideAction(showHideBtn);
        addCopyAction(copyButton);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(addBtn);
        buttonsPanel.add(deleteBtn);
        buttonsPanel.add(saveBtn);
        buttonsPanel.add(filterBtn);
        buttonsPanel.add(clearFilterBtn);
        buttonsPanel.add(showHideBtn);
        buttonsPanel.add(copyButton);

        clearFilterBtn.addActionListener(e -> {
            sorter.setRowFilter(null);
            logLabel.setText("Showing all passwords");
        });

        logLabel = new JLabel(" ");
        logLabel.setFont(MyFonts.TEXT_FONT_BOLD);
        logLabel.setForeground(Color.GRAY);

        topPanel.setLayout(new BorderLayout());
        topPanel.add(buttonsPanel, BorderLayout.NORTH);
        topPanel.add(logLabel, BorderLayout.SOUTH);

        // Table
        String[] columns = { "Website", "Username", "Password", "Strength" };
        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col != 3; // Strength column is non-editable
            }
        };

        table = new JTable(model);
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Password column hidden initially
        table.getColumnModel().getColumn(2).setCellRenderer(new PasswordRenderer());

        // Strength column colored
        table.getColumnModel().getColumn(3).setCellRenderer(new StrengthRenderer());

        // Update strength when editing existing password
        model.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                if (col == 2) { // Password column
                    String password = table.getValueAt(row, col).toString();
                    String strength = getPasswordStrength(password);
                    table.setValueAt(strength, row, 3);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));
        mainBody.add(scrollPane, BorderLayout.CENTER);
    }

    // === Actions ===
    private void addAddAction(PMButton addBtn) {
        addBtn.addActionListener(e -> {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            JTextField siteField = new JTextField(20);
            JTextField userField = new JTextField(20);
            JTextField passField = new JTextField(20);
            JLabel strengthLabel = new JLabel("Strength: ");

            panel.add(new JLabel("Website:"));
            panel.add(siteField);
            panel.add(new JLabel("Username:"));
            panel.add(userField);
            panel.add(new JLabel("Password:"));
            panel.add(passField);
            panel.add(strengthLabel);

            // Update strength while typing
            passField.getDocument().addDocumentListener(new DocumentListener() {
                void update() {
                    String password = passField.getText();
                    String strength = getPasswordStrength(password);
                    strengthLabel.setText("Strength: " + strength);
                    if ("Weak".equals(strength))
                        strengthLabel.setForeground(Color.RED);
                    else if ("Medium".equals(strength))
                        strengthLabel.setForeground(Color.ORANGE);
                    else
                        strengthLabel.setForeground(Color.GREEN.darker());
                }

                @Override
                public void insertUpdate(DocumentEvent e) {
                    update();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    update();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    update();
                }
            });

            int result = JOptionPane.showConfirmDialog(parentFrame, panel, "Add Password", JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                if (!siteField.getText().trim().isEmpty()) {
                    String password = passField.getText().trim();
                    model.addRow(new Object[] { siteField.getText().trim(), userField.getText().trim(), password,
                            getPasswordStrength(password) });
                    logLabel.setText("Password added for " + siteField.getText().trim());
                } else {
                    logLabel.setText("Website is required!");
                }
            }
        });
    }

    private void addDeleteAction(PMButton deleteBtn) {
        deleteBtn.addActionListener(e -> {
            int[] selectedRows = table.getSelectedRows();
            if (selectedRows.length == 0) {
                logLabel.setText("No entry selected!");
                return;
            }
            for (int i = selectedRows.length - 1; i >= 0; i--) {
                int modelRow = table.convertRowIndexToModel(selectedRows[i]);
                model.removeRow(modelRow);
            }
            logLabel.setText("Deleted selected entries");
        });
    }

    private void addSaveAction(PMButton saveBtn) {
        saveBtn.addActionListener(e -> savePasswords());
    }

    private void savePasswords() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
            for (int i = 0; i < model.getRowCount(); i++) {
                List<String> row = new ArrayList<>();
                for (int j = 0; j < model.getColumnCount(); j++) {
                    row.add(model.getValueAt(i, j).toString());
                }
                writer.write(String.join(",", row));
                writer.newLine();
            }
            logLabel.setText("Passwords saved");
        } catch (Exception ex) {
            logLabel.setText("Error saving passwords!");
            JOptionPane.showMessageDialog(parentFrame, "Error saving passwords!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadPasswords() {
        if (!currentFile.exists())
            return;
        model.setRowCount(0);
        try (BufferedReader reader = new BufferedReader(new FileReader(currentFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    model.addRow(parts);
                }
            }
        } catch (Exception ex) {
            logLabel.setText("Error loading passwords!");
        }
    }

    private void addFilterAction(PMButton filterBtn) {
        filterBtn.addActionListener(e -> {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JTextField siteField = new JTextField(20);
            JTextField userField = new JTextField(20);

            panel.add(new JLabel("Website:"));
            panel.add(siteField);
            panel.add(new JLabel("Username:"));
            panel.add(userField);

            int result = JOptionPane.showConfirmDialog(parentFrame, panel, "Filter Passwords",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result != JOptionPane.OK_OPTION)
                return;

            List<RowFilter<Object, Object>> filters = new ArrayList<>();
            if (!siteField.getText().trim().isEmpty())
                filters.add(RowFilter.regexFilter("(?i)" + siteField.getText().trim(), 0));
            if (!userField.getText().trim().isEmpty())
                filters.add(RowFilter.regexFilter("(?i)" + userField.getText().trim(), 1));

            sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
            logLabel.setText(filters.isEmpty() ? "Showing all passwords" : "Filter applied");
        });
    }

    private void addShowHideAction(PMButton showHideBtn) {
        showHideBtn.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                logLabel.setText("Select a row to show/hide password");
                return;
            }
            TableColumn passwordColumn = table.getColumnModel().getColumn(2);

            if (passwordsVisible) {
                passwordColumn.setCellRenderer(new PasswordRenderer());
                showHideBtn.setText("Show");
            } else {
                passwordColumn.setCellRenderer(new DefaultTableCellRenderer() {
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                            boolean hasFocus, int row, int column) {
                        if (row == selectedRow) {
                            setText(value.toString());
                        } else {
                            setText("*".repeat(value.toString().length()));
                        }
                        setForeground(isSelected ? table.getSelectionForeground() : table.getForeground());
                        setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                        return this;
                    }
                });
                showHideBtn.setText("Hide");
            }
            passwordsVisible = !passwordsVisible;
            table.repaint();
        });
    }

    private void addCopyAction(PMButton cButton) {
        cButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            int col = table.getSelectedColumn();

            if (row != -1 && col != -1) {
                // Get the actual value from the table cell
                Object cellValue = table.getValueAt(row, col);

                if (cellValue != null) {
                    String text = cellValue.toString();
                    Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                    clipboard.setContents(new StringSelection(text), null);

                    if (col != 2) {
                        logLabel.setText("Copied to clipboard: " + text);
                    } else {
                        logLabel.setText("Copied password to clipboard.");
                    }
                } else {
                    logLabel.setText("Cell is empty!");
                }
            } else {
                logLabel.setText("No cell selected!");
            }
        });
    }

    // === Password strength calculation ===
    private String getPasswordStrength(String password) {
        int score = 0;
        if (password.length() >= 8)
            score++;
        if (password.matches(".*[A-Z].*"))
            score++;
        if (password.matches(".*[a-z].*"))
            score++;
        if (password.matches(".*\\d.*"))
            score++;
        if (password.matches(".*[^a-zA-Z0-9].*"))
            score++;

        if (score <= 2)
            return "Weak";
        else if (score <= 4)
            return "Medium";
        else
            return "Strong";
    }

    // === Renderers ===
    static class PasswordRenderer extends DefaultTableCellRenderer {
        @Override
        protected void setValue(Object value) {
            setText(value == null ? "" : "*".repeat(value.toString().length()));
        }
    }

    static class StrengthRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if ("Weak".equals(value))
                c.setForeground(Color.RED);
            else if ("Medium".equals(value))
                c.setForeground(Color.ORANGE);
            else if ("Strong".equals(value))
                c.setForeground(Color.GREEN.darker());
            else
                c.setForeground(Color.BLACK);
            return c;
        }
    }

}
