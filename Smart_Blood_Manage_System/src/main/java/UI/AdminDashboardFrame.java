package UI;

import Admin.AdminController;
import database.databaseConnectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

// JFreeChart Imports
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

public class AdminDashboardFrame extends JFrame {
    private JTable donorTable;
    private DefaultTableModel tableModel;
    private DefaultCategoryDataset barDataset; // Holds data for the Bar Chart

    public AdminDashboardFrame() {
        setTitle("Admin Dashboard - Manage System & Analytics");
        setSize(700, 550); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout()); 

        JTabbedPane tabbedPane = new JTabbedPane();

        // --- TAB 1: Create Event ---
        JPanel pnlCreateEvent = new JPanel(new GridLayout(6, 2, 5, 5));
        pnlCreateEvent.add(new JLabel("Event Name:"));
        JTextField txtEvent = new JTextField();
        pnlCreateEvent.add(txtEvent);

        pnlCreateEvent.add(new JLabel("Location:"));
        JTextField txtLocation = new JTextField();
        pnlCreateEvent.add(txtLocation);

        pnlCreateEvent.add(new JLabel("Date (YYYY-MM-DD):"));
        JTextField txtDate = new JTextField();
        pnlCreateEvent.add(txtDate);

        pnlCreateEvent.add(new JLabel("Target Capacity:"));
        JSpinner spnCapacity = new JSpinner(new SpinnerNumberModel(50, 10, 1000, 10));
        pnlCreateEvent.add(spnCapacity);

        JButton btnCreate = new JButton("Create Event");
        pnlCreateEvent.add(btnCreate);
        
        btnCreate.addActionListener(e -> {
            try {
                AdminController controller = new AdminController();
                controller.createDonationEvent(txtEvent.getText(), txtLocation.getText(), LocalDate.parse(txtDate.getText()), (Integer) spnCapacity.getValue());
                JOptionPane.showMessageDialog(this, "Event Created!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error formatting date.");
            }
        });

        // --- TAB 2: Manage Donors ---
        JPanel pnlManageDonors = new JPanel(new BorderLayout());
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Blood Group", "Location"}, 0);
        donorTable = new JTable(tableModel);
        pnlManageDonors.add(new JScrollPane(donorTable), BorderLayout.CENTER);

        JPanel pnlTableButtons = new JPanel();
        JButton btnRefresh = new JButton("Refresh Data");
        JButton btnDelete = new JButton("Delete Selected Donor");
        pnlTableButtons.add(btnRefresh);
        pnlTableButtons.add(btnDelete);
        pnlManageDonors.add(pnlTableButtons, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadDonorData());

        btnDelete.addActionListener(e -> {
            int selectedRow = donorTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a donor from the table first.");
                return;
            }
            String donorId = tableModel.getValueAt(selectedRow, 0).toString();
            deleteDonor(donorId);
        });

        // --- TAB 3: Donor Demographics (BAR CHART) ---
        JPanel pnlAnalytics = new JPanel(new BorderLayout());
        barDataset = new DefaultCategoryDataset();
        JFreeChart barChart = ChartFactory.createBarChart(
                "Donor Demographics by Location", // Chart Title
                "City / Location",               // X-Axis Label
                "Number of Donors",              // Y-Axis Label
                barDataset                       // Data
        );
        ChartPanel chartPanel = new ChartPanel(barChart);
        pnlAnalytics.add(chartPanel, BorderLayout.CENTER);

        JButton btnRefreshChart = new JButton("Refresh Chart Data");
        btnRefreshChart.addActionListener(e -> loadChartData());
        pnlAnalytics.add(btnRefreshChart, BorderLayout.SOUTH);

        // Add Tabs
        tabbedPane.add("Create Event", pnlCreateEvent);
        tabbedPane.add("Manage Donors", pnlManageDonors);
        tabbedPane.add("Analytics", pnlAnalytics); // New Analytics Tab
        add(tabbedPane, BorderLayout.CENTER); 

        // --- GLOBAL BACK BUTTON ---
        JPanel bottomPanel = new JPanel();
        JButton btnBack = new JButton("Back to Home");
        btnBack.addActionListener(e -> {
            new StartScreenFrame().setVisible(true);
            this.dispose();
        });
        bottomPanel.add(btnBack);
        add(bottomPanel, BorderLayout.SOUTH); 

        // Load initial data
        loadDonorData();
        loadChartData();
    }

    private void loadDonorData() {
        tableModel.setRowCount(0); 
        String sql = "SELECT donor_id, name, email, blood_group, location FROM Donors";
        
        try (Connection conn = databaseConnectors.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("donor_id"), rs.getString("name"), rs.getString("email"), rs.getString("blood_group"), rs.getString("location")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadChartData() {
        barDataset.clear(); // Clear old data
        // SQL query aggregates the count of donors grouped by their location
        String sql = "SELECT location, COUNT(*) as donor_count FROM Donors GROUP BY location";
        
        try (Connection conn = databaseConnectors.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String location = rs.getString("location");
                int count = rs.getInt("donor_count");
                if(location == null || location.isEmpty()) location = "Unknown";
                
                // Add data to the chart: (Value, Series Name, X-Axis Category)
                barDataset.addValue(count, "Donors", location);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void deleteDonor(String donorId) {
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete Donor ID " + donorId + "?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            String sql = "DELETE FROM Donors WHERE donor_id = ?";
            try (Connection conn = databaseConnectors.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, donorId);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Donor Deleted.");
                loadDonorData(); 
                loadChartData(); // Refresh chart when a donor is deleted
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}