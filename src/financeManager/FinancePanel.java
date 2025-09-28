package financeManager;

import components.BaseAppPanel;
import components.MyColors;
import components.MyFonts;
import components.MyButton;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import com.toedter.calendar.JDateChooser;

// PDF Export
class FinanceButton extends MyButton {
    FinanceButton(String text) {
        super(text);
        this.setBackground(new Color(0x4CAF50));
        this.hoverBg = new Color(0x81C784);
        this.setForeground(Color.WHITE);
    }
}

class TypeRenderer extends DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
            int row, int column) {
        Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if ("Earning".equals(value))
            c.setForeground(MyColors.financeActive);
        else if ("Expense".equals(value))
            c.setForeground(Color.RED);
        return c;
    }
}

public class FinancePanel extends BaseAppPanel {
    private final File currentFile;
    private JTable table;
    private DefaultTableModel model;
    private JLabel logLabel;
    private JLabel totalsLabel; 
    private TableRowSorter<DefaultTableModel> sorter;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private ExpenseGraph chartPanel;

    public FinancePanel(JFrame parentFrame) {
        super(MyColors.financeInactive);
        currentFile = new File(System.getProperty("user.home"), "lifestack/finance_records.csv");
        buildUI();
        loadRecordsForMonth(new Date()); // default: this month
    }

    @Override
    protected void buildUI() {
        // === Toolbar Buttons ===
        FinanceButton addBtn = new FinanceButton("New");
        FinanceButton deleteBtn = new FinanceButton("Delete");
        FinanceButton saveBtn = new FinanceButton("Save");
        FinanceButton filterBtn = new FinanceButton("Filter");
        FinanceButton clearFilterBtn = new FinanceButton("Clear Filter");
        FinanceButton exportBtn = new FinanceButton("Export PDF");
        Export exp = new Export();

        addBtn.addActionListener(e -> addRecord());
        deleteBtn.addActionListener(e -> deleteRecord());
        saveBtn.addActionListener(e -> saveRecords());
        filterBtn.addActionListener(e -> applyFilter());
        clearFilterBtn.addActionListener(e -> {
            sorter.setRowFilter(null);
            chartPanel.updateChart(model);
            logLabel.setText("Filter cleared");
            updateTotals();
        });
        exportBtn.addActionListener(e -> exp.exportPdf(model, sdf, logLabel, totalsLabel.getText()));

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(addBtn);
        buttonsPanel.add(deleteBtn);
        buttonsPanel.add(saveBtn);
        buttonsPanel.add(filterBtn);
        buttonsPanel.add(clearFilterBtn);
        buttonsPanel.add(exportBtn);

        // === Log Label ===
        logLabel = new JLabel(" ");
        logLabel.setFont(MyFonts.TEXT_FONT_BOLD);
        logLabel.setForeground(Color.GRAY);

        // === Totals Label (NEW) ===
        totalsLabel = new JLabel("Earnings: 0.00 | Costs: 0.00 | Net: 0.00");
        totalsLabel.setFont(MyFonts.TEXT_FONT_BOLD);
        totalsLabel.setForeground(Color.DARK_GRAY);

        // === Top Panel ===
        JPanel bottomInfoPanel = new JPanel(new GridLayout(2, 1));
        bottomInfoPanel.setOpaque(false);
        bottomInfoPanel.add(logLabel);
        bottomInfoPanel.add(totalsLabel);

        topPanel.setLayout(new BorderLayout());
        topPanel.add(buttonsPanel, BorderLayout.NORTH);
        topPanel.add(bottomInfoPanel, BorderLayout.SOUTH);

        // === Table setup ===
        String[] columns = { "Date", "Description", "Type", "Amount" };
        model = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int col) {
                return switch (col) {
                case 0 -> Date.class;
                case 3 -> Double.class;
                case 2 -> TypeRenderer.class;
                default -> String.class;
                };
            }
        };

        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.setRowHeight(26);
        table.getColumnModel().getColumn(2).setCellRenderer(new TypeRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        mainBody.add(scrollPane, BorderLayout.CENTER);

        // === Chart panel ===
        chartPanel = new ExpenseGraph();
        chartPanel.setPreferredSize(new Dimension(400, 200));
        mainBody.add(chartPanel, BorderLayout.SOUTH);
    }

    // === Add Record ===
    private void addRecord() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));

        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDate(new Date());
        JTextField descField = new JTextField();
        JComboBox<String> typeBox = new JComboBox<>(new String[] { "Earning", "Expense" });
        JTextField amountField = new JTextField();

        panel.add(new JLabel("Date:"));
        panel.add(dateChooser);
        panel.add(new JLabel("Description:"));
        panel.add(descField);
        panel.add(new JLabel("Type:"));
        panel.add(typeBox);
        panel.add(new JLabel("Amount:"));
        panel.add(amountField);

        int result = JOptionPane.showConfirmDialog(null, panel, "Add Record", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                Date date = dateChooser.getDate();
                String desc = descField.getText().trim();
                String type = typeBox.getSelectedItem().toString();
                double amt = Double.parseDouble(amountField.getText().trim());

                model.addRow(new Object[] { date, desc, type, amt });
                logLabel.setText("Record added");
                chartPanel.updateChart(model);
                updateTotals(); 
            } catch (Exception ex) {
                logLabel.setText("Invalid input!");
            }
        }
    }

    // === Delete Record ===
    private void deleteRecord() {
        int[] rows = table.getSelectedRows();
        for (int i = rows.length - 1; i >= 0; i--) {
            model.removeRow(table.convertRowIndexToModel(rows[i]));
        }
        chartPanel.updateChart(model);
        updateTotals(); 
    }

    // === Save Records ===
    private void saveRecords() {
        try (BufferedWriter w = new BufferedWriter(new FileWriter(currentFile))) {
            for (int i = 0; i < model.getRowCount(); i++) {
                String date = sdf.format((Date) model.getValueAt(i, 0));
                String desc = model.getValueAt(i, 1).toString();
                String type = model.getValueAt(i, 2).toString();
                String amt = model.getValueAt(i, 3).toString();
                w.write(String.join(",", date, desc, type, amt));
                w.newLine();
            }
            logLabel.setText("Saved");
            updateTotals(); 
        } catch (Exception e) {
            logLabel.setText("Error saving!");
        }
    }

    // === Load Records for Current Month ===
    private void loadRecordsForMonth(Date month) {
        if (!currentFile.exists())
            return;
        model.setRowCount(0);
        Calendar cal = Calendar.getInstance();
        cal.setTime(month);
        int m = cal.get(Calendar.MONTH);
        int y = cal.get(Calendar.YEAR);

        try (BufferedReader r = new BufferedReader(new FileReader(currentFile))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split(",");
                if (p.length == 4) {
                    Date d = sdf.parse(p[0]);
                    Calendar c = Calendar.getInstance();
                    c.setTime(d);
                    if (c.get(Calendar.MONTH) == m && c.get(Calendar.YEAR) == y) {
                        model.addRow(new Object[] { d, p[1], p[2], Double.parseDouble(p[3]) });
                    }
                }
            }
        } catch (Exception e) {
            logLabel.setText("Error loading!");
        }
        chartPanel.updateChart(model);
        updateTotals();
    }

    // === Apply Filter ===
    private void applyFilter() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        JTextField descField = new JTextField();
        JComboBox<String> typeBox = new JComboBox<>(new String[] { "", "Earning", "Expense" });

        panel.add(new JLabel("Description contains:"));
        panel.add(descField);
        panel.add(new JLabel("Type:"));
        panel.add(typeBox);

        if (JOptionPane.showConfirmDialog(null, panel, "Filter", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION)
            return;

        List<RowFilter<Object, Object>> filters = new ArrayList<>();
        if (!descField.getText().trim().isEmpty())
            filters.add(RowFilter.regexFilter("(?i)" + descField.getText().trim(), 1));
        if (typeBox.getSelectedIndex() > 0)
            filters.add(RowFilter.regexFilter("^" + typeBox.getSelectedItem() + "$", 2));

        sorter.setRowFilter(filters.isEmpty() ? null : RowFilter.andFilter(filters));
        chartPanel.updateChart(model);
        updateTotals(); 
    }

    // === NEW: Update Totals ===
    private void updateTotals() {
        double earnings = 0, costs = 0;
        for (int i = 0; i < model.getRowCount(); i++) {
            String type = model.getValueAt(i, 2).toString();
            double amt = (Double) model.getValueAt(i, 3);
            if ("Earning".equalsIgnoreCase(type)) {
                earnings += amt;
            } else if ("Expense".equalsIgnoreCase(type)) {
                costs += amt;
            }
        }
        double net = earnings - costs;
        totalsLabel.setText(String.format("Earnings: %.2f | Costs: %.2f | Net: %.2f", earnings, costs, net));
    }

}
