package inventory;

import database.databaseConnectors;
import java.sql.*;
import java.time.LocalDate;

public class BloodBankController {

    public void addBloodBag(String bloodGroup) {
        LocalDate today = LocalDate.now();
        LocalDate expiryDate = today.plusDays(42); 
        // Notice status is now 'Testing' by default, not 'Available'
        String sql = "INSERT INTO Inventory (blood_group, collection_date, expiry_date, status, screening_status) VALUES (?, ?, ?, 'Testing', 'Pending')";
        
        try (Connection conn = databaseConnectors.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, bloodGroup);
            pstmt.setDate(2, Date.valueOf(today));
            pstmt.setDate(3, Date.valueOf(expiryDate));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void screenBloodBag(String bagId, boolean isSafe) {
        String newStatus = isSafe ? "Available" : "Discarded";
        String newScreening = isSafe ? "Passed" : "Failed Disease Screen";
        
        String sql = "UPDATE Inventory SET status = ?, screening_status = ? WHERE bag_id = ?";
        try (Connection conn = databaseConnectors.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setString(2, newScreening);
            pstmt.setString(3, bagId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}