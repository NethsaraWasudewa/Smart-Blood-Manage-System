package user;

import database.databaseConnectors;
import java.sql.*;
import java.time.LocalDate;

public class DonorController {

    // Safely handles new user registrations (including null/blank dates)
    public boolean registerDonor(String name, String email, String bloodGroup, String location, LocalDate lastDonation) {
        String sql = "INSERT INTO Donors (name, email, blood_group, location, last_donation_date) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = databaseConnectors.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, email);
            pstmt.setString(3, bloodGroup);
            pstmt.setString(4, location);
            
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

    // Used purely for the registration screen to prevent instant invalid sign-ups
    public boolean isEligibleToDonateFormDate(LocalDate providedDate) {
        if (providedDate == null) return true; 
        LocalDate sixMonthsAgo = LocalDate.now().minusMonths(6);
        return providedDate.isBefore(sixMonthsAgo) || providedDate.isEqual(sixMonthsAgo);
    }

    // --- NEW: THE ADVANCED EVENT ELIGIBILITY ENGINE ---
    public boolean checkEventEligibility(int donorId, LocalDate targetEventDate) {
        try (Connection conn = databaseConnectors.getConnection()) {
            
            // 1. Check against their last physical past donation
            String sql1 = "SELECT last_donation_date FROM Donors WHERE donor_id = ?";
            try (PreparedStatement pstmt1 = conn.prepareStatement(sql1)) {
                pstmt1.setInt(1, donorId);
                ResultSet rs1 = pstmt1.executeQuery();
                if (rs1.next()) {
                    Date lastDate = rs1.getDate("last_donation_date");
                    if (lastDate != null) {
                        LocalDate lastDonation = lastDate.toLocalDate();
                        // If the event happens less than 6 months after their last donation, block it.
                        if (targetEventDate.isBefore(lastDonation.plusMonths(6))) {
                            return false; 
                        }
                    }
                }
            }
            
            // 2. Check against ALL other events this user is already registered for
            String sql2 = "SELECT e.event_date FROM Events e JOIN Event_Registrations r ON e.event_id = r.event_id WHERE r.donor_id = ?";
            try (PreparedStatement pstmt2 = conn.prepareStatement(sql2)) {
                pstmt2.setInt(1, donorId);
                ResultSet rs2 = pstmt2.executeQuery();
                
                while (rs2.next()) {
                    LocalDate registeredDate = rs2.getDate("event_date").toLocalDate();
                    
                    // Create a 6-month "buffer zone" around the target event
                    LocalDate bufferStart = targetEventDate.minusMonths(6);
                    LocalDate bufferEnd = targetEventDate.plusMonths(6);
                    
                    // If they are already registered for an event inside this buffer zone, block it!
                    if (registeredDate.isAfter(bufferStart) && registeredDate.isBefore(bufferEnd)) {
                        return false; 
                    }
                }
            }
            return true; // Passed all checks!
            
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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