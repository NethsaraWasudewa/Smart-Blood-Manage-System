package hospital;

import database.databaseConnectors;
import user.EmailEngine;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HospitalController {

    // --- 1. THE NEW MEDICAL COMPATIBILITY ENGINE ---
    // Returns a prioritized list of compatible blood types (Exact match first, universal donors last)
    public List<String> getCompatibleBloodTypes(String patientBlood) {
        List<String> compatible = new ArrayList<>();
        switch (patientBlood) {
            case "A+":  compatible = List.of("A+", "A-", "O+", "O-"); break;
            case "A-":  compatible = List.of("A-", "O-"); break;
            case "B+":  compatible = List.of("B+", "B-", "O+", "O-"); break;
            case "B-":  compatible = List.of("B-", "O-"); break;
            case "AB+": compatible = List.of("AB+", "AB-", "A+", "A-", "B+", "B-", "O+", "O-"); break; // Universal Recipient
            case "AB-": compatible = List.of("AB-", "A-", "B-", "O-"); break;
            case "O+":  compatible = List.of("O+", "O-"); break;
            case "O-":  compatible = List.of("O-"); break; // Universal Donor
        }
        return compatible;
    }

    // --- 2. SMART INVENTORY SCANNER ---
    // Checks the DB for exact matches and compatible alternatives
    public String analyzeInventoryCompatibility(String requestedBlood, int requiredQuantity) {
        List<String> compatibleTypes = getCompatibleBloodTypes(requestedBlood);
        StringBuilder report = new StringBuilder();
        
        try (Connection conn = databaseConnectors.getConnection()) {
            int totalFound = 0;
            boolean exactMatchFound = false;

            for (String bloodType : compatibleTypes) {
                String sql = "SELECT COUNT(*) AS available FROM Inventory WHERE blood_group = ? AND status = 'Available'";
                try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, bloodType);
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next()) {
                        int available = rs.getInt("available");
                        if (available > 0) {
                            if (bloodType.equals(requestedBlood)) {
                                report.append("✓ Exact Match (").append(bloodType).append("): ").append(available).append(" bags available.\n");
                                exactMatchFound = true;
                            } else {
                                report.append("⚠ Compatible Alternative (").append(bloodType).append("): ").append(available).append(" bags available.\n");
                            }
                            totalFound += available;
                        }
                    }
                }
            }

            if (totalFound >= requiredQuantity) {
                if (exactMatchFound) {
                    return "STATUS: OK.\n" + report.toString() + "\nSufficient exact stock is available to fulfill this request.";
                } else {
                    return "STATUS: ALTERNATIVE REQUIRED.\n" + report.toString() + "\nExact match unavailable, but safe compatible alternatives are ready for dispatch.";
                }
            } else if (totalFound > 0) {
                return "STATUS: CRITICAL SHORTAGE.\n" + report.toString() + "\nOnly " + totalFound + " compatible bags found. Request cannot be fully fulfilled.";
            } else {
                return "STATUS: OUT OF STOCK.\nNo exact or compatible blood types (like O-) are currently available in the inventory!";
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return "Error scanning inventory.";
        }
    }

    // --- 3. EXISTING METHODS (Kept Perfectly Intact) ---
    public boolean registerPatient(String hospitalName, String patientName, String bloodGroup, String wardNumber) {
        String sql = "INSERT INTO Patients (hospital_name, patient_name, blood_group, ward_number) VALUES (?, ?, ?, ?)";
        try (Connection conn = databaseConnectors.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hospitalName); pstmt.setString(2, patientName);
            pstmt.setString(3, bloodGroup); pstmt.setString(4, wardNumber);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean submitRequest(String hospitalName, String city, int patientId, String bloodGroup, int quantity, String urgency) {
        String sql = "INSERT INTO Requests (hospital_name, patient_id, blood_group, urgency_level, quantity) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = databaseConnectors.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, hospitalName); pstmt.setInt(2, patientId);
            pstmt.setString(3, bloodGroup); pstmt.setString(4, urgency); pstmt.setInt(5, quantity);
            boolean success = pstmt.executeUpdate() > 0;
            if (success && urgency.equals("Emergency")) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) triggerEmergencyAlerts(generatedKeys.getInt(1), hospitalName, city, bloodGroup);
                }
            }
            return success;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    private void triggerEmergencyAlerts(int requestId, String hospitalName, String city, String bloodGroup) {
        String searchSql = "SELECT name, email FROM Donors WHERE blood_group = ? AND location = ? AND (last_donation_date IS NULL OR last_donation_date <= DATE_SUB(CURDATE(), INTERVAL 6 MONTH))";
        String logSql = "INSERT INTO Emergency_Logs (request_id, donor_name, donor_email) VALUES (?, ?, ?)";
        try (Connection conn = databaseConnectors.getConnection(); PreparedStatement searchStmt = conn.prepareStatement(searchSql); PreparedStatement logStmt = conn.prepareStatement(logSql)) {
            searchStmt.setString(1, bloodGroup); searchStmt.setString(2, city);
            ResultSet rs = searchStmt.executeQuery();
            while (rs.next()) {
                String donorName = rs.getString("name"); String donorEmail = rs.getString("email");
                String subject = "URGENT: Emergency " + bloodGroup + " Blood Request in " + city + "!";
                String body = "<h2 style='color:#e74c3c;'>Emergency Blood Request Alert</h2><p>Dear <b>" + donorName + "</b>,</p><p>A patient at <b>" + hospitalName + "</b> in <b>" + city + "</b> is in critical need of <b>" + bloodGroup + "</b> blood.</p><p>Please contact your local blood bank immediately to help.</p>";
                EmailEngine.sendEmailInBackground(donorEmail, subject, body);
                logStmt.setInt(1, requestId); logStmt.setString(2, donorName); logStmt.setString(3, donorEmail);
                logStmt.executeUpdate(); 
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}