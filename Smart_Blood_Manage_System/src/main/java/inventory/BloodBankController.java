package inventory;

import database.databaseConnectors;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BloodBankController {

    public void addBloodBag(String bloodGroup) {
        LocalDate today = LocalDate.now();
        LocalDate expiryDate = today.plusDays(42); 
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

    // --- NEW: DOUBLE-BOOKING PREVENTION SYSTEM ---
    public boolean allocateBlood(int requestId, String bloodGroup, int quantity) {
        // 'FOR UPDATE' applies the pessimistic lock!
        String selectBlood = "SELECT bag_id FROM Inventory WHERE blood_group = ? AND status = 'Available' ORDER BY expiry_date ASC LIMIT ? FOR UPDATE";
        String updateBlood = "UPDATE Inventory SET status = 'Dispatched' WHERE bag_id = ?";
        String updateRequest = "UPDATE Requests SET status = 'Approved' WHERE request_id = ?";
        String insertDelivery = "INSERT INTO Deliveries (request_id, driver_name, status) VALUES (?, 'System Auto-Dispatch Courier', 'In Transit')";

        try (Connection conn = databaseConnectors.getConnection()) {
            // 1. Pause auto-saving. Start the locked transaction.
            conn.setAutoCommit(false); 

            try (PreparedStatement pstmtSelect = conn.prepareStatement(selectBlood)) {
                pstmtSelect.setString(1, bloodGroup);
                pstmtSelect.setInt(2, quantity);
                ResultSet rs = pstmtSelect.executeQuery();

                List<Integer> bagsToAllocate = new ArrayList<>();
                while (rs.next()) {
                    bagsToAllocate.add(rs.getInt("bag_id"));
                }

                // 2. Double Booking Check: Did another hospital steal the blood while we were loading?
                if (bagsToAllocate.size() < quantity) {
                    conn.rollback(); // Cancel the whole transaction instantly
                    return false; 
                }

                // 3. Update the exact bags
                try (PreparedStatement pstmtUpdateBlood = conn.prepareStatement(updateBlood)) {
                    for (int bagId : bagsToAllocate) {
                        pstmtUpdateBlood.setInt(1, bagId);
                        pstmtUpdateBlood.executeUpdate();
                    }
                }

                // 4. Update the Hospital Request
                try (PreparedStatement pstmtReq = conn.prepareStatement(updateRequest)) {
                    pstmtReq.setInt(1, requestId);
                    pstmtReq.executeUpdate();
                }

                // 5. Create the Delivery Record
                try (PreparedStatement pstmtDel = conn.prepareStatement(insertDelivery)) {
                    pstmtDel.setInt(1, requestId);
                    pstmtDel.executeUpdate();
                }

                // 6. Everything was successful. Release the lock and save!
                conn.commit(); 
                return true;

            } catch (SQLException ex) {
                conn.rollback(); // If anything fails, abort and unlock
                throw ex;
            } finally {
                conn.setAutoCommit(true); // Return database to normal
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}