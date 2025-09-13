package todo;

import components.BaseAppPanel;
import components.MyColors;
import components.MyFonts;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TodoPanel extends BaseAppPanel {
    private JFrame parentFrame;
    private JTable table;
    private DefaultTableModel model;
    private JLabel logLabel;
    private final File currentFile; // default file in user directory
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public TodoPanel(JFrame parentFrame) {
        super(MyColors.toDoInactive);
        this.parentFrame = parentFrame;
        currentFile = new File(System.getProperty("user.home"), "todo_tasks.csv");
        buildUI();
        loadTasks(); // load by default
    }

    @Override
    protected void buildUI() {
        // Toolbar Buttons
        JButton addBtn = new JButton("Add Task");
        JButton doneBtn = new JButton("Mark as Done");
        JButton saveBtn = new JButton("Save");

        addAddAction(addBtn);
        addMarkDoneAction(doneBtn);
        addSaveAction(saveBtn);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(addBtn);
        buttonsPanel.add(doneBtn);
        buttonsPanel.add(saveBtn);

        logLabel = new JLabel(" ");
        logLabel.setFont(MyFonts.TEXT_FONT_BOLD);
        logLabel.setForeground(Color.GRAY);

        topPanel.setLayout(new BorderLayout());
        topPanel.add(buttonsPanel, BorderLayout.NORTH);
        topPanel.add(logLabel, BorderLayout.SOUTH);

        // Table
        String[] columns = {"Task", "Due Date", "Priority", "Done"};
        model = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int col) {
                return switch (col) {
                    case 1 -> Date.class;
                    case 2 -> String.class;
                    case 3 -> Boolean.class;
                    default -> String.class;
                };
            }

            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 3;
            }
        };

        table = new JTable(model) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                boolean done = (Boolean) getModel().getValueAt(convertRowIndexToModel(row), 3);
                c.setBackground(done ? new Color(200, 255, 200) : Color.WHITE); // greenish if done
                return c;
            }
        };

        table.setRowHeight(28);
        table.setFillsViewportHeight(true);
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.getColumnModel().getColumn(2).setCellRenderer(new PriorityRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));

        mainBody.add(scrollPane, BorderLayout.CENTER);
    }

    // === Actions ===
    private void addAddAction(JButton addBtn) {
        addBtn.addActionListener(e -> {
            JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
            JTextField taskField = new JTextField();
            JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
            dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd"));
            JComboBox<String> priorityBox = new JComboBox<>(new String[]{"Low", "Medium", "High"});

            panel.add(new JLabel("Task:"));
            panel.add(taskField);
            panel.add(new JLabel("Due Date:"));
            panel.add(dateSpinner);
            panel.add(new JLabel("Priority:"));
            panel.add(priorityBox);

            int result = JOptionPane.showConfirmDialog(parentFrame, panel,
                    "Add Task", JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                String task = taskField.getText().trim();
                Date date = (Date) dateSpinner.getValue();
                String priority = (String) priorityBox.getSelectedItem();

                if (!task.isEmpty()) {
                    model.addRow(new Object[]{task, date, priority, false});
                    logLabel.setText("Task added: " + task);
                } else {
                    logLabel.setText("Task name required!");
                }
            }
        });
    }

    private void addMarkDoneAction(JButton doneBtn) {
        doneBtn.addActionListener(e -> {
            int[] selectedRows = table.getSelectedRows();
            if (selectedRows.length == 0) {
                logLabel.setText("No task selected!");
                return;
            }
            for (int row : selectedRows) {
                int modelRow = table.convertRowIndexToModel(row);
                model.setValueAt(true, modelRow, 3);
            }
            table.repaint();
            logLabel.setText("Marked as done");
        });
    }

    private void addSaveAction(JButton saveBtn) {
        saveBtn.addActionListener(e -> saveTasks());
    }

    private void saveTasks() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
            for (int i = 0; i < model.getRowCount(); i++) {
                String task = model.getValueAt(i, 0).toString();
                String date = sdf.format((Date) model.getValueAt(i, 1));
                String priority = model.getValueAt(i, 2).toString();
                String done = model.getValueAt(i, 3).toString();
                writer.write(String.join(",", task, date, priority, done));
                writer.newLine();
            }
            logLabel.setText("Saved tasks to " + currentFile.getAbsolutePath());
        } catch (Exception ex) {
            logLabel.setText("Error saving tasks!");
            JOptionPane.showMessageDialog(parentFrame, "Error saving tasks!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTasks() {
        if (!currentFile.exists()) return;
        model.setRowCount(0);
        try (BufferedReader reader = new BufferedReader(new FileReader(currentFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String task = parts[0];
                    Date date = sdf.parse(parts[1]);
                    String priority = parts[2];
                    boolean done = Boolean.parseBoolean(parts[3]);
                    model.addRow(new Object[]{task, date, priority, done});
                }
            }
        } catch (Exception ex) {
            logLabel.setText("Error loading tasks!");
        }
    }

    // === Priority Renderer ===
    static class PriorityRenderer extends DefaultTableCellRenderer {
        @Override
        protected void setValue(Object value) {
            setText("");
            if (value != null) {
                Color color = switch (value.toString()) {
                    case "High" -> Color.RED;
                    case "Medium" -> Color.ORANGE;
                    default -> Color.GREEN;
                };
                setIcon(makeColorIcon(color));
            } else {
                setIcon(null);
            }
        }

        private static Icon makeColorIcon(Color c) {
            BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = img.createGraphics();
            g2.setColor(c);
            g2.fillRect(0, 0, 16, 16);
            g2.dispose();
            return new ImageIcon(img);
        }
    }
}
