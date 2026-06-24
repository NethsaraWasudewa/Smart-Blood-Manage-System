package UI;

import Admin.AdminController;
import Admin.ReportGenerator; 
import database.databaseConnectors;
import com.toedter.calendar.JDateChooser; 
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

public class AdminDashboardFrame extends JFrame {
    private JTable donorTable, patientTable, requestTable, deliveryTable, alertLogTable;
    private DefaultTableModel donorModel, patientModel, requestModel, deliveryModel, alertLogModel;
    private DefaultCategoryDataset barDataset;
    
    // Modern Theme Colors
    private final Color primaryBlue = new Color(41, 128, 185);
    private final Color dangerRed = new Color(231, 76, 60);
    private final Color successGreen = new Color(46, 204, 113);
    private final Color warningOrange = new Color(230, 126, 34); 

    public AdminDashboardFrame() {
        setTitle("Admin Dashboard - Manage System & Analytics");
        setSize(1000, 750); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout()); 
        getContentPane().setBackground(Color.WHITE);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14)); 
        tabbedPane.setBackground(Color.WHITE);

        // ==========================================
        // TAB 1: CREATE EVENT 
        // ==========================================
        JPanel pnlCreateEventWrapper = new JPanel(new BorderLayout());
        pnlCreateEventWrapper.setBackground(Color.WHITE);
        
        JLabel lblFormTitle = new JLabel("Create New Donation Event", SwingConstants.CENTER);
        lblFormTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblFormTitle.setForeground(primaryBlue);
        lblFormTitle.setBorder(BorderFactory.createEmptyBorder(30, 0, 20, 0));
        pnlCreateEventWrapper.add(lblFormTitle, BorderLayout.NORTH);

        JPanel pnlCreateEvent = new JPanel(new GridLayout(8, 1, 5, 5));
        pnlCreateEvent.setBackground(Color.WHITE);
        pnlCreateEvent.setBorder(BorderFactory.createEmptyBorder(0, 100, 20, 100)); 

        JLabel lblEventName = new JLabel("Event Name:");
        lblEventName.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JTextField txtEvent = new JTextField(); 
        
        JLabel lblLocation = new JLabel("Location:");
        lblLocation.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JTextField txtLocation = new JTextField(); 
        
        JLabel lblDate = new JLabel("Event Date:");
        lblDate.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JDateChooser dateChooser = new JDateChooser(); 
        dateChooser.setDateFormatString("yyyy-MM-dd");
        
        JLabel lblCapacity = new JLabel("Target Capacity:");
        lblCapacity.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JSpinner spnCapacity = new JSpinner(new SpinnerNumberModel(50, 10, 1000, 10)); 
        
        pnlCreateEvent.add(lblEventName); pnlCreateEvent.add(txtEvent);
        pnlCreateEvent.add(lblLocation); pnlCreateEvent.add(txtLocation);
        pnlCreateEvent.add(lblDate); pnlCreateEvent.add(dateChooser);
        pnlCreateEvent.add(lblCapacity); pnlCreateEvent.add(spnCapacity);
        
        pnlCreateEventWrapper.add(pnlCreateEvent, BorderLayout.CENTER);

        JButton btnCreate = new JButton("Create Event"); 
        styleButton(btnCreate, primaryBlue);
        btnCreate.setPreferredSize(new Dimension(0, 45)); 
        
        JPanel pnlBtnWrapper = new JPanel(new BorderLayout());
        pnlBtnWrapper.setBackground(Color.WHITE);
        pnlBtnWrapper.setBorder(BorderFactory.createEmptyBorder(10, 100, 40, 100));
        pnlBtnWrapper.add(btnCreate, BorderLayout.CENTER);
        pnlCreateEventWrapper.add(pnlBtnWrapper, BorderLayout.SOUTH);
        
        btnCreate.addActionListener(e -> {
            try {
                if (dateChooser.getDate() == null) {
                    JOptionPane.showMessageDialog(this, "Please select a valid date."); return;
                }
                LocalDate eventDate = dateChooser.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                new AdminController().createDonationEvent(txtEvent.getText(), txtLocation.getText(), eventDate, (Integer) spnCapacity.getValue());
                JOptionPane.showMessageDialog(this, "Event Created Successfully!");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "An error occurred while creating the event."); }
        });

        // ==========================================
        // TAB 2: MANAGE DONORS
        // ==========================================
        JPanel pnlManageDonors = new JPanel(new BorderLayout(10, 10));
        pnlManageDonors.setBackground(Color.WHITE);
        pnlManageDonors.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        donorModel = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Blood Group", "Location"}, 0);
        donorTable = new JTable(donorModel);
        donorTable.setRowHeight(25);
        pnlManageDonors.add(new JScrollPane(donorTable), BorderLayout.CENTER);
        setupSearchPanel(pnlManageDonors, donorModel, donorTable);
        
        JPanel pnlTableButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlTableButtons.setBackground(Color.WHITE);
        JButton btnRefresh = new JButton("Refresh Data");
        styleButton(btnRefresh, primaryBlue);
        
        JButton btnDelete = new JButton("Delete Selected Donor");
        styleButton(btnDelete, dangerRed);
        
        pnlTableButtons.add(btnRefresh); pnlTableButtons.add(btnDelete);
        pnlManageDonors.add(pnlTableButtons, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadDonorData());
        btnDelete.addActionListener(e -> deleteDonor());

        // ==========================================
        // TAB 3: HOSPITAL RECORDS 
        // ==========================================
        JPanel pnlHospitalRecords = new JPanel(new BorderLayout(10, 10));
        pnlHospitalRecords.setBackground(Color.WHITE);
        pnlHospitalRecords.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setBackground(Color.WHITE);

        JPanel pnlPatients = new JPanel(new BorderLayout());
        pnlPatients.setBackground(Color.WHITE);
        pnlPatients.setBorder(BorderFactory.createTitledBorder(null, "Registered Patients", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new Font("Segoe UI", Font.BOLD, 14), primaryBlue));
        
        patientModel = new DefaultTableModel(new String[]{"Patient ID", "Hospital", "Name", "Blood Group", "Ward"}, 0);
        patientTable = new JTable(patientModel);
        patientTable.setRowHeight(25);
        pnlPatients.add(new JScrollPane(patientTable), BorderLayout.CENTER);
        setupSearchPanel(pnlPatients, patientModel, patientTable);
        
        JPanel pnlPatBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlPatBtns.setBackground(Color.WHITE);
        JButton btnDelPat = new JButton("Delete Selected Patient");
        styleButton(btnDelPat, dangerRed);
        pnlPatBtns.add(btnDelPat);
        pnlPatients.add(pnlPatBtns, BorderLayout.SOUTH);

        JPanel pnlRequests = new JPanel(new BorderLayout());
        pnlRequests.setBackground(Color.WHITE);
        pnlRequests.setBorder(BorderFactory.createTitledBorder(null, "Blood Requests", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new Font("Segoe UI", Font.BOLD, 14), primaryBlue));
        
        requestModel = new DefaultTableModel(new String[]{"Req ID", "Hospital", "Patient ID", "Blood Group", "Urgency", "Qty", "Status"}, 0);
        requestTable = new JTable(requestModel);
        requestTable.setRowHeight(25);
        pnlRequests.add(new JScrollPane(requestTable), BorderLayout.CENTER);
        setupSearchPanel(pnlRequests, requestModel, requestTable);
        
        JPanel pnlReqBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlReqBtns.setBackground(Color.WHITE);
        JButton btnDelReq = new JButton("Delete Selected Request");
        styleButton(btnDelReq, dangerRed);
        pnlReqBtns.add(btnDelReq);
        pnlRequests.add(pnlReqBtns, BorderLayout.SOUTH);

        splitPane.setTopComponent(pnlPatients);
        splitPane.setBottomComponent(pnlRequests);
        pnlHospitalRecords.add(splitPane, BorderLayout.CENTER);

        JButton btnRefreshHospitals = new JButton("Refresh Hospital Data");
        styleButton(btnRefreshHospitals, primaryBlue);
        
        JButton btnExportPDF = new JButton("Export Requests to PDF");
        styleButton(btnExportPDF, warningOrange); 
        
        JPanel pnlHospRefreshWrapper = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlHospRefreshWrapper.setBackground(Color.WHITE);
        pnlHospRefreshWrapper.add(btnRefreshHospitals);
        pnlHospRefreshWrapper.add(btnExportPDF); 
        pnlHospitalRecords.add(pnlHospRefreshWrapper, BorderLayout.SOUTH);

        btnRefreshHospitals.addActionListener(e -> loadHospitalData());
        btnDelPat.addActionListener(e -> deletePatient());
        btnDelReq.addActionListener(e -> deleteRequest());
        
        btnExportPDF.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Save PDF Report");
            fileChooser.setSelectedFile(new File("Hospital_Requests_Report.pdf"));
            
            int userSelection = fileChooser.showSaveDialog(this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                String filePath = fileToSave.getAbsolutePath();
                if (!filePath.toLowerCase().endsWith(".pdf")) filePath += ".pdf";
                
                ReportGenerator reportGen = new ReportGenerator();
                if(reportGen.generateHospitalReport(filePath)) {
                    JOptionPane.showMessageDialog(this, "PDF Generated Successfully!\nSaved at: " + filePath, "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to generate PDF.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // ==========================================
        // TAB 4: LOGISTICS & TRANSPORT
        // ==========================================
        JPanel pnlLogistics = new JPanel(new BorderLayout(10, 10));
        pnlLogistics.setBackground(Color.WHITE);
        pnlLogistics.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        deliveryModel = new DefaultTableModel(new String[]{"Delivery ID", "Req ID", "Hospital", "Driver", "Status", "Dispatch Time"}, 0);
        deliveryTable = new JTable(deliveryModel);
        deliveryTable.setRowHeight(25);
        pnlLogistics.add(new JScrollPane(deliveryTable), BorderLayout.CENTER);
        setupSearchPanel(pnlLogistics, deliveryModel, deliveryTable);
        
        JPanel pnlLogisticsBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlLogisticsBtns.setBackground(Color.WHITE);
        
        JButton btnRefreshLogistics = new JButton("Refresh Tracker");
        styleButton(btnRefreshLogistics, primaryBlue);
        
        JButton btnConfirmArrival = new JButton("Confirm Safe Arrival (Mark Delivered)");
        styleButton(btnConfirmArrival, successGreen);
        
        pnlLogisticsBtns.add(btnRefreshLogistics);
        pnlLogisticsBtns.add(btnConfirmArrival);
        pnlLogistics.add(pnlLogisticsBtns, BorderLayout.SOUTH);

        btnRefreshLogistics.addActionListener(e -> loadLogisticsData());
        btnConfirmArrival.addActionListener(e -> confirmDeliveryArrival());

        // ==========================================
        // TAB 5: EMERGENCY LOGS (UPGRADED)
        // ==========================================
        JPanel pnlAlertLogs = new JPanel(new BorderLayout(10, 10));
        pnlAlertLogs.setBackground(Color.WHITE);
        pnlAlertLogs.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        alertLogModel = new DefaultTableModel(new String[]{"Log ID", "Req ID", "Patient Name", "Hospital", "Donor Alerted", "Donor Email", "Time Sent"}, 0);
        alertLogTable = new JTable(alertLogModel);
        alertLogTable.setRowHeight(25);
        pnlAlertLogs.add(new JScrollPane(alertLogTable), BorderLayout.CENTER);
        setupSearchPanel(pnlAlertLogs, alertLogModel, alertLogTable);
        
        JPanel pnlAlertBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlAlertBtns.setBackground(Color.WHITE);
        
        JButton btnRefreshLogs = new JButton("Refresh Audit Logs");
        styleButton(btnRefreshLogs, primaryBlue);
        
        // NEW DELETE BUTTON
        JButton btnDeleteLogReq = new JButton("Delete Selected Request");
        styleButton(btnDeleteLogReq, dangerRed);
        
        pnlAlertBtns.add(btnRefreshLogs);
        pnlAlertBtns.add(btnDeleteLogReq); // Added to panel
        pnlAlertLogs.add(pnlAlertBtns, BorderLayout.SOUTH);

        btnRefreshLogs.addActionListener(e -> loadEmergencyLogs());
        btnDeleteLogReq.addActionListener(e -> deleteRequestFromLog()); // Connected to new action

        // ==========================================
        // TAB 6: ANALYTICS
        // ==========================================
        JPanel pnlAnalytics = new JPanel(new BorderLayout());
        pnlAnalytics.setBackground(Color.WHITE);
        pnlAnalytics.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        barDataset = new DefaultCategoryDataset();
        JFreeChart barChart = ChartFactory.createBarChart("Donor Demographics by Location", "City / Location", "Number of Donors", barDataset);
        pnlAnalytics.add(new ChartPanel(barChart), BorderLayout.CENTER);
        
        JButton btnRefreshChart = new JButton("Refresh Chart Data");
        styleButton(btnRefreshChart, primaryBlue);
        JPanel pnlChartBtnWrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlChartBtnWrapper.setBackground(Color.WHITE);
        pnlChartBtnWrapper.add(btnRefreshChart);
        pnlAnalytics.add(pnlChartBtnWrapper, BorderLayout.SOUTH);
        
        btnRefreshChart.addActionListener(e -> loadChartData());

        // --- ADD TABS ---
        tabbedPane.add("Create Event", pnlCreateEventWrapper);
        tabbedPane.add("Manage Donors", pnlManageDonors);
        tabbedPane.add("Hospital Records", pnlHospitalRecords); 
        tabbedPane.add("Logistics", pnlLogistics); 
        tabbedPane.add("Emergency Logs", pnlAlertLogs); 
        tabbedPane.add("Analytics", pnlAnalytics); 
        add(tabbedPane, BorderLayout.CENTER); 

        // ==========================================
        // STYLED BACK BUTTON
        // ==========================================
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

        // Initial Data Load
        loadDonorData();
        loadHospitalData();
        loadLogisticsData();
        loadEmergencyLogs();
        loadChartData();
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

    // --- DATA LOADERS ---
    private void loadDonorData() {
        donorModel.setRowCount(0); 
        String sql = "SELECT donor_id, name, email, blood_group, location FROM Donors";
        try (Connection conn = databaseConnectors.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) donorModel.addRow(new Object[]{ rs.getInt("donor_id"), rs.getString("name"), rs.getString("email"), rs.getString("blood_group"), rs.getString("location") });
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadHospitalData() {
        patientModel.setRowCount(0);
        requestModel.setRowCount(0);
        try (Connection conn = databaseConnectors.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs1 = stmt.executeQuery("SELECT patient_id, hospital_name, patient_name, blood_group, ward_number FROM Patients");
            while (rs1.next()) patientModel.addRow(new Object[]{rs1.getInt("patient_id"), rs1.getString("hospital_name"), rs1.getString("patient_name"), rs1.getString("blood_group"), rs1.getString("ward_number")});
            
            ResultSet rs2 = stmt.executeQuery("SELECT request_id, hospital_name, patient_id, blood_group, urgency_level, quantity, status FROM Requests ORDER BY request_id DESC");
            while (rs2.next()) requestModel.addRow(new Object[]{rs2.getInt("request_id"), rs2.getString("hospital_name"), rs2.getInt("patient_id"), rs2.getString("blood_group"), rs2.getString("urgency_level"), rs2.getInt("quantity"), rs2.getString("status")});
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadLogisticsData() {
        deliveryModel.setRowCount(0);
        String sql = "SELECT d.delivery_id, d.request_id, r.hospital_name, d.driver_name, d.status, d.dispatch_timestamp " +
                     "FROM Deliveries d JOIN Requests r ON d.request_id = r.request_id ORDER BY d.dispatch_timestamp DESC";
        try (Connection conn = databaseConnectors.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                deliveryModel.addRow(new Object[]{
                    rs.getInt("delivery_id"), rs.getInt("request_id"), rs.getString("hospital_name"), 
                    rs.getString("driver_name"), rs.getString("status"), rs.getTimestamp("dispatch_timestamp")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadEmergencyLogs() {
        alertLogModel.setRowCount(0);
        String sql = "SELECT l.log_id, l.request_id, p.patient_name, r.hospital_name, l.donor_name, l.donor_email, l.dispatch_timestamp " +
                     "FROM Emergency_Logs l " +
                     "JOIN Requests r ON l.request_id = r.request_id " +
                     "JOIN Patients p ON r.patient_id = p.patient_id " +
                     "ORDER BY l.dispatch_timestamp DESC";
        try (Connection conn = databaseConnectors.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                alertLogModel.addRow(new Object[]{
                    rs.getInt("log_id"), rs.getInt("request_id"), rs.getString("patient_name"),
                    rs.getString("hospital_name"), rs.getString("donor_name"), rs.getString("donor_email"), 
                    rs.getTimestamp("dispatch_timestamp")
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadChartData() {
        barDataset.clear(); 
        String sql = "SELECT location, COUNT(*) as donor_count FROM Donors GROUP BY location";
        try (Connection conn = databaseConnectors.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String loc = rs.getString("location");
                barDataset.addValue(rs.getInt("donor_count"), "Donors", (loc == null || loc.isEmpty()) ? "Unknown" : loc);
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    // --- ACTIONS ---
    private void confirmDeliveryArrival() {
        int selectedRow = deliveryTable.getSelectedRow();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Select a delivery to confirm."); return; }
        
        int modelRow = deliveryTable.convertRowIndexToModel(selectedRow);
        int deliveryId = (int) deliveryModel.getValueAt(modelRow, 0);
        int requestId = (int) deliveryModel.getValueAt(modelRow, 1);
        String status = deliveryModel.getValueAt(modelRow, 4).toString();
        
        if (status.equals("Delivered")) {
            JOptionPane.showMessageDialog(this, "This delivery has already been marked as safely arrived.");
            return;
        }

        if (JOptionPane.showConfirmDialog(this, "Confirm that Delivery ID " + deliveryId + " has safely arrived at the hospital?", "Confirm Arrival", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            if (new AdminController().confirmDelivery(deliveryId, requestId)) {
                JOptionPane.showMessageDialog(this, "Loop Closed! Delivery logged and Request marked as Fulfilled.");
                loadLogisticsData();
                loadHospitalData(); 
            } else {
                JOptionPane.showMessageDialog(this, "Error confirming delivery.");
            }
        }
    }

    private void deleteDonor() {
        int selectedRow = donorTable.getSelectedRow();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Select a donor first."); return; }
        
        int modelRow = donorTable.convertRowIndexToModel(selectedRow);
        String donorId = donorModel.getValueAt(modelRow, 0).toString();
        
        if (JOptionPane.showConfirmDialog(this, "Delete Donor ID " + donorId + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM Donors WHERE donor_id = ?";
            try (Connection conn = databaseConnectors.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, donorId);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Donor Deleted.");
                loadDonorData(); loadChartData(); 
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void deletePatient() {
        int selectedRow = patientTable.getSelectedRow();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Select a patient first."); return; }
        
        int modelRow = patientTable.convertRowIndexToModel(selectedRow);
        String patientId = patientModel.getValueAt(modelRow, 0).toString();
        
        if (JOptionPane.showConfirmDialog(this, "Delete Patient ID " + patientId + "?\nWARNING: This will also delete all blood requests linked to this patient!", "Critical Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM Patients WHERE patient_id = ?";
            try (Connection conn = databaseConnectors.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, patientId);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Patient and linked requests deleted.");
                loadHospitalData(); 
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void deleteRequest() {
        int selectedRow = requestTable.getSelectedRow();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Select a request first."); return; }
        
        int modelRow = requestTable.convertRowIndexToModel(selectedRow);
        String requestId = requestModel.getValueAt(modelRow, 0).toString();
        
        if (JOptionPane.showConfirmDialog(this, "Delete Request ID " + requestId + "?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM Requests WHERE request_id = ?";
            try (Connection conn = databaseConnectors.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, requestId);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Request deleted.");
                loadHospitalData(); 
            } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    // --- NEW ACTION: DELETE REQUEST DIRECTLY FROM THE EMERGENCY LOG SCREEN ---
    private void deleteRequestFromLog() {
        int selectedRow = alertLogTable.getSelectedRow();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Select a log entry first."); return; }
        
        // Grab the Request ID from column index 1
        int modelRow = alertLogTable.convertRowIndexToModel(selectedRow);
        String requestId = alertLogModel.getValueAt(modelRow, 1).toString(); 
        
        if (JOptionPane.showConfirmDialog(this, "Are you sure you want to completely DELETE Request ID " + requestId + "?\nBecause of database rules, this will permanently remove the Request and automatically clear this Emergency Log.", "Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM Requests WHERE request_id = ?";
            try (Connection conn = databaseConnectors.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, requestId);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Request and all associated logs deleted successfully.");
                loadEmergencyLogs(); 
                loadHospitalData(); // Refreshes the hospital tab so the tables stay synced!
            } catch (SQLException e) { 
                e.printStackTrace(); 
                JOptionPane.showMessageDialog(this, "Error deleting request.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}