package UI;

import hospital.HospitalController;
import database.databaseConnectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;

public class HospitalRequestFrame extends JFrame {
    private JTable inventoryTable, deliveriesTable, patientTable;
    private DefaultTableModel inventoryModel, deliveriesModel, patientModel;
    private JTabbedPane tabbedPane;

    // Form fields declared here so they can be auto-filled from other tabs
    private JTextField txtReqHospName, txtReqCity, txtReqPatId;
    private JComboBox<String> cmbReqBlood, cmbReqUrgency;
    private JSpinner spnReqQty;

    private final Color primaryBlue = new Color(41, 128, 185);
    private final Color dangerRed = new Color(231, 76, 60);
    private final Color successGreen = new Color(46, 204, 113);

    public HospitalRequestFrame() {
        setTitle("Hospital Portal - Blood Requests & Deliveries");
        setSize(900, 750); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(Color.WHITE);

        tabbedPane = new JTabbedPane();
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
                
                // --- UPGRADE: Automatically jump to Patient Records so they see the new ID ---
                loadPatientData(); 
                tabbedPane.setSelectedIndex(2); 
            } else {
                JOptionPane.showMessageDialog(this, "Error registering patient.");
            }
        });

        // ==========================================
        // TAB 3: PATIENT RECORDS (NEW DIRECTORY TAB)
        // ==========================================
        JPanel pnlPatientRecords = new JPanel(new BorderLayout(10, 10));
        pnlPatientRecords.setBackground(Color.WHITE);
        pnlPatientRecords.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        patientModel = new DefaultTableModel(new String[]{"Patient ID", "Hospital Name", "Patient Name", "Blood Group", "Ward"}, 0);
        patientTable = new JTable(patientModel);
        patientTable.setRowHeight(25);
        pnlPatientRecords.add(new JScrollPane(patientTable), BorderLayout.CENTER);
        
        setupSearchPanel(pnlPatientRecords, patientModel, patientTable);

        JPanel pnlPatientBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlPatientBtns.setBackground(Color.WHITE);

        JButton btnRefreshPatients = new JButton("Refresh Directory");
        styleButton(btnRefreshPatients, primaryBlue);

        JButton btnAutoFill = new JButton("Send Request for Selected Patient");
        styleButton(btnAutoFill, successGreen); // Highlighted in green because it's a primary action!

        pnlPatientBtns.add(btnRefreshPatients);
        pnlPatientBtns.add(btnAutoFill);
        pnlPatientRecords.add(pnlPatientBtns, BorderLayout.SOUTH);

        btnRefreshPatients.addActionListener(e -> loadPatientData());
        
        // --- UPGRADE: Smart Auto-Fill Logic ---
        btnAutoFill.addActionListener(e -> {
            int selectedRow = patientTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a patient from the list first.", "Action Required", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int modelRow = patientTable.convertRowIndexToModel(selectedRow);
            String patId = patientModel.getValueAt(modelRow, 0).toString();
            String hospName = patientModel.getValueAt(modelRow, 1).toString();
            String bGroup = patientModel.getValueAt(modelRow, 3).toString();

            // Inject the data into the form fields on the next tab
            txtReqPatId.setText(patId);
            txtReqHospName.setText(hospName);
            cmbReqBlood.setSelectedItem(bGroup);

            // Jump the user directly to the "Send Request" tab
            tabbedPane.setSelectedIndex(3); 
        });

        // ==========================================
        // TAB 4: SEND REQUEST
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
        txtReqHospName = new JTextField(); pnlRequest.add(txtReqHospName);

        JLabel lblCity = new JLabel("Hospital City / Region (For Emergency Alerts):");
        lblCity.setForeground(dangerRed); 
        pnlRequest.add(lblCity);
        txtReqCity = new JTextField(); pnlRequest.add(txtReqCity);

        pnlRequest.add(new JLabel("Patient ID (Required):"));
        txtReqPatId = new JTextField(); pnlRequest.add(txtReqPatId);

        pnlRequest.add(new JLabel("Required Blood Group:"));
        cmbReqBlood = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        pnlRequest.add(cmbReqBlood);

        pnlRequest.add(new JLabel("Quantity (Bags):"));
        spnReqQty = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        pnlRequest.add(spnReqQty);

        pnlRequest.add(new JLabel("Urgency Level:"));
        cmbReqUrgency = new JComboBox<>(new String[]{"Standard", "Emergency"});
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
                
                boolean success = controller.submitRequest(
                    txtReqHospName.getText(), txtReqCity.getText(), patientId, requestedBlood, qty, urgency
                );

                if (success) {
                    String smartReport = controller.analyzeInventoryCompatibility(requestedBlood, qty);
                    
                    if(urgency.equals("Emergency")) {
                        JOptionPane.showMessageDialog(this, "EMERGENCY LOGGED: Local donors in " + txtReqCity.getText() + " alerted!\n\n--- INVENTORY SCAN RESULT ---\n" + smartReport, "Emergency Dispatch & Inventory", JOptionPane.WARNING_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this, "Request Logged Successfully.\n\n--- INVENTORY SCAN RESULT ---\n" + smartReport, "Smart Compatibility Report", JOptionPane.INFORMATION_MESSAGE);
                    }
                    txtReqPatId.setText(""); // Clear ID to prevent double submission
                } else {
                    JOptionPane.showMessageDialog(this, "Error submitting request. Verify Patient ID exists.");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Patient ID must be a number.");
            }
        });

        // ==========================================
        // TAB 5: TRACK DELIVERIES
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
        tabbedPane.add("Patient Records", pnlPatientRecords); // <--- NEW TAB ADDED HERE
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
        loadPatientData(); 
        loadDeliveriesData();
    }

    private void styleButton(JButton btn, Color bgColor) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private void setupSearchPanel(JPanel panel, DefaultTableModel model, JTable table) {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.WHITE);
        JLabel lblSearch = new JLabel("Live Search Filter: ");
        lblSearch.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchPanel.add(lblSearch);
        
        JTextField txtSearch = new JTextField(25);
        searchPanel.add(txtSearch);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = txtSearch.getText();
                if (text.trim().length() == 0) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
            }
        });
        panel.add(searchPanel, BorderLayout.NORTH);
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

    // --- NEW: FETCH PATIENT DATA FOR THE DIRECTORY ---
    private void loadPatientData() {
        patientModel.setRowCount(0);
        String sql = "SELECT patient_id, hospital_name, patient_name, blood_group, ward_number FROM Patients ORDER BY patient_id DESC";
        try (Connection conn = databaseConnectors.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while(rs.next()) {
                patientModel.addRow(new Object[]{rs.getInt("patient_id"), rs.getString("hospital_name"), rs.getString("patient_name"), rs.getString("blood_group"), rs.getString("ward_number")});
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