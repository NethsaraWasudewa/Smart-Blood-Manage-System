package hospital;

import database.databaseConnectors;
import java.sql.*;

public class HospitalController {
    public void registerPatient(String hospitalName, String patientName, String bloodGroup, String ward) {
        String sql = "INSERT INTO Patients (hospital_name, patient_name, blood_group, ward_number) VALUES (?, ?, ?, ?)";
        try (Connection conn = databaseConnectors.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hospitalName);
            pstmt.setString(2, patientName);
            pstmt.setString(3, bloodGroup);
            pstmt.setString(4, ward);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void requestBlood(String hospitalName, String bloodGroup, String urgency, int quantity, int patientId) {
        String sql = "INSERT INTO Requests (hospital_name, blood_group, urgency_level, quantity, status, patient_id) VALUES (?, ?, ?, ?, 'Pending', ?)";
        try (Connection conn = databaseConnectors.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, hospitalName);
            pstmt.setString(2, bloodGroup);
            pstmt.setString(3, urgency);
            pstmt.setInt(4, quantity);
            pstmt.setInt(5, patientId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}