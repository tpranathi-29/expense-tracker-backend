package com.example.expense_tracker.pdf;

import com.example.expense_tracker.model.Expense;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;

import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.List;

@Service
public class PdfService {

    public byte[] generateExpenseReport(
            List<Expense> expenses,
            String email) {

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {

            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    22);

            Font headingFont = FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    12);

            Font normalFont = FontFactory.getFont(
                    FontFactory.HELVETICA,
                    11);

            // ===============================
            // Title
            // ===============================

            Paragraph title = new Paragraph(
                    "Expense Tracker Report",
                    titleFont);

            title.setAlignment(Element.ALIGN_CENTER);

            document.add(title);
            document.add(new Paragraph(" "));

            document.add(new Paragraph(
                    "User : " + email,
                    headingFont));

            document.add(new Paragraph(
                    "Generated On : " + LocalDate.now(),
                    headingFont));

            document.add(new Paragraph(" "));

            // ===============================
            // Expense Table
            // ===============================

            PdfPTable table = new PdfPTable(6);

            table.setWidthPercentage(100);

            table.setWidths(new float[]{
                    3f,
                    2f,
                    2f,
                    2f,
                    2f,
                    4f
            });

            addHeader(table, "Title");
            addHeader(table, "Category");
            addHeader(table, "Type");
            addHeader(table, "Amount");
            addHeader(table, "Date");
            addHeader(table, "Description");

            double totalIncome = 0;
            double totalExpense = 0;

            for (Expense expense : expenses) {

                table.addCell(new Phrase(
                        expense.getTitle() == null ? "" : expense.getTitle(),
                        normalFont));

                table.addCell(new Phrase(
                        expense.getCategory() == null ? "" : expense.getCategory(),
                        normalFont));

                table.addCell(new Phrase(
                        expense.getType() == null ? "" : expense.getType(),
                        normalFont));

                table.addCell(new Phrase(
                        String.format("%.2f", expense.getAmount()),
                        normalFont));

                table.addCell(new Phrase(
                        expense.getDate().toString(),
                        normalFont));

                table.addCell(new Phrase(
                        expense.getDescription() == null
                                ? ""
                                : expense.getDescription(),
                        normalFont));

                if ("Income".equalsIgnoreCase(expense.getType())) {

                    totalIncome += expense.getAmount();

                } else {

                    totalExpense += expense.getAmount();

                }
            }

            document.add(table);

            document.add(new Paragraph(" "));

            // ===============================
            // Summary
            // ===============================

            document.add(new Paragraph(
                    "Summary",
                    headingFont));

            document.add(new Paragraph(
                    "Total Income : "
                            + String.format("%.2f", totalIncome),
                    normalFont));

            document.add(new Paragraph(
                    "Total Expense : "
                            + String.format("%.2f", totalExpense),
                    normalFont));

            document.add(new Paragraph(
                    "Balance : "
                            + String.format(
                                    "%.2f",
                                    totalIncome - totalExpense),
                    normalFont));

            document.add(new Paragraph(" "));

            document.add(new Paragraph(
                    "Thank you for using Expense Tracker!",
                    headingFont));

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error while generating PDF report",
                    e);

        } finally {

            if (document.isOpen()) {
                document.close();
            }
        }

        return out.toByteArray();
    }

    // ==========================================
    // Helper Method
    // ==========================================

    private void addHeader(
            PdfPTable table,
            String text) {

        Font font = FontFactory.getFont(
                FontFactory.HELVETICA_BOLD,
                11);

        PdfPCell cell = new PdfPCell(
                new Phrase(text, font));

        cell.setHorizontalAlignment(
                Element.ALIGN_CENTER);

        cell.setPadding(8);

        table.addCell(cell);
    }

}