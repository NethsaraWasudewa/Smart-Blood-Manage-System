package UI;

import hospital.HospitalController;
import database.databaseConnectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;

public class HospitalRequestFrame extends JFrame {
    private DefaultTableModel capacityModel;
    private DefaultTableModel deliveryModel;
    private TableRowSorter<DefaultTableModel> capacitySorter;
    private TableRowSorter<DefaultTableModel> deliverySorter;

    public HospitalRequestFrame() {
        setTitle("Hospital Portal - Blood Requests & Deliveries");
        setSize(800, 650); // Made larger to accommodate professional padding
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // =====================================================================
        // TAB 1: Live Blood Capacity (Redesigned with Search Logic)
        // =====================================================================
        JPanel pnlCapacity = new JPanel(new BorderLayout(10, 10));
        pnlCapacity.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30)); // Added Padding

        // Search Bar Area
        JPanel pnlCapSearch = new JPanel(new BorderLayout(10, 10));
        JLabel lblCapSearch = new JLabel("Live Search Filter (Blood Group):");
        lblCapSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JTextField txtCapSearch = new JTextField();
        txtCapSearch.setPreferredSize(new Dimension(200, 30));
        pnlCapSearch.add(lblCapSearch, BorderLayout.WEST);
        pnlCapSearch.add(txtCapSearch, BorderLayout.CENTER);
        pnlCapacity.add(pnlCapSearch, BorderLayout.NORTH);

        // Table Area
        capacityModel = new DefaultTableModel(new String[]{"Blood Group", "Safe & Available Bags"}, 0);
        JTable capacityTable = new JTable(capacityModel);
        capacityTable.setRowHeight(25); // Taller rows for readability
        
        // Add Live Search Logic
        capacitySorter = new TableRowSorter<>(capacityModel);
        capacityTable.setRowSorter(capacitySorter);
        txtCapSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String text = txtCapSearch.getText();
                if (text.trim().length() == 0) capacitySorter.setRowFilter(null);
                else capacitySorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });
        
        pnlCapacity.add(new JScrollPane(capacityTable), BorderLayout.CENTER);

        JButton btnRefreshStock = new JButton("Check Current Stock");
        stylePrimaryButton(btnRefreshStock);
        btnRefreshStock.addActionListener(e -> loadTables());
        pnlCapacity.add(btnRefreshStock, BorderLayout.SOUTH);

        // =====================================================================
        // TAB 2: Register Patient (Redesigned with GridBagLayout)
        // =====================================================================
        JPanel pnlPatient = new JPanel(new GridBagLayout());
        pnlPatient.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        GridBagConstraints gbcPat = new GridBagConstraints();
        gbcPat.fill = GridBagConstraints.HORIZONTAL;
        gbcPat.insets = new Insets(8, 0, 8, 0); // Spacing between rows
        gbcPat.weightx = 1.0;

        JLabel lblPatTitle = new JLabel("Register New Patient", SwingConstants.CENTER);
        lblPatTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblPatTitle.setForeground(new Color(41, 128, 185));

        JTextField txtHospPatient = new JTextField(); txtHospPatient.setPreferredSize(new Dimension(300, 30));
        JTextField txtPatName = new JTextField(); txtPatName.setPreferredSize(new Dimension(300, 30));
        JComboBox<String> cmbPatBlood = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        JTextField txtWard = new JTextField(); txtWard.setPreferredSize(new Dimension(300, 30));
        
        JButton btnRegPatient = new JButton("Register Patient");
        stylePrimaryButton(btnRegPatient);

        int row = 0;
        gbcPat.gridy = row++; pnlPatient.add(lblPatTitle, gbcPat);
        gbcPat.gridy = row++; pnlPatient.add(new JLabel("Hospital Name:"), gbcPat);
        gbcPat.gridy = row++; pnlPatient.add(txtHospPatient, gbcPat);
        gbcPat.gridy = row++; pnlPatient.add(new JLabel("Patient Full Name:"), gbcPat);
        gbcPat.gridy = row++; pnlPatient.add(txtPatName, gbcPat);
        gbcPat.gridy = row++; pnlPatient.add(new JLabel("Patient Blood Group:"), gbcPat);
        gbcPat.gridy = row++; pnlPatient.add(cmbPatBlood, gbcPat);
        gbcPat.gridy = row++; pnlPatient.add(new JLabel("Ward Number:"), gbcPat);
        gbcPat.gridy = row++; pnlPatient.add(txtWard, gbcPat);
        gbcPat.gridy = row++; pnlPatient.add(new JLabel(" "), gbcPat); // Spacer
        gbcPat.gridy = row++; pnlPatient.add(btnRegPatient, gbcPat);

        btnRegPatient.addActionListener(e -> {
            new HospitalController().registerPatient(txtHospPatient.getText(), txtPatName.getText(), cmbPatBlood.getSelectedItem().toString(), txtWard.getText());
            JOptionPane.showMessageDialog(this, "Patient Registered Successfully. Check Database for Patient ID.");
        });

        // =====================================================================
        // TAB 3: Send Request (Redesigned with GridBagLayout)
        // =====================================================================
        JPanel pnlRequest = new JPanel(new GridBagLayout());
        pnlRequest.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        GridBagConstraints gbcReq = new GridBagConstraints();
        gbcReq.fill = GridBagConstraints.HORIZONTAL;
        gbcReq.insets = new Insets(8, 0, 8, 0);
        gbcReq.weightx = 1.0;

        JLabel lblReqTitle = new JLabel("Submit Blood Allocation Request", SwingConstants.CENTER);
        lblReqTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblReqTitle.setForeground(new Color(41, 128, 185));

        JTextField txtHospital = new JTextField(); txtHospital.setPreferredSize(new Dimension(300, 30));
        JTextField txtPatientId = new JTextField(); txtPatientId.setPreferredSize(new Dimension(300, 30));
        JComboBox<String> cmbBloodGroup = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        JSpinner spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        spnQuantity.setPreferredSize(new Dimension(300, 30));
        JComboBox<String> cmbUrgency = new JComboBox<>(new String[]{"Standard", "Emergency"});
        
        JButton btnSubmit = new JButton("Submit Request");
        stylePrimaryButton(btnSubmit);

        row = 0;
        gbcReq.gridy = row++; pnlRequest.add(lblReqTitle, gbcReq);
        gbcReq.gridy = row++; pnlRequest.add(new JLabel("Hospital Name:"), gbcReq);
        gbcReq.gridy = row++; pnlRequest.add(txtHospital, gbcReq);
        gbcReq.gridy = row++; pnlRequest.add(new JLabel("Patient ID (Required):"), gbcReq);
        gbcReq.gridy = row++; pnlRequest.add(txtPatientId, gbcReq);
        gbcReq.gridy = row++; pnlRequest.add(new JLabel("Required Blood Group:"), gbcReq);
        gbcReq.gridy = row++; pnlRequest.add(cmbBloodGroup, gbcReq);
        gbcReq.gridy = row++; pnlRequest.add(new JLabel("Quantity (Bags):"), gbcReq);
        gbcReq.gridy = row++; pnlRequest.add(spnQuantity, gbcReq);
        gbcReq.gridy = row++; pnlRequest.add(new JLabel("Urgency Level:"), gbcReq);
        gbcReq.gridy = row++; pnlRequest.add(cmbUrgency, gbcReq);
        gbcReq.gridy = row++; pnlRequest.add(new JLabel(" "), gbcReq); // Spacer
        gbcReq.gridy = row++; pnlRequest.add(btnSubmit, gbcReq);

        btnSubmit.addActionListener(e -> {
            try {
                int pId = Integer.parseInt(txtPatientId.getText());
                new HospitalController().requestBlood(txtHospital.getText(), cmbBloodGroup.getSelectedItem().toString(), cmbUrgency.getSelectedItem().toString(), (Integer) spnQuantity.getValue(), pId);
                JOptionPane.showMessageDialog(this, "Request Submitted to Blood Bank.");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Error: Patient ID must be a number.");
            }
        });

        // =====================================================================
        // TAB 4: Track Deliveries (Redesigned with Search Logic)
        // =====================================================================
        JPanel pnlDeliveries = new JPanel(new BorderLayout(10, 10));
        pnlDeliveries.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JPanel pnlDelSearch = new JPanel(new BorderLayout(10, 10));
        JLabel lblDelSearch = new JLabel("Live Search Filter (Req ID / Status):");
        lblDelSearch.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JTextField txtDelSearch = new JTextField();
        txtDelSearch.setPreferredSize(new Dimension(200, 30));
        pnlDelSearch.add(lblDelSearch, BorderLayout.WEST);
        pnlDelSearch.add(txtDelSearch, BorderLayout.CENTER);
        pnlDeliveries.add(pnlDelSearch, BorderLayout.NORTH);

        deliveryModel = new DefaultTableModel(new String[]{"Delivery ID", "Req ID", "Driver", "Status"}, 0);
        JTable deliveryTable = new JTable(deliveryModel);
        deliveryTable.setRowHeight(25);
        
        // Add Live Search Logic
        deliverySorter = new TableRowSorter<>(deliveryModel);
        deliveryTable.setRowSorter(deliverySorter);
        txtDelSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String text = txtDelSearch.getText();
                if (text.trim().length() == 0) deliverySorter.setRowFilter(null);
                else deliverySorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });
        
        pnlDeliveries.add(new JScrollPane(deliveryTable), BorderLayout.CENTER);

        JButton btnRefreshData = new JButton("Refresh System Data");
        stylePrimaryButton(btnRefreshData);
        btnRefreshData.addActionListener(e -> loadTables());
        pnlDeliveries.add(btnRefreshData, BorderLayout.SOUTH);

        // =====================================================================
        // ASSEMBLE TABS AND GLOBAL BACK BUTTON
        // =====================================================================
        tabbedPane.add("Live Capacity", pnlCapacity);
        tabbedPane.add("Register Patient", new JScrollPane(pnlPatient)); // Scrollpane added in case screen is small
        tabbedPane.add("Send Request", new JScrollPane(pnlRequest));
        tabbedPane.add("Track Deliveries", pnlDeliveries);
        add(tabbedPane, BorderLayout.CENTER);

        // Global Back Button (Styled as subtle link)
        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        JButton btnBack = new JButton("← Back to Home");
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setForeground(Color.GRAY);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.addActionListener(e -> {
            new StartScreenFrame().setVisible(true);
            this.dispose();
        });
        bottomPanel.add(btnBack);
        add(bottomPanel, BorderLayout.SOUTH);

        loadTables();
    }

    // --- HELPER METHOD TO STYLE BUTTONS UNIFORMLY ---
    private void stylePrimaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(41, 128, 185));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(200, 40)); // Make them chunky and modern
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