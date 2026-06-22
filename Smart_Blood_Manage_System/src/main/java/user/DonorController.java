package user;

import database.databaseConnectors;
import java.sql.*;
import java.time.LocalDate;

public class DonorController {

    // Upgraded to handle 'null' if a user has never donated before
    public boolean registerDonor(String name, String email, String bloodGroup, String location, LocalDate lastDonation) {
        String sql = "INSERT INTO Donors (name, email, blood_group, location, last_donation_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = databaseConnectors.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, bloodGroup);
            pstmt.setString(4, location);
            
            // Safe Null Handling
            if (lastDonation != null) {
                pstmt.setDate(5, Date.valueOf(lastDonation));
            } else {
                pstmt.setNull(5, Types.DATE);
            }
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // EXISTING: Checks eligibility for EXISTING users in the database
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

    // NEW BUG FIX: Checks eligibility for NEW users based on the form input!
    public boolean isEligibleToDonateFormDate(LocalDate providedDate) {
        if (providedDate == null) return true; // Never donated before = Eligible!
        
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        return providedDate.isBefore(sixMonthsAgo) || providedDate.isEqual(sixMonthsAgo);
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