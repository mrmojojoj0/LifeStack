package todo;

import components.BaseAppPanel;
import components.MyColors;
import components.MyFonts;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Date;

public class TodoPanel extends BaseAppPanel {
    private JFrame parentFrame;
    private JTable table;
    private DefaultTableModel model;
    private JLabel logLabel;

    public TodoPanel(JFrame parentFrame) {
        super(MyColors.toDoInactive); // define color in MyColors like notepadInactive
        this.parentFrame = parentFrame;
        buildUI();
    }

    @Override
    protected void buildUI() {
        // === Toolbar Buttons ===
        JButton addBtn = new JButton("Add Task");
        JButton doneBtn = new JButton("Mark as Done");

        addAddAction(addBtn);
        addMarkDoneAction(doneBtn);

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(addBtn);
        buttonsPanel.add(doneBtn);

        // Log / info label
        logLabel = new JLabel(" ");
        logLabel.setFont(MyFonts.TEXT_FONT_BOLD);
        logLabel.setForeground(Color.GRAY);

        topPanel.setLayout(new BorderLayout());
        topPanel.add(buttonsPanel, BorderLayout.NORTH);
        topPanel.add(logLabel, BorderLayout.SOUTH);

        // === Table ===
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
                return col == 3; // only checkbox editable
            }
        };

        table = new JTable(model);
        table.setRowHeight(28);
        table.setFillsViewportHeight(true);

        // Sorting
        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        // Custom renderer for priority (colored square only)
        table.getColumnModel().getColumn(2).setCellRenderer(new PriorityRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));

        mainBody.add(scrollPane, BorderLayout.CENTER);
    }

    // === ACTIONS ===

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
            logLabel.setText("Marked as done");
        });
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

        private Icon makeColorIcon(Color c) {
            BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2 = img.createGraphics();
            g2.setColor(c);
            g2.fillRect(0, 0, 16, 16);
            g2.dispose();
            return new ImageIcon(img);
        }
    }
}
