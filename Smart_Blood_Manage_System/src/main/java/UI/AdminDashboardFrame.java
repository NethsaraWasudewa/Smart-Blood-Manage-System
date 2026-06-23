package UI;

import Admin.AdminController; 
import database.databaseConnectors;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;

// JFreeChart Imports
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

public class AdminDashboardFrame extends JFrame {
    private JTable donorTable, patientTable, requestTable;
    private DefaultTableModel donorModel, patientModel, requestModel;
    private TableRowSorter<DefaultTableModel> donorSorter, patientSorter, requestSorter;
    private DefaultCategoryDataset barDataset;

    public AdminDashboardFrame() {
        setTitle("Admin Dashboard - Manage System & Analytics");
        setSize(900, 750); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // =====================================================================
        // TAB 1: Create Event
        // =====================================================================
        JPanel pnlCreateEvent = new JPanel(new GridBagLayout());
        pnlCreateEvent.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        GridBagConstraints gbcEvent = new GridBagConstraints();
        gbcEvent.fill = GridBagConstraints.HORIZONTAL;
        gbcEvent.insets = new Insets(10, 0, 10, 0);
        gbcEvent.weightx = 1.0;

        JLabel lblEventTitle = new JLabel("Schedule New Donation Campaign", SwingConstants.CENTER);
        lblEventTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblEventTitle.setForeground(new Color(41, 128, 185));

        JTextField txtEvent = new JTextField(); txtEvent.setPreferredSize(new Dimension(300, 30));
        JTextField txtLocation = new JTextField(); txtLocation.setPreferredSize(new Dimension(300, 30));
        JDateChooser dateChooser = new JDateChooser(); dateChooser.setPreferredSize(new Dimension(300, 30));
        JSpinner spnCapacity = new JSpinner(new SpinnerNumberModel(50, 10, 1000, 10)); spnCapacity.setPreferredSize(new Dimension(300, 30));
        
        JButton btnCreate = new JButton("Publish Campaign Event");
        stylePrimaryButton(btnCreate);

        // Here is our first "row" variable!
        int row = 0;
        gbcEvent.gridy = row++; pnlCreateEvent.add(lblEventTitle, gbcEvent);
        gbcEvent.gridy = row++; pnlCreateEvent.add(new JLabel("Campaign Name:"), gbcEvent);
        gbcEvent.gridy = row++; pnlCreateEvent.add(txtEvent, gbcEvent);
        gbcEvent.gridy = row++; pnlCreateEvent.add(new JLabel("Target Location / City:"), gbcEvent);
        gbcEvent.gridy = row++; pnlCreateEvent.add(txtLocation, gbcEvent);
        gbcEvent.gridy = row++; pnlCreateEvent.add(new JLabel("Event Date:"), gbcEvent);
        gbcEvent.gridy = row++; pnlCreateEvent.add(dateChooser, gbcEvent);
        gbcEvent.gridy = row++; pnlCreateEvent.add(new JLabel("Target Donor Capacity:"), gbcEvent);
        gbcEvent.gridy = row++; pnlCreateEvent.add(spnCapacity, gbcEvent);
        gbcEvent.gridy = row++; pnlCreateEvent.add(new JLabel(" "), gbcEvent); 
        gbcEvent.gridy = row++; pnlCreateEvent.add(btnCreate, gbcEvent);

        btnCreate.addActionListener(e -> {
            try {
                if (dateChooser.getDate() == null) {
                    JOptionPane.showMessageDialog(this, "Please select a valid date from the calendar.");
                    return;
                }
                LocalDate eventDate = dateChooser.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                AdminController controller = new AdminController();
                controller.createDonationEvent(txtEvent.getText(), txtLocation.getText(), eventDate, (Integer) spnCapacity.getValue());
                JOptionPane.showMessageDialog(this, "Campaign Event Successfully Published!");
                txtEvent.setText(""); txtLocation.setText(""); dateChooser.setDate(null);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "System Error: Failed to process event data.");
            }
        });

        // =====================================================================
        // TAB 2: Manage Donors
        // =====================================================================
        JPanel pnlManageDonors = new JPanel(new BorderLayout(10, 10));
        pnlManageDonors.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        pnlManageDonors.add(createSearchBar("Live Donor Filter (Name, Group, Location):", "donors"), BorderLayout.NORTH);

        donorModel = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Blood Group", "Location"}, 0);
        donorTable = new JTable(donorModel);
        donorTable.setRowHeight(25);
        donorSorter = new TableRowSorter<>(donorModel);
        donorTable.setRowSorter(donorSorter);
        pnlManageDonors.add(new JScrollPane(donorTable), BorderLayout.CENTER);

        JPanel pnlDonorBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnRefreshDonors = new JButton("Refresh Ledger"); stylePrimaryButton(btnRefreshDonors);
        JButton btnDeleteDonor = new JButton("✖ Purge Selected Donor"); styleDangerButton(btnDeleteDonor);
        pnlDonorBtns.add(btnRefreshDonors); pnlDonorBtns.add(btnDeleteDonor);
        pnlManageDonors.add(pnlDonorBtns, BorderLayout.SOUTH);

        btnRefreshDonors.addActionListener(e -> loadData());
        btnDeleteDonor.addActionListener(e -> {
            int selectedRow = donorTable.getSelectedRow();
            if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Select a donor from the table first."); return; }
            String donorId = donorModel.getValueAt(donorTable.convertRowIndexToModel(selectedRow), 0).toString();
            deleteRecord("Donors", "donor_id", donorId);
        });

        // =====================================================================
        // TAB 3: Hospital Records
        // =====================================================================
        JPanel pnlHospital = new JPanel(new GridLayout(2, 1, 10, 15)); 
        pnlHospital.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Top Half: Patients
        JPanel pnlPatWrapper = new JPanel(new BorderLayout(5, 5));
        pnlPatWrapper.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Registered Patients Directory"));
        pnlPatWrapper.add(createSearchBar("Search Patient Name or Hospital:", "patients"), BorderLayout.NORTH);
        
        patientModel = new DefaultTableModel(new String[]{"Patient ID", "Hospital", "Name", "Blood Group", "Ward"}, 0);
        patientTable = new JTable(patientModel); patientTable.setRowHeight(25);
        patientSorter = new TableRowSorter<>(patientModel); patientTable.setRowSorter(patientSorter);
        pnlPatWrapper.add(new JScrollPane(patientTable), BorderLayout.CENTER);
        
        JButton btnDelPatient = new JButton("✖ Delete Patient"); styleDangerButton(btnDelPatient);
        JPanel pnlPatBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT)); pnlPatBtn.add(btnDelPatient);
        pnlPatWrapper.add(pnlPatBtn, BorderLayout.SOUTH);

        // Bottom Half: Requests
        JPanel pnlReqWrapper = new JPanel(new BorderLayout(5, 5));
        pnlReqWrapper.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.GRAY), "Hospital Blood Requests"));
        pnlReqWrapper.add(createSearchBar("Search Request ID or Status:", "requests"), BorderLayout.NORTH);
        
        requestModel = new DefaultTableModel(new String[]{"Req ID", "Hospital", "Patient ID", "Blood Group", "Urgency", "Quantity", "Status"}, 0);
        requestTable = new JTable(requestModel); requestTable.setRowHeight(25);
        requestSorter = new TableRowSorter<>(requestModel); requestTable.setRowSorter(requestSorter);
        pnlReqWrapper.add(new JScrollPane(requestTable), BorderLayout.CENTER);

        JButton btnDelRequest = new JButton("✖ Delete Request"); styleDangerButton(btnDelRequest);
        JPanel pnlReqBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT)); pnlReqBtn.add(btnDelRequest);
        pnlReqWrapper.add(pnlReqBtn, BorderLayout.SOUTH);

        pnlHospital.add(pnlPatWrapper);
        pnlHospital.add(pnlReqWrapper);

        // FIXED: Renamed 'row' to 'selectedPatRow' to avoid Java conflicts!
        btnDelPatient.addActionListener(e -> {
            int selectedPatRow = patientTable.getSelectedRow();
            if (selectedPatRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a patient from the table first.");
                return;
            }
            String patId = patientModel.getValueAt(patientTable.convertRowIndexToModel(selectedPatRow), 0).toString();
            deleteRecord("Patients", "patient_id", patId);
        });

        // FIXED: Renamed 'row' to 'selectedReqRow' to avoid Java conflicts!
        btnDelRequest.addActionListener(e -> {
            int selectedReqRow = requestTable.getSelectedRow();
            if (selectedReqRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a request from the table first.");
                return;
            }
            String reqId = requestModel.getValueAt(requestTable.convertRowIndexToModel(selectedReqRow), 0).toString();
            deleteRecord("Requests", "request_id", reqId);
        });

        // =====================================================================
        // TAB 4: Analytics
        // =====================================================================
        JPanel pnlAnalytics = new JPanel(new BorderLayout(10, 10));
        pnlAnalytics.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        barDataset = new DefaultCategoryDataset();
        JFreeChart barChart = ChartFactory.createBarChart("Donor Demographics by Geographic Sector", "City / Location", "Registered Volunteers", barDataset);
        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setBackground(Color.WHITE); 
        pnlAnalytics.add(chartPanel, BorderLayout.CENTER);

        JPanel pnlChartBtn = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnRefreshChart = new JButton("Refresh Analytics Metrics"); stylePrimaryButton(btnRefreshChart);
        btnRefreshChart.addActionListener(e -> loadChartData());
        pnlChartBtn.add(btnRefreshChart);
        pnlAnalytics.add(pnlChartBtn, BorderLayout.SOUTH);

        // =====================================================================
        // ASSEMBLE TABS AND GLOBAL BACK BUTTON
        // =====================================================================
        tabbedPane.add("Create Event", new JScrollPane(pnlCreateEvent));
        tabbedPane.add("Manage Donors", pnlManageDonors);
        tabbedPane.add("Hospital Records", pnlHospital);
        tabbedPane.add("Analytics", pnlAnalytics); 
        add(tabbedPane, BorderLayout.CENTER); 

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        JButton btnBack = new JButton("← Back to Admin Core");
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

        loadData();
        loadChartData();
    }

    // --- HELPER METHODS FOR SEARCH BARS ---
    private JPanel createSearchBar(String labelText, String targetTable) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        JTextField txtSearch = new JTextField();
        txtSearch.setPreferredSize(new Dimension(250, 30));
        
        txtSearch.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                String text = txtSearch.getText();
                TableRowSorter<DefaultTableModel> activeSorter = null;
                if (targetTable.equals("donors")) activeSorter = donorSorter;
                if (targetTable.equals("patients")) activeSorter = patientSorter;
                if (targetTable.equals("requests")) activeSorter = requestSorter;

                if (activeSorter != null) {
                    if (text.trim().length() == 0) activeSorter.setRowFilter(null);
                    else activeSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
                }
            }
        });
        panel.add(label, BorderLayout.WEST);
        panel.add(txtSearch, BorderLayout.CENTER);
        return panel;
    }

    // --- HELPER METHODS FOR BUTTON STYLES ---
    private void stylePrimaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(41, 128, 185)); 
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(250, 40)); 
    }

    private void styleDangerButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(231, 76, 60)); 
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(220, 40)); 
    }

    // --- DATA LOADING & SQL LOGIC ---
    private void loadData() {
        donorModel.setRowCount(0); patientModel.setRowCount(0); requestModel.setRowCount(0);
        
        try (Connection conn = databaseConnectors.getConnection();
             Statement stmt = conn.createStatement()) {
            
            ResultSet rs1 = stmt.executeQuery("SELECT donor_id, name, email, blood_group, location FROM Donors");
            while (rs1.next()) {
                donorModel.addRow(new Object[]{rs1.getInt("donor_id"), rs1.getString("name"), rs1.getString("email"), rs1.getString("blood_group"), rs1.getString("location")});
            }

            ResultSet rs2 = stmt.executeQuery("SELECT patient_id, hospital_name, patient_name, blood_group, ward_number FROM Patients");
            while (rs2.next()) {
                patientModel.addRow(new Object[]{rs2.getInt("patient_id"), rs2.getString("hospital_name"), rs2.getString("patient_name"), rs2.getString("blood_group"), rs2.getString("ward_number")});
            }

            ResultSet rs3 = stmt.executeQuery("SELECT request_id, hospital_name, patient_id, blood_group, urgency_level, quantity, status FROM Requests");
            while (rs3.next()) {
                requestModel.addRow(new Object[]{rs3.getInt("request_id"), rs3.getString("hospital_name"), rs3.getInt("patient_id"), rs3.getString("blood_group"), rs3.getString("urgency_level"), rs3.getInt("quantity"), rs3.getString("status")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadChartData() {
        barDataset.clear(); 
        String sql = "SELECT location, COUNT(*) as donor_count FROM Donors GROUP BY location";
        try (Connection conn = databaseConnectors.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String location = rs.getString("location");
                int count = rs.getInt("donor_count");
                if(location == null || location.isEmpty()) location = "Unspecified";
                barDataset.addValue(count, "Registered Volunteers", location);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteRecord(String tableName, String idColumn, String idValue) {
        int confirm = JOptionPane.showConfirmDialog(this, "Confirm irreversible deletion execution for ID " + idValue + " from " + tableName + "?", "Data Integrity Warning", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM " + tableName + " WHERE " + idColumn + " = ?";
            try (Connection conn = databaseConnectors.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, idValue);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Entity purged from system records.");
                loadData(); 
                if (tableName.equals("Donors")) loadChartData(); 
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}