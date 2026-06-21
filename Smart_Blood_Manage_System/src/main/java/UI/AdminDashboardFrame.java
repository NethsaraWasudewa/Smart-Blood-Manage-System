package UI;

import Admin.AdminController;
import database.databaseConnectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

public class AdminDashboardFrame extends JFrame {
    private JTable donorTable;
    private DefaultTableModel tableModel;
    private DefaultCategoryDataset barDataset;

    public AdminDashboardFrame() {
        setTitle("Admin Dashboard - Manage System & Analytics");
        setSize(750, 600); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout()); 

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel pnlCreateEvent = new JPanel(new GridLayout(6, 2, 5, 5));
        pnlCreateEvent.add(new JLabel("Event Name:")); JTextField txtEvent = new JTextField(); pnlCreateEvent.add(txtEvent);
        pnlCreateEvent.add(new JLabel("Location:")); JTextField txtLocation = new JTextField(); pnlCreateEvent.add(txtLocation);
        pnlCreateEvent.add(new JLabel("Date (YYYY-MM-DD):")); JTextField txtDate = new JTextField(); pnlCreateEvent.add(txtDate);
        pnlCreateEvent.add(new JLabel("Target Capacity:")); JSpinner spnCapacity = new JSpinner(new SpinnerNumberModel(50, 10, 1000, 10)); pnlCreateEvent.add(spnCapacity);
        JButton btnCreate = new JButton("Create Event"); pnlCreateEvent.add(btnCreate);
        
        btnCreate.addActionListener(e -> {
            try {
                new AdminController().createDonationEvent(txtEvent.getText(), txtLocation.getText(), LocalDate.parse(txtDate.getText()), (Integer) spnCapacity.getValue());
                JOptionPane.showMessageDialog(this, "Event Created!");
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error formatting date."); }
        });

        JPanel pnlManageDonors = new JPanel(new BorderLayout());
        tableModel = new DefaultTableModel(new String[]{"ID", "Name", "Email", "Blood Group", "Location"}, 0);
        donorTable = new JTable(tableModel);
        pnlManageDonors.add(new JScrollPane(donorTable), BorderLayout.CENTER);
        JPanel pnlTableButtons = new JPanel();
        JButton btnRefresh = new JButton("Refresh Data");
        JButton btnDelete = new JButton("Delete Selected Donor");
        pnlTableButtons.add(btnRefresh); pnlTableButtons.add(btnDelete);
        pnlManageDonors.add(pnlTableButtons, BorderLayout.SOUTH);

        btnRefresh.addActionListener(e -> loadDonorData());
        btnDelete.addActionListener(e -> deleteDonor());

        JPanel pnlAnalytics = new JPanel(new BorderLayout());
        barDataset = new DefaultCategoryDataset();
        JFreeChart barChart = ChartFactory.createBarChart("Donor Demographics by Location", "City / Location", "Number of Donors", barDataset);
        pnlAnalytics.add(new ChartPanel(barChart), BorderLayout.CENTER);
        JButton btnRefreshChart = new JButton("Refresh Chart Data");
        btnRefreshChart.addActionListener(e -> loadChartData());
        pnlAnalytics.add(btnRefreshChart, BorderLayout.SOUTH);

        tabbedPane.add("Create Event", pnlCreateEvent);
        tabbedPane.add("Manage Donors", pnlManageDonors);
        tabbedPane.add("Analytics", pnlAnalytics); 
        add(tabbedPane, BorderLayout.CENTER); 

        JPanel bottomPanel = new JPanel();
        JButton btnBack = new JButton("Back to Home");
        btnBack.addActionListener(e -> { new StartScreenFrame().setVisible(true); this.dispose(); });
        bottomPanel.add(btnBack);
        add(bottomPanel, BorderLayout.SOUTH); 

        loadDonorData();
        loadChartData();
    }

    private void loadDonorData() {
        tableModel.setRowCount(0); 
        String sql = "SELECT donor_id, name, email, blood_group, location FROM Donors";
        try (Connection conn = databaseConnectors.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) tableModel.addRow(new Object[]{ rs.getInt("donor_id"), rs.getString("name"), rs.getString("email"), rs.getString("blood_group"), rs.getString("location") });
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

    private void deleteDonor() {
        int selectedRow = donorTable.getSelectedRow();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Select a donor first."); return; }
        String donorId = tableModel.getValueAt(selectedRow, 0).toString();
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
}