package UI; // Updated to match the capital letters in your screenshot

import hospital.HospitalController;
import database.databaseConnectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class HospitalRequestFrame extends JFrame {
    private DefaultTableModel capacityModel;
    private DefaultTableModel deliveryModel;

    public HospitalRequestFrame() {
        setTitle("Hospital Portal - Blood Requests & Deliveries");
        setSize(700, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        // --- TAB 1: Live Blood Capacity ---
        JPanel pnlCapacity = new JPanel(new BorderLayout());
        capacityModel = new DefaultTableModel(new String[]{"Blood Group", "Safe & Available Bags"}, 0);
        JTable capacityTable = new JTable(capacityModel);
        pnlCapacity.add(new JScrollPane(capacityTable), BorderLayout.CENTER);

        // --- TAB 2: Register Patient (NEW ENTITY) ---
        JPanel pnlPatient = new JPanel(new GridLayout(6, 2, 5, 5));
        pnlPatient.add(new JLabel("Hospital Name:"));
        JTextField txtHospPatient = new JTextField();
        pnlPatient.add(txtHospPatient);
        
        pnlPatient.add(new JLabel("Patient Full Name:"));
        JTextField txtPatName = new JTextField();
        pnlPatient.add(txtPatName);
        
        pnlPatient.add(new JLabel("Patient Blood Group:"));
        JComboBox<String> cmbPatBlood = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        pnlPatient.add(cmbPatBlood);
        
        pnlPatient.add(new JLabel("Ward Number:"));
        JTextField txtWard = new JTextField();
        pnlPatient.add(txtWard);
        
        JButton btnRegPatient = new JButton("Register Patient");
        pnlPatient.add(btnRegPatient);

        btnRegPatient.addActionListener(e -> {
            new HospitalController().registerPatient(txtHospPatient.getText(), txtPatName.getText(), cmbPatBlood.getSelectedItem().toString(), txtWard.getText());
            JOptionPane.showMessageDialog(this, "Patient Registered Successfully. Check Database for Patient ID.");
        });

        // --- TAB 3: Make a Request (UPDATED TO REQUIRE PATIENT ID) ---
        JPanel pnlRequest = new JPanel(new GridLayout(7, 2, 5, 5));
        pnlRequest.add(new JLabel("Hospital Name:"));
        JTextField txtHospital = new JTextField();
        pnlRequest.add(txtHospital);
        
        // This is the new field needed to fix your error
        pnlRequest.add(new JLabel("Patient ID (Required):"));
        JTextField txtPatientId = new JTextField();
        pnlRequest.add(txtPatientId);
        
        pnlRequest.add(new JLabel("Required Blood Group:"));
        JComboBox<String> cmbBloodGroup = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        pnlRequest.add(cmbBloodGroup);
        
        pnlRequest.add(new JLabel("Quantity (Bags):"));
        JSpinner spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        pnlRequest.add(spnQuantity);
        
        pnlRequest.add(new JLabel("Urgency Level:"));
        JComboBox<String> cmbUrgency = new JComboBox<>(new String[]{"Standard", "Emergency"});
        pnlRequest.add(cmbUrgency);
        
        JButton btnSubmit = new JButton("Submit Request");
        pnlRequest.add(btnSubmit);

        // This action listener now correctly captures the Patient ID and sends 5 arguments
        btnSubmit.addActionListener(e -> {
            try {
                int pId = Integer.parseInt(txtPatientId.getText());
                new HospitalController().requestBlood(txtHospital.getText(), cmbBloodGroup.getSelectedItem().toString(), cmbUrgency.getSelectedItem().toString(), (Integer) spnQuantity.getValue(), pId);
                JOptionPane.showMessageDialog(this, "Request Submitted to Blood Bank.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Error: Patient ID must be a number.");
            }
        });

        // --- TAB 4: Track Deliveries (NEW ENTITY) ---
        JPanel pnlDeliveries = new JPanel(new BorderLayout());
        deliveryModel = new DefaultTableModel(new String[]{"Delivery ID", "Req ID", "Driver", "Status"}, 0);
        JTable deliveryTable = new JTable(deliveryModel);
        pnlDeliveries.add(new JScrollPane(deliveryTable), BorderLayout.CENTER);
        JButton btnRefreshData = new JButton("Refresh System Data");
        btnRefreshData.addActionListener(e -> loadTables());
        pnlDeliveries.add(btnRefreshData, BorderLayout.SOUTH);

        // Add all tabs to the main panel
        tabbedPane.add("Live Capacity", pnlCapacity);
        tabbedPane.add("Register Patient", pnlPatient);
        tabbedPane.add("Send Request", pnlRequest);
        tabbedPane.add("Track Deliveries", pnlDeliveries);
        add(tabbedPane, BorderLayout.CENTER);

        // Global Back Button
        JPanel bottomPanel = new JPanel();
        JButton btnBack = new JButton("Back to Home");
        btnBack.addActionListener(e -> {
            new StartScreenFrame().setVisible(true);
            this.dispose();
        });
        bottomPanel.add(btnBack);
        add(bottomPanel, BorderLayout.SOUTH);

        loadTables();
    }

    private void loadTables() {
        capacityModel.setRowCount(0);
        deliveryModel.setRowCount(0);
        
        try (Connection conn = databaseConnectors.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Load live stock capacity
            ResultSet rs1 = stmt.executeQuery("SELECT blood_group, COUNT(*) as amount FROM Inventory WHERE status = 'Available' GROUP BY blood_group");
            while (rs1.next()) {
                capacityModel.addRow(new Object[]{rs1.getString("blood_group"), rs1.getInt("amount")});
            }

            // Load delivery statuses
            ResultSet rs2 = stmt.executeQuery("SELECT delivery_id, request_id, driver_name, status FROM Deliveries");
            while (rs2.next()) {
                deliveryModel.addRow(new Object[]{rs2.getInt("delivery_id"), rs2.getInt("request_id"), rs2.getString("driver_name"), rs2.getString("status")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}