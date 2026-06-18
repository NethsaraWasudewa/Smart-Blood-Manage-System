package emergency;

import database.databaseConnectors;
import java.sql.*;

public class EmergencyEngine {

    public void triggerEmergencyEmails(String requiredBloodGroup, String location) {
        // SQL checks blood group, location, and the 6-month rule
        String sql = "SELECT name, email FROM Donors WHERE blood_group = ? AND location = ? AND (last_donation_date IS NULL OR last_donation_date <= DATE_SUB(CURDATE(), INTERVAL 6 MONTH))";
        
        try (Connection conn = databaseConnectors.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, requiredBloodGroup);
            pstmt.setString(2, location);
            ResultSet rs = pstmt.executeQuery();
            
            boolean donorsFound = false;
            
            while (rs.next()) {
                donorsFound = true;
                String donorName = rs.getString("name");
                String donorEmail = rs.getString("email");
                
                // Print to console (In reality, use JavaMail API here)
                System.out.println("-------------------------------------------------");
                System.out.println("Sending Emergency Email to: " + donorEmail);
                System.out.println("Subject: URGENT: " + requiredBloodGroup + " Needed at " + location);
                System.out.println("Message: Hello " + donorName + ", a critical emergency has occurred in your area. According to our records, you are eligible to donate. Please visit the nearest hospital immediately.");
                System.out.println("-------------------------------------------------");
            }
            
            if (!donorsFound) {
                System.out.println("No eligible donors found in " + location + " for blood type " + requiredBloodGroup);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}