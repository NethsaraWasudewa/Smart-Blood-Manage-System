package hospital;

import database.databaseConnectors;
import java.sql.*;

public class HospitalController {

    public void requestBlood(String hospitalName, String bloodGroup, String urgency, int quantity) {
        String sql = "INSERT INTO Requests (hospital_name, blood_group, urgency_level, quantity, status) VALUES (?, ?, ?, ?, 'Pending')";
        
        try (Connection conn = databaseConnectors.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, hospitalName);
            pstmt.setString(2, bloodGroup);
            pstmt.setString(3, urgency);
            pstmt.setInt(4, quantity);
            
            pstmt.executeUpdate();
            System.out.println("Blood request submitted for " + hospitalName);
            
            // If it's an emergency, notify the system!
            if (urgency.equalsIgnoreCase("Emergency")) {
                System.out.println("ALERT: Triggering Emergency Engine...");
                // Member 5's code will pick this up
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}