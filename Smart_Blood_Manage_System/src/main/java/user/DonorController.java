package user;

import database.databaseConnectors;
import java.sql.*;
import java.time.LocalDate;

public class DonorController {

    public boolean registerDonor(String name, String email, String password, String bloodGroup, String location, LocalDate lastDonation) {
        // FIXED: Now inserts into 'account_password'
        String sql = "INSERT INTO Donors (name, email, account_password, blood_group, location, last_donation_date) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = databaseConnectors.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, password);
            pstmt.setString(4, bloodGroup);
            pstmt.setString(5, location);
            
            if (lastDonation != null) {
                pstmt.setDate(6, Date.valueOf(lastDonation));
            } else {
                pstmt.setNull(6, Types.DATE);
            }
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int loginDonor(String email, String password) {
        // FIXED: Now queries 'account_password'
        String sql = "SELECT donor_id FROM Donors WHERE email = ? AND account_password = ?";
        try (Connection conn = databaseConnectors.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("donor_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; 
    }

    public boolean cancelRegistration(int donorId, String eventName) {
        String sql = "DELETE r FROM Event_Registrations r JOIN Events e ON r.event_id = e.event_id WHERE r.donor_id = ? AND e.event_name = ?";
        try (Connection conn = databaseConnectors.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, donorId);
            pstmt.setString(2, eventName);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isEligibleToDonateFormDate(LocalDate providedDate) {
        if (providedDate == null) return true; 
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        return providedDate.isBefore(sixMonthsAgo) || providedDate.isEqual(sixMonthsAgo);
    }

    public boolean checkEventEligibility(int donorId, LocalDate targetEventDate) {
        try (Connection conn = databaseConnectors.getConnection()) {
            String sql1 = "SELECT last_donation_date FROM Donors WHERE donor_id = ?";
            try (PreparedStatement pstmt1 = conn.prepareStatement(sql1)) {
                pstmt1.setInt(1, donorId);
                ResultSet rs1 = pstmt1.executeQuery();
                if (rs1.next()) {
                    Date lastDate = rs1.getDate("last_donation_date");
                    if (lastDate != null) {
                        if (targetEventDate.isBefore(lastDate.toLocalDate().plusMonths(6))) return false; 
                    }
                }
            }
            
            String sql2 = "SELECT e.event_date FROM Events e JOIN Event_Registrations r ON e.event_id = r.event_id WHERE r.donor_id = ?";
            try (PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
                pstmt2.setInt(1, donorId);
                ResultSet rs2 = pstmt2.executeQuery();
                while (rs2.next()) {
                    LocalDate registeredDate = rs2.getDate("event_date").toLocalDate();
                    LocalDate bufferStart = targetEventDate.minusMonths(6);
                    LocalDate bufferEnd = targetEventDate.plusMonths(6);
                    if (registeredDate.isAfter(bufferStart) && registeredDate.isBefore(bufferEnd)) return false; 
                }
            }
            return true; 
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String[] getDonorDetails(int donorId) {
        String sql = "SELECT name, blood_group, last_donation_date FROM Donors WHERE donor_id = ?";
        try (Connection conn = databaseConnectors.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, donorId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String dateStr = rs.getDate("last_donation_date") != null ? rs.getDate("last_donation_date").toString() : "No Record";
                return new String[]{rs.getString("name"), rs.getString("blood_group"), dateStr};
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean registerForEvent(int donorId, int eventId) {
        String sql = "INSERT INTO Event_Registrations (donor_id, event_id) VALUES (?, ?)";
        try (Connection conn = databaseConnectors.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, donorId);
            pstmt.setInt(2, eventId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            return false; 
        }
    }
}