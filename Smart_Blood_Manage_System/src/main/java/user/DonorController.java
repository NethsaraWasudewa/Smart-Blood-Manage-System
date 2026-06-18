package user;

import database.databaseConnectors;
import java.sql.*;
import java.time.LocalDate;

public class DonorController {
    
    public boolean registerDonor(String name, String email, String bloodGroup, String location, LocalDate lastDonation) {
        String sql = "INSERT INTO Donors (name, email, blood_group, location, last_donation_date) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = databaseConnectors.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, bloodGroup);
            pstmt.setString(4, location);
            pstmt.setDate(5, Date.valueOf(lastDonation));
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.out.println("Registration failed: " + e.getMessage());
            return false;
        }
    }

    public boolean isEligibleToDonate(String email) {
        String sql = "SELECT last_donation_date FROM Donors WHERE email = ?";
        try (Connection conn = databaseConnectors.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Date lastDate = rs.getDate("last_donation_date");
                if (lastDate == null) return true; // Never donated
                
                LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
                return lastDate.toLocalDate().isBefore(sixMonthsAgo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}