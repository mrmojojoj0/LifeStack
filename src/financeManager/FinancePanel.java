package financeManager;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.itextpdf.layout.Document;

import components.MyFonts;

// For PDF export
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

public class FinancePanel extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private File dbFile;
    private JLabel totalEarningsLabel;
    private JLabel totalCostsLabel;
    private JLabel balanceLabel;

    public FinancePanel() {
        setLayout(new BorderLayout());

        // === Setup CSV file ===
        String userDir = System.getProperty("user.home");
        dbFile = new File(userDir, "finances.csv");

        // === Table ===
        model = new DefaultTableModel(new String[]{"Date", "Description", "Type", "Amount"}, 0);
        table = new JTable(model);

        // Row color renderer
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String type = table.getValueAt(row, 2).toString();
                if ("Earning".equalsIgnoreCase(type)) {
                    c.setForeground(Color.GREEN.darker());
                } else if ("Cost".equalsIgnoreCase(type)) {
                    c.setForeground(Color.RED);
                } else {
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });

        loadFinanceData();
        add(new JScrollPane(table), BorderLayout.CENTER);

        // === Buttons ===
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add");
        JButton deleteBtn = new JButton("Delete");
        JButton saveBtn = new JButton("Save");
        JButton exportPdfBtn = new JButton("Export PDF");

        buttons.add(addBtn);
        buttons.add(deleteBtn);
        buttons.add(saveBtn);
        buttons.add(exportPdfBtn);
        add(buttons, BorderLayout.NORTH);

        // === Summary Panel ===
        JPanel summary = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalEarningsLabel = new JLabel("Earnings: 0");
        totalCostsLabel = new JLabel("Costs: 0");
        balanceLabel = new JLabel("Balance: 0");

        summary.add(totalEarningsLabel);
        summary.add(totalCostsLabel);
        summary.add(balanceLabel);
        add(summary, BorderLayout.SOUTH);

        // === Actions ===
        addBtn.addActionListener(e -> addEntry());
        deleteBtn.addActionListener(e -> {
            deleteEntry();
            updateSummary();
        });
        saveBtn.addActionListener(e -> saveFinanceData());
        exportPdfBtn.addActionListener(e -> exportToPDF());

        updateSummary();
    }

    private void addEntry() {
        // Date selector
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);

        JTextField descField = new JTextField();
        JComboBox<String> typeBox = new JComboBox<>(new String[]{"Earning", "Cost"});
        JTextField amountField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(4, 2));
        panel.add(new JLabel("Date:"));
        panel.add(dateSpinner);
        panel.add(new JLabel("Description:"));
        panel.add(descField);
        panel.add(new JLabel("Type:"));
        panel.add(typeBox);
        panel.add(new JLabel("Amount:"));
        panel.add(amountField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Add Finance Entry", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            Date date = (Date) dateSpinner.getValue();
            String formattedDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
            model.addRow(new Object[]{formattedDate, descField.getText(), typeBox.getSelectedItem(), amountField.getText()});
            updateSummary();
        }
    }

    private void deleteEntry() {
        int row = table.getSelectedRow();
        if (row != -1) {
            model.removeRow(row);
        } else {
            JOptionPane.showMessageDialog(this, "Select a row to delete.", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void loadFinanceData() {
        if (!dbFile.exists()) return;
        try (BufferedReader reader = Files.newBufferedReader(dbFile.toPath())) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", -1);
                if (parts.length == 4) {
                    model.addRow(parts);
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error loading finance data!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveFinanceData() {
        try (BufferedWriter writer = Files.newBufferedWriter(dbFile.toPath())) {
            for (int i = 0; i < model.getRowCount(); i++) {
                String date = model.getValueAt(i, 0).toString();
                String desc = model.getValueAt(i, 1).toString();
                String type = model.getValueAt(i, 2).toString();
                String amount = model.getValueAt(i, 3).toString();
                writer.write(date + "," + desc + "," + type + "," + amount);
                writer.newLine();
            }
            JOptionPane.showMessageDialog(this, "Finance data saved to: " + dbFile.getAbsolutePath());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving finance data!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateSummary() {
        double totalEarnings = 0;
        double totalCosts = 0;

        for (int i = 0; i < model.getRowCount(); i++) {
            String type = model.getValueAt(i, 2).toString();
            double amount = Double.parseDouble(model.getValueAt(i, 3).toString());
            if ("Earning".equalsIgnoreCase(type)) {
                totalEarnings += amount;
            } else if ("Cost".equalsIgnoreCase(type)) {
                totalCosts += amount;
            }
        }

        double balance = totalEarnings - totalCosts;
        totalEarningsLabel.setText("Earnings: " + totalEarnings);
        totalCostsLabel.setText("Costs: " + totalCosts);
        balanceLabel.setText("Balance: " + balance);
    }

    private void exportToPDF() {
    JFileChooser chooser = new JFileChooser();
    chooser.setSelectedFile(new File("finance_report.pdf"));
    int option = chooser.showSaveDialog(this);
    if (option == JFileChooser.APPROVE_OPTION) {
        File pdfFile = chooser.getSelectedFile();

        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream content = new PDPageContentStream(document, page);
            // content.setFont(, 16);
            content.beginText();
            content.newLineAtOffset(50, 750);
            content.showText("Finance Report");
            content.endText();

            // content.setFont(PDType1Font.HELVETICA, 12);
            content.beginText();
            content.newLineAtOffset(50, 730);
            content.showText("Generated on: " + new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
            content.endText();

            int y = 700;
            // Table header
            String[] headers = {"Date", "Description", "Type", "Amount"};
            content.beginText();
            content.newLineAtOffset(50, y);
            content.showText(String.join(" | ", headers));
            content.endText();

            y -= 20;
            // Table rows
            for (int i = 0; i < model.getRowCount(); i++) {
                String row = model.getValueAt(i, 0).toString() + " | "
                        + model.getValueAt(i, 1).toString() + " | "
                        + model.getValueAt(i, 2).toString() + " | "
                        + model.getValueAt(i, 3).toString();
                content.beginText();
                content.newLineAtOffset(50, y);
                content.showText(row);
                content.endText();
                y -= 20;
            }

            // Summary
            y -= 20;
            content.beginText();
            content.newLineAtOffset(50, y);
            content.showText(totalEarningsLabel.getText() + " | " +
                             totalCostsLabel.getText() + " | " +
                             balanceLabel.getText());
            content.endText();

            content.close();
            document.save(pdfFile);
            JOptionPane.showMessageDialog(this, "Exported to: " + pdfFile.getAbsolutePath());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error exporting to PDF!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
}
