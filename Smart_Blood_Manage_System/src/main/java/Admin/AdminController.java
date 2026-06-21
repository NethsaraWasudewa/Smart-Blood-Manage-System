package Admin;

import database.databaseConnectors;
import java.sql.*;
import java.time.LocalDate;

public class AdminController {
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