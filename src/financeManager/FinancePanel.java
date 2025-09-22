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
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.*;

// PDF Export
import org.apache.pdfbox.pdmodel.*;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

public class FinancePanel extends BaseAppPanel {
    private final File currentFile;
    private JTable table;
    private DefaultTableModel model;
    private JLabel logLabel;
    private TableRowSorter<DefaultTableModel> sorter;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private JPanel chartPanel;

    public FinancePanel(JFrame parentFrame) {
        super(MyColors.toDoInactive);
        currentFile = new File(System.getProperty("user.home"), "finance_records.csv");
        buildUI();
        loadRecordsForMonth(new Date()); // default: this month
    }

    // === Custom Button ===
    static class FinanceButton extends MyButton {
        FinanceButton(String text) {
            super(text);
            this.setBackground(new Color(0x4CAF50));
            this.hoverBg = new Color(0x81C784);
            this.setForeground(Color.WHITE);
        }
    }

    @Override
    protected void buildUI() {
        // Toolbar Buttons
        FinanceButton addBtn = new FinanceButton("New");
        FinanceButton deleteBtn = new FinanceButton("Delete");
        FinanceButton saveBtn = new FinanceButton("Save");
        FinanceButton filterBtn = new FinanceButton("Filter");
        FinanceButton clearFilterBtn = new FinanceButton("Clear Filter");
        FinanceButton exportBtn = new FinanceButton("Export PDF");

        addBtn.addActionListener(e -> addRecord());
        deleteBtn.addActionListener(e -> deleteRecord());
        saveBtn.addActionListener(e -> saveRecords());
        filterBtn.addActionListener(e -> applyFilter());
        clearFilterBtn.addActionListener(e -> {
            sorter.setRowFilter(null);
            updateChart();
            logLabel.setText("Filter cleared");
        });
        exportBtn.addActionListener(e -> exportPdf());

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        buttonsPanel.setOpaque(false);
        buttonsPanel.add(addBtn);
        buttonsPanel.add(deleteBtn);
        buttonsPanel.add(saveBtn);
        buttonsPanel.add(filterBtn);
        buttonsPanel.add(clearFilterBtn);
        buttonsPanel.add(exportBtn);

        logLabel = new JLabel(" ");
        logLabel.setFont(MyFonts.TEXT_FONT_BOLD);
        logLabel.setForeground(Color.GRAY);

        topPanel.setLayout(new BorderLayout());
        topPanel.add(buttonsPanel, BorderLayout.NORTH);
        topPanel.add(logLabel, BorderLayout.SOUTH);

        // Table setup
        String[] columns = { "Date", "Description", "Type", "Amount" };
        model = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int col) {
                return switch (col) {
                case 0 -> Date.class;
                case 3 -> Double.class;
                default -> String.class;
                };
            }
        };

        table = new JTable(model);
        sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);
        table.setRowHeight(26);

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        mainBody.add(scrollPane, BorderLayout.CENTER);

        // Chart panel
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setPreferredSize(new Dimension(400, 200));
        mainBody.add(chartPanel, BorderLayout.SOUTH);
    }

    // === Add Record ===
    private void addRecord() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));

        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDate(new Date());
        JTextField descField = new JTextField();
        JComboBox<String> typeBox = new JComboBox<>(new String[] { "Earning", "Cost" });
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
                updateChart();
            } catch (Exception ex) {
                logLabel.setText("Invalid input!");
            }
        }
    }

    private void deleteRecord() {
        int[] rows = table.getSelectedRows();
        for (int i = rows.length - 1; i >= 0; i--) {
            model.removeRow(table.convertRowIndexToModel(rows[i]));
        }
        updateChart();
    }

    // === Save/Load ===
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
        } catch (Exception e) {
            logLabel.setText("Error saving!");
        }
    }

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
        updateChart();
    }

    // === Filter ===
    private void applyFilter() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        JTextField descField = new JTextField();
        JComboBox<String> typeBox = new JComboBox<>(new String[] { "", "Earning", "Cost" });

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
        updateChart();
    }

    // === Chart ===
    private void updateChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, Double> monthlyTotals = new TreeMap<>(); // sorted by month

        for (int i = 0; i < model.getRowCount(); i++) {
            Date d = (Date) model.getValueAt(i, 0);
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            String key = c.get(Calendar.YEAR) + "-" + String.format("%02d", c.get(Calendar.MONTH) + 1);
            double amt = (Double) model.getValueAt(i, 3);
            if (model.getValueAt(i, 2).equals("Cost"))
                amt *= -1;
            monthlyTotals.put(key, monthlyTotals.getOrDefault(key, 0.0) + amt);
        }

        for (String k : monthlyTotals.keySet()) {
            dataset.addValue(monthlyTotals.get(k), "Net", k);
        }

        JFreeChart chart = ChartFactory.createBarChart("Monthly Net", "Month", "Amount", dataset,
                PlotOrientation.VERTICAL, false, true, false);

        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.GRAY);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, new Color(76, 175, 80));
        renderer.setBarPainter(new StandardBarPainter());
        renderer.setShadowVisible(false);

        chartPanel.removeAll();
        chartPanel.add(new ChartPanel(chart), BorderLayout.CENTER);
        chartPanel.revalidate();
    }

    // === Export PDF ===
    private void exportPdf() {
        String[] options = { "This Month", "All" };
        int choice = JOptionPane.showOptionDialog(null, "Export PDF", "Choose", 0, JOptionPane.PLAIN_MESSAGE, null,
                options, options[0]);
        if (choice == -1)
            return;

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(doc, page);
            float y = 750;

            PDFont fTitle = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDFont fText = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            // Title
            cs.beginText();
            cs.setFont(fTitle, 14);
            cs.newLineAtOffset(50, y);
            cs.showText("Finance Report");
            cs.endText();

            y -= 30;

            // Table content
            for (int i = 0; i < model.getRowCount(); i++) {
                if (y < 50) { // new page
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    y = 750;
                }

                String line = sdf.format((Date) model.getValueAt(i, 0)) + " | " + model.getValueAt(i, 1) + " | "
                        + model.getValueAt(i, 2) + " | " + model.getValueAt(i, 3);

                cs.beginText();
                cs.setFont(fText, 10);
                cs.newLineAtOffset(50, y);
                cs.showText(line);
                cs.endText();

                y -= 15;
            }

            cs.close();
            doc.save(new File(System.getProperty("user.home"), "finance_report.pdf"));
            logLabel.setText("PDF exported successfully!");
        } catch (Exception ex) {
            logLabel.setText("Error exporting: " + ex.getMessage());
        }
    }

}
