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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import components.MyButton;
import java.util.List;

import com.toedter.calendar.JDateChooser;

class TodoButton extends MyButton {

    TodoButton(String text) {
        super(text);
        this.setBackground(new Color(0xdf4f4f));
        this.hoverBg = MyColors.toDoActive;
    }
}

public class TodoPanel extends BaseAppPanel {
    private final File currentFile;

    private JFrame parentFrame;
    private JTable table;
    private DefaultTableModel model;
    private JLabel logLabel;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    private TableRowSorter<DefaultTableModel> sorter;

    public TodoPanel(JFrame parentFrame) {
        super(MyColors.toDoInactive);
        this.parentFrame = parentFrame;
        currentFile = new File(System.getProperty("user.home"), "todo_tasks.csv");
        buildUI();
        loadTasks();
    }

    @Override
    protected void buildUI() {
        // Toolbar Buttons
        TodoButton addBtn = new TodoButton("New");
        TodoButton deleteBtn = new TodoButton("Delete");
        TodoButton saveBtn = new TodoButton("Save");

        addAddAction(addBtn);
        addDeleteAction(deleteBtn);
        addSaveAction(saveBtn);

        TodoButton searchBtn = new TodoButton("Filter");
        TodoButton clearFilter = new TodoButton("Clear Filter");

        searchBtn.addActionListener(e -> applySearchFilter());

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(addBtn);
        buttonsPanel.add(deleteBtn);
        buttonsPanel.add(saveBtn);
        buttonsPanel.add(searchBtn);
        buttonsPanel.add(clearFilter);

        clearFilter.addActionListener(e -> {
            sorter.setRowFilter(null);
            logLabel.setText("Showing all tasks");
        });

        logLabel = new JLabel(" ");
        logLabel.setFont(MyFonts.TEXT_FONT_BOLD);
        logLabel.setForeground(Color.GRAY);

        topPanel.setLayout(new BorderLayout());
        topPanel.add(buttonsPanel, BorderLayout.NORTH);
        topPanel.add(logLabel, BorderLayout.SOUTH);

        // Table
        String[] columns = { "Task", "Due Date", "Priority", "Status" };

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
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.getColumnModel().getColumn(2).setCellRenderer(new PriorityRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));

        mainBody.add(scrollPane, BorderLayout.CENTER);

    }

    // === Actions ===
    private void addAddAction(TodoButton addBtn) {
        addBtn.addActionListener(e -> {
            JPanel panel = new JPanel();

            JTextField taskField = new JTextField();

            JDateChooser dateChooser = new JDateChooser();
            dateChooser.setDate(new Date());
            dateChooser.setDateFormatString("dd/MM/yyyy");

            JComboBox<String> priorityBox = new JComboBox<>(new String[] { "Low", "Medium", "High" });

            taskField.setPreferredSize(new Dimension(180, 25));
            panel.add(new JLabel("Task:"));
            panel.add(taskField);
            panel.add(new JLabel("Due Date:"));
            dateChooser.setPreferredSize(new Dimension(100,25));
            panel.add(dateChooser);
            panel.add(new JLabel("Priority:"));
            panel.add(priorityBox);

            int result = JOptionPane.showConfirmDialog(parentFrame, panel, "Add New Task",
                    JOptionPane.OK_CANCEL_OPTION);

            if (result == JOptionPane.OK_OPTION) {
                String task = taskField.getText().trim();
                Date date = (Date) dateChooser.getDate();
                String priority = (String) priorityBox.getSelectedItem();

                if (!task.isEmpty()) {
                    model.addRow(new Object[] { task, date, priority, false });
                    logLabel.setText("Task added: " + task);
                } else {
                    logLabel.setText("Task name required!");
                }
            }
        });
    }

    private void addDeleteAction(TodoButton deleteBtn) {
        deleteBtn.addActionListener(e -> {
            int[] selectedRows = table.getSelectedRows();
            if (selectedRows.length == 0) {
                logLabel.setText("No task selected!");
                return;
            }
            for (int i = selectedRows.length - 1; i >= 0; i--) {
                int modelRow = table.convertRowIndexToModel(selectedRows[i]);
                model.removeRow(modelRow);
            }
            logLabel.setText("Deleted selected tasks");
        });
    }

    private void addSaveAction(TodoButton saveBtn) {
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
            logLabel.setText("Saved tasks");
        } catch (Exception ex) {
            logLabel.setText("Error saving tasks!");
            JOptionPane.showMessageDialog(parentFrame, "Error saving tasks!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadTasks() {
        if (!currentFile.exists())
            return;
        model.setRowCount(0);
        int dueTodayCount = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(currentFile))) {
            String line;
            Date today = new Date();
            String todayStr = sdf.format(today);

            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 4) {
                    String task = parts[0];
                    Date date = sdf.parse(parts[1]);
                    String priority = parts[2];
                    boolean done = Boolean.parseBoolean(parts[3]);
                    model.addRow(new Object[] { task, date, priority, done });

                    if (sdf.format(date).equals(todayStr) && !done) {
                        dueTodayCount++;
                    }
                }
            }

            if (dueTodayCount > 0) {
                logLabel.setText("You have " + dueTodayCount + " task(s) due today!");
            } else {
                logLabel.setText("No tasks due today.");
            }

        } catch (Exception ex) {
            logLabel.setText("Error loading tasks!");
        }
    }

    private void applySearchFilter() {
        // Create panel with vertical layout
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Fields
        JTextField taskField = new JTextField(20);
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("dd/MM/yyyy");
        JComboBox<String> priorityBox = new JComboBox<>(new String[] { "", "Low", "Medium", "High" });
        JComboBox<String> doneBox = new JComboBox<>(new String[] { "", "Done", "Not Done" });

        // Add fields with labels
        panel.add(new JLabel("Task:"));
        panel.add(taskField);
        panel.add(new JLabel("Due Date:"));
        panel.add(dateChooser);
        panel.add(new JLabel("Priority:"));
        panel.add(priorityBox);
        panel.add(new JLabel("Status:"));
        panel.add(doneBox);

        if (JOptionPane.showConfirmDialog(parentFrame, panel, "Filter Tasks", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE) != JOptionPane.OK_OPTION)
            return;

        // Build filters
        List<RowFilter<Object, Object>> filters = new ArrayList<>();

        if (!taskField.getText().trim().isEmpty())
            filters.add(RowFilter.regexFilter("(?i)" + taskField.getText().trim(), 0));

        if (dateChooser.getDate() != null)
            filters.add(new RowFilter<Object, Object>() {
                public boolean include(Entry<?, ?> entry) {
                    Date cellDate = (Date) entry.getValue(1);
                    Calendar c1 = Calendar.getInstance();
                    c1.setTime(cellDate);
                    Calendar c2 = Calendar.getInstance();
                    c2.setTime(dateChooser.getDate());
                    return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                            && c1.get(Calendar.MONTH) == c2.get(Calendar.MONTH)
                            && c1.get(Calendar.DAY_OF_MONTH) == c2.get(Calendar.DAY_OF_MONTH);
                }
            });

        if (priorityBox.getSelectedIndex() > 0)
            filters.add(RowFilter.regexFilter("^" + priorityBox.getSelectedItem() + "$", 2));

        if (doneBox.getSelectedIndex() > 0) {
            boolean done = doneBox.getSelectedItem().equals("Done");
            filters.add(new RowFilter<Object, Object>() {
                public boolean include(Entry<?, ?> entry) {
                    return (Boolean) entry.getValue(3) == done;
                }
            });
        }

        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        logLabel.setText(filters.isEmpty() ? "Showing all tasks" : "Filter applied");
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
                setText(value.toString());
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
