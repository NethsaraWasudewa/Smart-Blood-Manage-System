package UI; 

import inventory.BloodBankController;
import database.databaseConnectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

public class InventoryManagementFrame extends JFrame {
    private DefaultTableModel stockModel, screeningModel, requestModel; 
    private JTable screeningTable, requestTable;
    private DefaultPieDataset pieDataset;

    public InventoryManagementFrame() {
        setTitle("Blood Bank Inventory & Analytics");
        setSize(850, 650);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel pnlAddBlood = new JPanel(new GridLayout(3, 2, 5, 5));
        pnlAddBlood.add(new JLabel("Blood Group Received:"));
        JComboBox<String> cmbBloodGroup = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        pnlAddBlood.add(cmbBloodGroup);
        JButton btnAdd = new JButton("Add to Inventory (Send to Testing)"); pnlAddBlood.add(btnAdd);

        btnAdd.addActionListener(e -> {
            new BloodBankController().addBloodBag(cmbBloodGroup.getSelectedItem().toString());
            JOptionPane.showMessageDialog(this, "Bag sent to Screening Lab.");
            loadTables(); loadChartData();
        });

        JPanel pnlScreening = new JPanel(new BorderLayout());
        screeningModel = new DefaultTableModel(new String[]{"Bag ID", "Blood Group", "Status"}, 0);
        screeningTable = new JTable(screeningModel);
        pnlScreening.add(new JScrollPane(screeningTable), BorderLayout.CENTER);
        JPanel pnlScreeningBtns = new JPanel();
        JButton btnPass = new JButton("Mark Safe (Passed)");
        JButton btnFail = new JButton("Mark Unsafe (Discard)");
        pnlScreeningBtns.add(btnPass); pnlScreeningBtns.add(btnFail);
        pnlScreening.add(pnlScreeningBtns, BorderLayout.SOUTH);

        btnPass.addActionListener(e -> processScreening(true));
        btnFail.addActionListener(e -> processScreening(false));
        
        JPanel pnlRequests = new JPanel(new BorderLayout());
        requestModel = new DefaultTableModel(new String[]{"Req ID", "Hospital", "Blood Group", "Urgency", "Quantity"}, 0);
        requestTable = new JTable(requestModel);
        pnlRequests.add(new JScrollPane(requestTable), BorderLayout.CENTER);
        JButton btnFulfill = new JButton("Allocate Blood & Dispatch Courier");
        btnFulfill.addActionListener(e -> fulfillRequest());
        pnlRequests.add(btnFulfill, BorderLayout.SOUTH);

        JPanel pnlViewInventory = new JPanel(new BorderLayout());
        stockModel = new DefaultTableModel(new String[]{"Bag ID", "Blood Group", "Expiry Date", "Status", "Screening"}, 0);
        pnlViewInventory.add(new JScrollPane(new JTable(stockModel)), BorderLayout.CENTER);
        JButton btnRefresh = new JButton("Refresh Data");
        btnRefresh.addActionListener(e -> loadTables());
        pnlViewInventory.add(btnRefresh, BorderLayout.SOUTH);

        JPanel pnlAnalytics = new JPanel(new BorderLayout());
        pieDataset = new DefaultPieDataset();
        JFreeChart pieChart = ChartFactory.createPieChart("Live Capacity Breakdown (Safe Blood)", pieDataset, true, true, false);
        pnlAnalytics.add(new ChartPanel(pieChart), BorderLayout.CENTER);
        JButton btnRefreshChart = new JButton("Refresh Analytics");
        btnRefreshChart.addActionListener(e -> loadChartData());
        pnlAnalytics.add(btnRefreshChart, BorderLayout.SOUTH);

        tabbedPane.add("Add Stock", pnlAddBlood);
        tabbedPane.add("Lab Screening", pnlScreening);
        tabbedPane.add("Fulfill Requests", pnlRequests);
        tabbedPane.add("View Stock", pnlViewInventory);
        tabbedPane.add("Analytics", pnlAnalytics); 
        add(tabbedPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JButton btnBack = new JButton("Back to Home");
        btnBack.addActionListener(e -> { new StartScreenFrame().setVisible(true); this.dispose(); });
        bottomPanel.add(btnBack);
        add(bottomPanel, BorderLayout.SOUTH);

        loadTables(); loadChartData();
    }

    private void processScreening(boolean isSafe) {
        int selectedRow = screeningTable.getSelectedRow();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Select a bag."); return; }
        new BloodBankController().screenBloodBag(screeningModel.getValueAt(selectedRow, 0).toString(), isSafe);
        JOptionPane.showMessageDialog(this, isSafe ? "Bag approved." : "Bag discarded.");
        loadTables(); loadChartData(); 
    }

    private void fulfillRequest() {
        int selectedRow = requestTable.getSelectedRow();
        if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Select a request."); return; }
        if (new BloodBankController().allocateBlood((Integer) requestModel.getValueAt(selectedRow, 0), (String) requestModel.getValueAt(selectedRow, 2), (Integer) requestModel.getValueAt(selectedRow, 4))) {
            JOptionPane.showMessageDialog(this, "SUCCESS: Blood allocated safely.");
        } else {
            JOptionPane.showMessageDialog(this, "ERROR: Not enough safe blood or lock timeout.", "Error", JOptionPane.ERROR_MESSAGE);
        }
        loadTables(); loadChartData();
    }

    private void loadTables() {
        stockModel.setRowCount(0); screeningModel.setRowCount(0); requestModel.setRowCount(0);
        try (Connection conn = databaseConnectors.getConnection(); Statement stmt = conn.createStatement()) {
            ResultSet rs1 = stmt.executeQuery("SELECT bag_id, blood_group, status FROM Inventory WHERE status = 'Testing'");
            while (rs1.next()) screeningModel.addRow(new Object[]{rs1.getInt("bag_id"), rs1.getString("blood_group"), rs1.getString("status")});
            ResultSet rs2 = stmt.executeQuery("SELECT request_id, hospital_name, blood_group, urgency_level, quantity FROM Requests WHERE status = 'Pending'");
            while (rs2.next()) requestModel.addRow(new Object[]{rs2.getInt("request_id"), rs2.getString("hospital_name"), rs2.getString("blood_group"), rs2.getString("urgency_level"), rs2.getInt("quantity")});
            ResultSet rs3 = stmt.executeQuery("SELECT bag_id, blood_group, expiry_date, status, screening_status FROM Inventory ORDER BY expiry_date ASC");
            while (rs3.next()) stockModel.addRow(new Object[]{rs3.getInt("bag_id"), rs3.getString("blood_group"), rs3.getDate("expiry_date"), rs3.getString("status"), rs3.getString("screening_status")});
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadChartData() {
        pieDataset.clear(); 
        String sql = "SELECT blood_group, COUNT(*) as amount FROM Inventory WHERE status = 'Available' GROUP BY blood_group";
        try (Connection conn = databaseConnectors.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) pieDataset.setValue(rs.getString("blood_group") + " (" + rs.getInt("amount") + ")", rs.getInt("amount"));
        } catch (SQLException e) { e.printStackTrace(); }
    }
}