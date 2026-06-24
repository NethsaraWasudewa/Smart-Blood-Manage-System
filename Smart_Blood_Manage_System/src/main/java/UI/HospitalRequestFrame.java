package UI;

import hospital.HospitalController;
import database.databaseConnectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class HospitalRequestFrame extends JFrame {
    private JTable inventoryTable, deliveriesTable;
    private DefaultTableModel inventoryModel, deliveriesModel;

    private final Color primaryBlue = new Color(41, 128, 185);
    private final Color dangerRed = new Color(231, 76, 60);

    public HospitalRequestFrame() {
        setTitle("Hospital Portal - Blood Requests & Deliveries");
        setSize(850, 750); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setBackground(Color.WHITE);

        // ==========================================
        // TAB 1: LIVE CAPACITY (Inventory)
        // ==========================================
        JPanel pnlInventory = new JPanel(new BorderLayout(10, 10));
        pnlInventory.setBackground(Color.WHITE);
        pnlInventory.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        inventoryModel = new DefaultTableModel(new String[]{"Blood Group", "Available Bags"}, 0);
        inventoryTable = new JTable(inventoryModel);
        inventoryTable.setRowHeight(25);
        pnlInventory.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        JButton btnRefreshInv = new JButton("Refresh Live Capacity");
        styleButton(btnRefreshInv, primaryBlue);
        btnRefreshInv.addActionListener(e -> loadInventoryData());
        
        JPanel pnlInvBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlInvBtn.setBackground(Color.WHITE);
        pnlInvBtn.add(btnRefreshInv);
        pnlInventory.add(pnlInvBtn, BorderLayout.SOUTH);

        // ==========================================
        // TAB 2: REGISTER PATIENT
        // ==========================================
        JPanel pnlRegPatientWrapper = new JPanel(new BorderLayout());
        pnlRegPatientWrapper.setBackground(Color.WHITE);

        JLabel lblRegTitle = new JLabel("Register New Patient", SwingConstants.CENTER);
        lblRegTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblRegTitle.setForeground(primaryBlue);
        lblRegTitle.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));
        pnlRegPatientWrapper.add(lblRegTitle, BorderLayout.NORTH);

        JPanel pnlRegPatient = new JPanel(new GridLayout(8, 1, 5, 5));
        pnlRegPatient.setBackground(Color.WHITE);
        pnlRegPatient.setBorder(BorderFactory.createEmptyBorder(0, 150, 20, 150));

        pnlRegPatient.add(new JLabel("Hospital Name:"));
        JTextField txtRegHospName = new JTextField(); pnlRegPatient.add(txtRegHospName);

        pnlRegPatient.add(new JLabel("Patient Name:"));
        JTextField txtPatientName = new JTextField(); pnlRegPatient.add(txtPatientName);

        pnlRegPatient.add(new JLabel("Blood Group:"));
        JComboBox<String> cmbRegBlood = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        pnlRegPatient.add(cmbRegBlood);

        pnlRegPatient.add(new JLabel("Ward Number:"));
        JTextField txtWard = new JTextField(); pnlRegPatient.add(txtWard);

        pnlRegPatientWrapper.add(pnlRegPatient, BorderLayout.CENTER);

        JButton btnRegister = new JButton("Register Patient");
        styleButton(btnRegister, primaryBlue);
        btnRegister.setPreferredSize(new Dimension(0, 45));
        
        JPanel pnlRegBtnWrapper = new JPanel(new BorderLayout());
        pnlRegBtnWrapper.setBackground(Color.WHITE);
        pnlRegBtnWrapper.setBorder(BorderFactory.createEmptyBorder(10, 150, 40, 150));
        pnlRegBtnWrapper.add(btnRegister, BorderLayout.CENTER);
        pnlRegPatientWrapper.add(pnlRegBtnWrapper, BorderLayout.SOUTH);

        btnRegister.addActionListener(e -> {
            if(new HospitalController().registerPatient(txtRegHospName.getText(), txtPatientName.getText(), cmbRegBlood.getSelectedItem().toString(), txtWard.getText())) {
                JOptionPane.showMessageDialog(this, "Patient Registered Successfully!");
                txtRegHospName.setText(""); txtPatientName.setText(""); txtWard.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "Error registering patient.");
            }
        });

        // ==========================================
        // TAB 3: SEND REQUEST (UPGRADED WITH SMART ENGINE)
        // ==========================================
        JPanel pnlRequestWrapper = new JPanel(new BorderLayout());
        pnlRequestWrapper.setBackground(Color.WHITE);

        JLabel lblReqTitle = new JLabel("Submit Blood Allocation Request", SwingConstants.CENTER);
        lblReqTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblReqTitle.setForeground(primaryBlue);
        lblReqTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        pnlRequestWrapper.add(lblReqTitle, BorderLayout.NORTH);

        JPanel pnlRequest = new JPanel(new GridLayout(12, 1, 5, 2));
        pnlRequest.setBackground(Color.WHITE);
        pnlRequest.setBorder(BorderFactory.createEmptyBorder(0, 150, 10, 150));

        pnlRequest.add(new JLabel("Hospital Name:"));
        JTextField txtReqHospName = new JTextField(); pnlRequest.add(txtReqHospName);

        JLabel lblCity = new JLabel("Hospital City / Region (For Emergency Alerts):");
        lblCity.setForeground(dangerRed); 
        pnlRequest.add(lblCity);
        JTextField txtReqCity = new JTextField(); pnlRequest.add(txtReqCity);

        pnlRequest.add(new JLabel("Patient ID (Required):"));
        JTextField txtReqPatId = new JTextField(); pnlRequest.add(txtReqPatId);

        pnlRequest.add(new JLabel("Required Blood Group:"));
        JComboBox<String> cmbReqBlood = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        pnlRequest.add(cmbReqBlood);

        pnlRequest.add(new JLabel("Quantity (Bags):"));
        JSpinner spnReqQty = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        pnlRequest.add(spnReqQty);

        pnlRequest.add(new JLabel("Urgency Level:"));
        JComboBox<String> cmbReqUrgency = new JComboBox<>(new String[]{"Standard", "Emergency"});
        pnlRequest.add(cmbReqUrgency);

        pnlRequestWrapper.add(pnlRequest, BorderLayout.CENTER);

        JButton btnSubmitReq = new JButton("Submit Request");
        styleButton(btnSubmitReq, primaryBlue);
        btnSubmitReq.setPreferredSize(new Dimension(0, 45));
        
        JPanel pnlReqBtnWrapper = new JPanel(new BorderLayout());
        pnlReqBtnWrapper.setBackground(Color.WHITE);
        pnlReqBtnWrapper.setBorder(BorderFactory.createEmptyBorder(10, 150, 20, 150));
        pnlReqBtnWrapper.add(btnSubmitReq, BorderLayout.CENTER);
        pnlRequestWrapper.add(pnlReqBtnWrapper, BorderLayout.SOUTH);

        btnSubmitReq.addActionListener(e -> {
            try {
                int patientId = Integer.parseInt(txtReqPatId.getText());
                String urgency = cmbReqUrgency.getSelectedItem().toString();
                String requestedBlood = cmbReqBlood.getSelectedItem().toString();
                int qty = (Integer) spnReqQty.getValue();

                HospitalController controller = new HospitalController();
                
                // 1. Submit the Request to the DB
                boolean success = controller.submitRequest(
                    txtReqHospName.getText(), txtReqCity.getText(), patientId, requestedBlood, qty, urgency
                );

                if (success) {
                    // 2. RUN THE SMART MEDICAL INVENTORY SCAN
                    String smartReport = controller.analyzeInventoryCompatibility(requestedBlood, qty);
                    
                    if(urgency.equals("Emergency")) {
                        JOptionPane.showMessageDialog(this, "EMERGENCY LOGGED: Local donors in " + txtReqCity.getText() + " alerted!\n\n--- INVENTORY SCAN RESULT ---\n" + smartReport, "Emergency Dispatch & Inventory", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Request Logged Successfully.\n\n--- INVENTORY SCAN RESULT ---\n" + smartReport, "Smart Compatibility Report", JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Error submitting request. Verify Patient ID exists.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Patient ID must be a number.");
            }
        });

        // ==========================================
        // TAB 4: TRACK DELIVERIES
        // ==========================================
        JPanel pnlDeliveries = new JPanel(new BorderLayout(10, 10));
        pnlDeliveries.setBackground(Color.WHITE);
        pnlDeliveries.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        deliveriesModel = new DefaultTableModel(new String[]{"Delivery ID", "Req ID", "Driver", "Status", "Dispatch Time"}, 0);
        deliveriesTable = new JTable(deliveriesModel);
        deliveriesTable.setRowHeight(25);
        pnlDeliveries.add(new JScrollPane(deliveriesTable), BorderLayout.CENTER);

        JButton btnRefreshDel = new JButton("Refresh Tracker");
        styleButton(btnRefreshDel, primaryBlue);
        btnRefreshDel.addActionListener(e -> loadDeliveriesData());
        
        JPanel pnlDelBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlDelBtn.setBackground(Color.WHITE);
        pnlDelBtn.add(btnRefreshDel);
        pnlDeliveries.add(pnlDelBtn, BorderLayout.SOUTH);

        // --- ADD TABS ---
        tabbedPane.add("Live Capacity", pnlInventory);
        tabbedPane.add("Register Patient", pnlRegPatientWrapper);
        tabbedPane.add("Send Request", pnlRequestWrapper);
        tabbedPane.add("Track Deliveries", pnlDeliveries);
        add(tabbedPane, BorderLayout.CENTER);

        // --- BACK BUTTON ---
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(Color.WHITE);
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        JButton btnBack = new JButton("← Back to Home");
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnBack.setForeground(new Color(120, 120, 120));
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));

        btnBack.addActionListener(e -> { new StartScreenFrame().setVisible(true); this.dispose(); });
        bottomPanel.add(btnBack);
        add(bottomPanel, BorderLayout.SOUTH);

        loadInventoryData();
        loadDeliveriesData();
    }

    private void styleButton(JButton btn, Color bgColor) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void loadInventoryData() {
        inventoryModel.setRowCount(0);
        String sql = "SELECT blood_group, COUNT(*) as available FROM Inventory WHERE status = 'Available' GROUP BY blood_group";
        try (Connection conn = databaseConnectors.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                inventoryModel.addRow(new Object[]{rs.getString("blood_group"), rs.getInt("available")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadDeliveriesData() {
        deliveriesModel.setRowCount(0);
        String sql = "SELECT delivery_id, request_id, driver_name, status, dispatch_timestamp FROM Deliveries ORDER BY dispatch_timestamp DESC";
        try (Connection conn = databaseConnectors.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                deliveriesModel.addRow(new Object[]{rs.getInt("delivery_id"), rs.getInt("request_id"), rs.getString("driver_name"), rs.getString("status"), rs.getTimestamp("dispatch_timestamp")});
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}