package UI;

import Admin.AdminController;
import database.databaseConnectors;
import com.toedter.calendar.JDateChooser; // Upgraded to Visual Calendar
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.ZoneId;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

public class AdminDashboardFrame extends JFrame {
    private JTable donorTable, patientTable, requestTable;
    private DefaultTableModel donorModel, patientModel, requestModel;
    private DefaultCategoryDataset barDataset;

    public AdminDashboardFrame() {
        setTitle("Admin Dashboard - Manage System & Analytics");
        setSize(850, 700); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout()); 

        JTabbedPane tabbedPane = new JTabbedPane();

        // --- TAB 1: CREATE EVENT ---
        JPanel pnlCreateEvent = new JPanel(new GridLayout(6, 2, 5, 5));
        pnlCreateEvent.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        pnlCreateEvent.add(new JLabel("Event Name:")); 
        JTextField txtEvent = new JTextField(); pnlCreateEvent.add(txtEvent);
        
        pnlCreateEvent.add(new JLabel("Location:")); 
        JTextField txtLocation = new JTextField(); pnlCreateEvent.add(txtLocation);
        
        pnlCreateEvent.add(new JLabel("Event Date:")); 
        JDateChooser dateChooser = new JDateChooser(); // UPGRADED UX
        dateChooser.setDateFormatString("yyyy-MM-dd");
        pnlCreateEvent.add(dateChooser);
        
        pnlCreateEvent.add(new JLabel("Target Capacity:")); 
        JSpinner spnCapacity = new JSpinner(new SpinnerNumberModel(50, 10, 1000, 10)); pnlCreateEvent.add(spnCapacity);
        
        JButton btnCreate = new JButton("Create Event"); pnlCreateEvent.add(btnCreate);
        
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

        // --- TAB 2: MANAGE DONORS ---
        JPanel pnlManageDonors = new JPanel(new BorderLayout());
        donorModel = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Blood Group", "Location"}, 0);
        donorTable = new JTable(donorModel);
        pnlManageDonors.add(new JScrollPane(donorTable), BorderLayout.CENTER);
        setupSearchPanel(pnlManageDonors, donorModel, donorTable);
        
        JPanel pnlTableButtons = new JPanel();
        JButton btnRefresh = new JButton("Refresh Data");
        JButton btnDelete = new JButton("Delete Selected Donor");
        pnlTableButtons.add(btnRefresh); pnlTableButtons.add(btnDelete);
        pnlManageDonors.add(pnlTableButtons, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadDonorData());
        btnDelete.addActionListener(e -> deleteDonor());

        // --- NEW TAB 3: HOSPITAL RECORDS (Patients & Requests) ---
        JPanel pnlHospitalRecords = new JPanel(new BorderLayout());
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setResizeWeight(0.5); // 50/50 split

        // Top Half: Patients
        JPanel pnlPatients = new JPanel(new BorderLayout());
        pnlPatients.setBorder(BorderFactory.createTitledBorder("Registered Patients"));
        patientModel = new DefaultTableModel(new String[]{"Patient ID", "Hospital", "Name", "Blood Group", "Ward"}, 0);
        patientTable = new JTable(patientModel);
        pnlPatients.add(new JScrollPane(patientTable), BorderLayout.CENTER);
        setupSearchPanel(pnlPatients, patientModel, patientTable);
        
        JPanel pnlPatBtns = new JPanel();
        JButton btnDelPat = new JButton("Delete Selected Patient");
        pnlPatBtns.add(btnDelPat);
        pnlPatients.add(pnlPatBtns, BorderLayout.SOUTH);

        // Bottom Half: Requests
        JPanel pnlRequests = new JPanel(new BorderLayout());
        pnlRequests.setBorder(BorderFactory.createTitledBorder("Blood Requests"));
        requestModel = new DefaultTableModel(new String[]{"Req ID", "Hospital", "Patient ID", "Blood Group", "Urgency", "Quantity", "Status"}, 0);
        requestTable = new JTable(requestModel);
        pnlRequests.add(new JScrollPane(requestTable), BorderLayout.CENTER);
        setupSearchPanel(pnlRequests, requestModel, requestTable);
        
        JPanel pnlReqBtns = new JPanel();
        JButton btnDelReq = new JButton("Delete Selected Request");
        pnlReqBtns.add(btnDelReq);
        pnlRequests.add(pnlReqBtns, BorderLayout.SOUTH);

        splitPane.setTopComponent(pnlPatients);
        splitPane.setBottomComponent(pnlRequests);
        pnlHospitalRecords.add(splitPane, BorderLayout.CENTER);

        JButton btnRefreshHospitals = new JButton("Refresh Hospital Data");
        btnRefreshHospitals.addActionListener(e -> loadHospitalData());
        pnlHospitalRecords.add(btnRefreshHospitals, BorderLayout.SOUTH);

        // Delete Actions for Hospital Tab
        btnDelPat.addActionListener(e -> deletePatient());
        btnDelReq.addActionListener(e -> deleteRequest());

        // --- TAB 4: ANALYTICS ---
        JPanel pnlAnalytics = new JPanel(new BorderLayout());
        barDataset = new DefaultCategoryDataset();
        JFreeChart barChart = ChartFactory.createBarChart("Donor Demographics by Location", "City / Location", "Number of Donors", barDataset);
        pnlAnalytics.add(new ChartPanel(barChart), BorderLayout.CENTER);
        JButton btnRefreshChart = new JButton("Refresh Chart Data");
        btnRefreshChart.addActionListener(e -> loadChartData());
        pnlAnalytics.add(btnRefreshChart, BorderLayout.SOUTH);

        // Add Tabs
        tabbedPane.add("Create Event", pnlCreateEvent);
        tabbedPane.add("Manage Donors", pnlManageDonors);
        tabbedPane.add("Hospital Records", pnlHospitalRecords); // New Tab
        tabbedPane.add("Analytics", pnlAnalytics); 
        add(tabbedPane, BorderLayout.CENTER); 

        JPanel bottomPanel = new JPanel();
        JButton btnBack = new JButton("Back to Home");
        btnBack.addActionListener(e -> { new StartScreenFrame().setVisible(true); this.dispose(); });
        bottomPanel.add(btnBack);
        add(bottomPanel, BorderLayout.SOUTH); 

        // Initial Data Load
        loadDonorData();
        loadHospitalData();
        loadChartData();
    }

    // SEARCH ENGINE REUSABLE METHOD
    private void setupSearchPanel(JPanel panel, DefaultTableModel model, JTable table) {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Live Search Filter: "));
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

    // --- DATA LOADING METHODS ---
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
            
            ResultSet rs2 = stmt.executeQuery("SELECT request_id, hospital_name, patient_id, blood_group, urgency_level, quantity, status FROM Requests");
            while (rs2.next()) requestModel.addRow(new Object[]{rs2.getInt("request_id"), rs2.getString("hospital_name"), rs2.getInt("patient_id"), rs2.getString("blood_group"), rs2.getString("urgency_level"), rs2.getInt("quantity"), rs2.getString("status")});
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

    // --- DELETION METHODS ---
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
        
        // Critical Warning: Deleting a patient deletes all their requests due to database relations!
        if (JOptionPane.showConfirmDialog(this, "Delete Patient ID " + patientId + "?\nWARNING: This will also delete all blood requests linked to this patient!", "Critical Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM Patients WHERE patient_id = ?";
            try (Connection conn = databaseConnectors.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, patientId);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Patient and linked requests deleted.");
                loadHospitalData(); // Refreshes both tables automatically
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
}