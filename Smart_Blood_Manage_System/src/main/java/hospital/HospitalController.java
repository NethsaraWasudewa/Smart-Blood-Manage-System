package hospital;

import database.databaseConnectors;
import user.EmailEngine;
import java.sql.*;

public class HospitalController {

    public boolean registerPatient(String hospitalName, String patientName, String bloodGroup, String wardNumber) {
        String sql = "INSERT INTO Patients (hospital_name, patient_name, blood_group, ward_number) VALUES (?, ?, ?, ?)";
        try (Connection conn = databaseConnectors.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hospitalName);
            pstmt.setString(2, patientName);
            pstmt.setString(3, bloodGroup);
            pstmt.setString(4, wardNumber);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean submitRequest(String hospitalName, String city, int patientId, String bloodGroup, int quantity, String urgency) {
        String sql = "INSERT INTO Requests (hospital_name, patient_id, blood_group, urgency_level, quantity) VALUES (?, ?, ?, ?, ?)";
        
        // UPGRADED: Added Statement.RETURN_GENERATED_KEYS to capture the new Request ID
        try (Connection conn = databaseConnectors.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setString(1, hospitalName);
            pstmt.setInt(2, patientId);
            pstmt.setString(3, bloodGroup);
            pstmt.setString(4, urgency);
            pstmt.setInt(5, quantity);

            boolean success = pstmt.executeUpdate() > 0;

            if (success && urgency.equals("Emergency")) {
                // Get the newly generated Request ID
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int newRequestId = generatedKeys.getInt(1);
                        // Trigger emails AND log them
                        triggerEmergencyAlerts(newRequestId, hospitalName, city, bloodGroup);
                    }
                }
            }
            return success;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // UPGRADED: Now accepts the request ID and logs the action to the database
    private void triggerEmergencyAlerts(int requestId, String hospitalName, String city, String bloodGroup) {
        String searchSql = "SELECT name, email FROM Donors WHERE blood_group = ? AND location = ? " +
                           "AND (last_donation_date IS NULL OR last_donation_date <= DATE_SUB(CURDATE(), INTERVAL 6 MONTH))";
        
        String logSql = "INSERT INTO Emergency_Logs (request_id, donor_name, donor_email) VALUES (?, ?, ?)";

        try (Connection conn = databaseConnectors.getConnection();
             PreparedStatement searchStmt = conn.prepareStatement(searchSql);
             PreparedStatement logStmt = conn.prepareStatement(logSql)) {

            searchStmt.setString(1, bloodGroup);
            searchStmt.setString(2, city);
            ResultSet rs = searchStmt.executeQuery();

            while (rs.next()) {
                String donorName = rs.getString("name");
                String donorEmail = rs.getString("email");

                // 1. Send the Email
                String subject = "URGENT: Emergency " + bloodGroup + " Blood Request in " + city + "!";
                String body = "<h2 style='color:#e74c3c;'>Emergency Blood Request Alert</h2>"
                            + "<p>Dear <b>" + donorName + "</b>,</p>"
                            + "<p>A patient at <b>" + hospitalName + "</b> in <b>" + city + "</b> is in critical need of <b>" + bloodGroup + "</b> blood.</p>"
                            + "<p>You are receiving this alert because you are listed as an eligible donor in this area.</p>"
                            + "<p style='font-size:14px; font-weight:bold;'>Please contact your local blood bank immediately if you can help save a life today.</p>";

                EmailEngine.sendEmailInBackground(donorEmail, subject, body);

                // 2. Log it to the Database for the Admin
                logStmt.setInt(1, requestId);
                logStmt.setString(2, donorName);
                logStmt.setString(3, donorEmail);
                logStmt.executeUpdate(); 
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}