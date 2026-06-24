package Admin;

import database.databaseConnectors;
import java.sql.*;
import java.time.LocalDate;

public class AdminController {

    public boolean loginAdmin(String email, String password) {
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

    public boolean confirmDelivery(int deliveryId, int requestId) {
        String updateDelivery = "UPDATE Deliveries SET status = 'Delivered' WHERE delivery_id = ?";
        String updateRequest = "UPDATE Requests SET status = 'Fulfilled' WHERE request_id = ?";

        try (Connection conn = databaseConnectors.getConnection()) {
            conn.setAutoCommit(false); 
            
            try (PreparedStatement pstmt1 = conn.prepareStatement(updateDelivery);
                 PreparedStatement pstmt2 = conn.prepareStatement(updateRequest)) {

                pstmt1.setInt(1, deliveryId);
                pstmt1.executeUpdate();

                pstmt2.setInt(1, requestId);
                pstmt2.executeUpdate();

                conn.commit(); 
                return true;
            } catch (SQLException ex) {
                conn.rollback(); 
                throw ex;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // --- NEW: EDIT REQUEST ENGINE ---
    public boolean updateRequest(int requestId, String hospitalName, int patientId, String bloodGroup, String urgency, int quantity, String status) {
        String sql = "UPDATE Requests SET hospital_name = ?, patient_id = ?, blood_group = ?, urgency_level = ?, quantity = ?, status = ? WHERE request_id = ?";
        try (Connection conn = databaseConnectors.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, hospitalName);
            pstmt.setInt(2, patientId);
            pstmt.setString(3, bloodGroup);
            pstmt.setString(4, urgency);
            pstmt.setInt(5, quantity);
            pstmt.setString(6, status);
            pstmt.setInt(7, requestId);
            
            return pstmt.executeUpdate() > 0;
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}