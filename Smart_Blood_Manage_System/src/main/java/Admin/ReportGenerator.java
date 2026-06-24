package Admin;

import database.databaseConnectors;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;

public class ReportGenerator {

    public boolean generateHospitalReport(String destinationPath) {
        // Create a new PDF document
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, new FileOutputStream(destinationPath));
            document.open();

            // 1. Add Title & Metadata
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Smart Blood Allocation System", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Font subTitleFont = FontFactory.getFont(FontFactory.HELVETICA, 12);
            Paragraph subTitle = new Paragraph("Official Hospital Requests Report\nGenerated on: " + new Date().toString() + "\n\n", subTitleFont);
            subTitle.setAlignment(Element.ALIGN_CENTER);
            document.add(subTitle);

            // 2. Create the Table (6 Columns for Requests)
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);

            // 3. Add Table Headers
            Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            String[] headers = {"Req ID", "Hospital", "Blood Group", "Urgency", "Qty", "Status"};
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // 4. Fetch Data from Database and Populate Table
            String sql = "SELECT request_id, hospital_name, blood_group, urgency_level, quantity, status FROM Requests ORDER BY request_id DESC";
            try (Connection conn = databaseConnectors.getConnection(); 
                 Statement stmt = conn.createStatement(); 
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                Font rowFont = FontFactory.getFont(FontFactory.HELVETICA, 11);
                
                while (rs.next()) {
                    table.addCell(new Phrase(String.valueOf(rs.getInt("request_id")), rowFont));
                    table.addCell(new Phrase(rs.getString("hospital_name"), rowFont));
                    table.addCell(new Phrase(rs.getString("blood_group"), rowFont));
                    table.addCell(new Phrase(rs.getString("urgency_level"), rowFont));
                    table.addCell(new Phrase(String.valueOf(rs.getInt("quantity")), rowFont));
                    table.addCell(new Phrase(rs.getString("status"), rowFont));
                }
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }

            // 5. Add Table to Document
            document.add(table);
            document.close();
            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}