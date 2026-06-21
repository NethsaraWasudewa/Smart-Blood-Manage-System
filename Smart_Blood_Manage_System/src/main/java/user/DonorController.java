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
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
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
                if (lastDate == null) return true; 
                LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
                return lastDate.toLocalDate().isBefore(sixMonthsAgo) || lastDate.toLocalDate().isEqual(sixMonthsAgo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public int loginDonor(String email) {
        String sql = "SELECT donor_id FROM Donors WHERE email = ?";
        try (Connection conn = databaseConnectors.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, email);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) return rs.getInt("donor_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1; 
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