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
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

public class Export {
void exportPdf(DefaultTableModel model, SimpleDateFormat sdf, JLabel logLabel, String total) {
    String[] options = {"This Month", "All"};
    int choice = JOptionPane.showOptionDialog(null, "Export PDF", "Choose", 0,
            JOptionPane.PLAIN_MESSAGE, null, options, options[0]);
    if (choice == -1) return;

    try (PDDocument doc = new PDDocument()) {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);

        PDPageContentStream cs = new PDPageContentStream(doc, page);
        float margin = 50;
        float y = page.getMediaBox().getHeight() - margin;

        PDFont fTitle = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDFont fHeader = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDFont fText = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        // Title
        cs.beginText();
        cs.setFont(fTitle, 18);
        cs.newLineAtOffset(margin, y);
        cs.showText("Finance Report");
        cs.endText();

        y -= 25;

        // Subtitle (date generated)
        cs.beginText();
        cs.setFont(fText, 10);
        cs.newLineAtOffset(margin, y);
        cs.showText("Generated on: " + new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        cs.endText();

        y -= 30;

        // Column headers
        String[] headers = {"Date", "Description", "Category", "Amount"};
        float[] colWidths = {100, 120, 80, 200};
        float x = margin;

        for (int i = 0; i < headers.length; i++) {
            cs.beginText();
            cs.setFont(fHeader, 12);
            cs.newLineAtOffset(x, y);
            cs.showText(headers[i]);
            cs.endText();
            x += colWidths[i];
        }

        y -= 20;

        double totalIncome = 0;
        double totalExpense = 0;

        // Draw table rows
        for (int i = 0; i < model.getRowCount(); i++) {
            if (y < 70) { // new page
                cs.close();
                page = new PDPage(PDRectangle.A4);
                doc.addPage(page);
                cs = new PDPageContentStream(doc, page);
                y = page.getMediaBox().getHeight() - margin;
            }

            x = margin;
            String[] values = {
                sdf.format((Date) model.getValueAt(i, 0)),
                model.getValueAt(i, 1).toString(),
                model.getValueAt(i, 2).toString(),
                model.getValueAt(i, 3).toString()
            };

            // Sum income/expense based on category (example: "Income"/"Expense")
            try {
                // values: [0]=Date, [1]=Description, [2]=Category, [3]=Amount
                double amt = Double.parseDouble(values[3]);
                String category = values[2].toLowerCase();
                if (category.contains("income")) totalIncome += amt;
                else totalExpense += amt;
            } catch (Exception ignored) {}

            for (int j = 0; j < values.length; j++) {
                cs.beginText();
                cs.setFont(fText, 10);
                cs.newLineAtOffset(x, y);
                cs.showText(values[j]);
                cs.endText();
                x += colWidths[j];
            }

            y -= 15;
        }

    // Totals / Summary
    y -= 20;
    cs.beginText();
    cs.setFont(fHeader, 12);
    cs.newLineAtOffset(margin, y);
    // show any summary string passed in
    if (total != null && !total.isEmpty()) cs.showText(total);
    cs.endText();

    y -= 15;
    cs.beginText();
    cs.setFont(fHeader, 12);
    cs.newLineAtOffset(margin, y);
    cs.showText(String.format("Total Income: %.2f", totalIncome));
    cs.endText();

    y -= 15;
    cs.beginText();
    cs.setFont(fHeader, 12);
    cs.newLineAtOffset(margin, y);
    cs.showText(String.format("Total Expense: %.2f", totalExpense));
    cs.endText();

    y -= 15;
    cs.beginText();
    cs.setFont(fHeader, 12);
    cs.newLineAtOffset(margin, y);
    cs.showText(String.format("Net Balance: %.2f", totalIncome - totalExpense));
    cs.endText();

        cs.close();
        doc.save(new File(System.getProperty("user.home"), "finance_report.pdf"));
        logLabel.setText("PDF exported successfully!");
    } catch (Exception ex) {
        logLabel.setText("Error exporting: " + ex.getMessage());
        ex.printStackTrace();
    }
}

}