package financeManager;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.font.T;;;

public class Export {

    public void exportPdf(DefaultTableModel model, SimpleDateFormat sdf, JLabel logLabel) {
        String[] options = { "This Month", "All" };
        int choice = JOptionPane.showOptionDialog(null, "Export PDF", "Choose Export Range", 0,
                JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
        if (choice == -1) return;

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            PDPageContentStream cs = new PDPageContentStream(doc, page);
            float y = 750;

            // Fonts
            PDFont titleFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
            PDFont textFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            // Title
            cs.beginText();
            cs.setFont(titleFont, 16);
            cs.newLineAtOffset(50, y);
            cs.showText("Finance Report");
            cs.endText();

            y -= 30;

            // Column positions
            float xDate = 50;
            float xCategory = 150;
            float xAmount = 250;
            float xDescription = 350;

            // Header
            cs.beginText();
            cs.setFont(titleFont, 12);
            cs.newLineAtOffset(xDate, y);
            cs.showText("Date");
            cs.newLineAtOffset(xCategory - xDate, 0);
            cs.showText("Category");
            cs.newLineAtOffset(xAmount - xCategory, 0);
            cs.showText("Amount");
            cs.newLineAtOffset(xDescription - xAmount, 0);
            cs.showText("Description");
            cs.endText();

            y -= 20;

            double totalIncome = 0;
            double totalExpense = 0;

            for (int i = 0; i < model.getRowCount(); i++) {
                if (y < 70) { // new page
                    cs.close();
                    page = new PDPage(PDRectangle.A4);
                    doc.addPage(page);
                    cs = new PDPageContentStream(doc, page);
                    y = 750;
                }

                Date date = (Date) model.getValueAt(i, 0);
                String category = model.getValueAt(i, 1).toString();
                double amount = Double.parseDouble(model.getValueAt(i, 2).toString());
                String description = model.getValueAt(i, 3).toString();

                if (category.equalsIgnoreCase("Income")) totalIncome += amount;
                if (category.equalsIgnoreCase("Expense")) totalExpense += amount;

                cs.beginText();
                cs.setFont(textFont, 11);
                cs.newLineAtOffset(xDate, y);
                cs.showText(sdf.format(date));
                cs.newLineAtOffset(xCategory - xDate, 0);
                cs.showText(category);
                cs.newLineAtOffset(xAmount - xCategory, 0);
                cs.showText(String.format("%.2f", amount));
                cs.newLineAtOffset(xDescription - xAmount, 0);
                cs.showText(description);
                cs.endText();

                y -= 15;
            }

            // Summary
            y -= 20;
            cs.beginText();
            cs.setFont(titleFont, 12);
            cs.newLineAtOffset(50, y);
            cs.showText("Summary:");
            cs.endText();

            y -= 15;
            cs.beginText();
            cs.setFont(textFont, 11);
            cs.newLineAtOffset(50, y);
            cs.showText(String.format("Total Income: %.2f", totalIncome));
            cs.endText();

            y -= 15;
            cs.beginText();
            cs.setFont(textFont, 11);
            cs.newLineAtOffset(50, y);
            cs.showText(String.format("Total Expense: %.2f", totalExpense));
            cs.endText();

            y -= 15;
            cs.beginText();
            cs.setFont(textFont, 11);
            cs.newLineAtOffset(50, y);
            cs.showText(String.format("Net Balance: %.2f", totalIncome - totalExpense));
            cs.endText();

            cs.close();

            File outputFile = new File(System.getProperty("user.home"), "finance_report.pdf");
            doc.save(outputFile);

            logLabel.setText("PDF exported successfully!");
        } catch (Exception ex) {
            logLabel.setText("Error exporting: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
