package inventory;

import database.databaseConnectors;
import java.sql.*;
import java.time.LocalDate;

public class BloodBankController {

    public void addBloodBag(String bloodGroup) {
        LocalDate today = LocalDate.now();
        LocalDate expiryDate = today.plusDays(42); 

        String sql = "INSERT INTO Inventory (blood_group, collection_date, expiry_date, status) VALUES (?, ?, ?, 'Available')";
        
        try (Connection conn = databaseConnectors.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, bloodGroup);
            pstmt.setDate(2, Date.valueOf(today));
            pstmt.setDate(3, Date.valueOf(expiryDate));
            
            pstmt.executeUpdate();
            System.out.println(bloodGroup + " bag added. Expires on: " + expiryDate);
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}