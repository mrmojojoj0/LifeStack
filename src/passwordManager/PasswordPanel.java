package passwordManager;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.util.*;
import components.BaseAppPanel;
import components.MyButton;
import components.MyColors;
import components.MyFonts;
import java.util.List;

class PMButton extends MyButton {
    PMButton(String text) {
        super(text);
        this.setBackground(new Color(0x4f8fdf));
        this.hoverBg = MyColors.toDoActive;
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

        addAddAction(addBtn);
        addDeleteAction(deleteBtn);
        addSaveAction(saveBtn);
        addFilterAction(filterBtn);
        addShowHideAction(showHideBtn);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(addBtn);
        buttonsPanel.add(deleteBtn);
        buttonsPanel.add(saveBtn);
        buttonsPanel.add(filterBtn);
        buttonsPanel.add(clearFilterBtn);
        buttonsPanel.add(showHideBtn);

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
        String[] columns = { "Website", "Username", "Password" };

        model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return true; // all editable
            }
        };

        table = new JTable(model);
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Hide passwords initially
        table.getColumnModel().getColumn(2).setCellRenderer(new PasswordRenderer());

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

            panel.add(new JLabel("Website:")); panel.add(siteField);
            panel.add(new JLabel("Username:")); panel.add(userField);
            panel.add(new JLabel("Password:")); panel.add(passField);

            int result = JOptionPane.showConfirmDialog(parentFrame, panel, "Add Password",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                if (!siteField.getText().trim().isEmpty()) {
                    model.addRow(new Object[] {
                            siteField.getText().trim(),
                            userField.getText().trim(),
                            passField.getText().trim()
                    });
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
            JOptionPane.showMessageDialog(parentFrame, "Error saving passwords!", "Error",
                    JOptionPane.ERROR_MESSAGE);
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
                if (parts.length == 3) {
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

            panel.add(new JLabel("Website:")); panel.add(siteField);
            panel.add(new JLabel("Username:")); panel.add(userField);

            int result = JOptionPane.showConfirmDialog(parentFrame, panel, "Filter Passwords",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result != JOptionPane.OK_OPTION) return;

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
            passwordsVisible = !passwordsVisible;
            TableColumn passwordColumn = table.getColumnModel().getColumn(2);
            if (passwordsVisible) {
                passwordColumn.setCellRenderer(null); // show actual passwords
                showHideBtn.setText("Hide");
            } else {
                passwordColumn.setCellRenderer(new PasswordRenderer()); // hide passwords
                showHideBtn.setText("Show");
            }
            table.repaint();
        });
    }

    // === Custom Renderer to hide passwords ===
    static class PasswordRenderer extends DefaultTableCellRenderer {
        @Override
        protected void setValue(Object value) {
            setText(value == null ? "" : "*".repeat(value.toString().length()));
        }
    }
}
