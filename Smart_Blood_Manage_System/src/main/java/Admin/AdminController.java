package Admin;

import database.databaseConnectors;
import java.sql.*;
import java.time.LocalDate;

public class AdminController {

    public boolean loginAdmin(String email, String password) {
        // FIXED: Now queries 'account_password' instead of 'password'
        String sql = "SELECT * FROM Admins WHERE email = ? AND account_password = ?";
        
        try (Connection conn = databaseConnectors.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); 
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void createDonationEvent(String eventName, String location, LocalDate eventDate, int targetCapacity) {
        String sql = "INSERT INTO Events (event_name, location, event_date, target_capacity) VALUES (?, ?, ?, ?)";
        try (Connection conn = databaseConnectors.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, eventName);
            pstmt.setString(2, location);
            pstmt.setDate(3, Date.valueOf(eventDate));
            pstmt.setInt(4, targetCapacity);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}