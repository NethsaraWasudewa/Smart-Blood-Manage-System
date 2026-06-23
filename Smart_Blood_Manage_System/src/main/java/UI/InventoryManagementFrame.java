package UI; 

import inventory.BloodBankController;
import database.databaseConnectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;

// JFreeChart Imports
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

public class InventoryManagementFrame extends JFrame {
    private DefaultTableModel stockModel, screeningModel, requestModel; 
    private JTable screeningTable, requestTable, inventoryTable;
    private TableRowSorter<DefaultTableModel> stockSorter, screeningSorter, requestSorter;
    private DefaultPieDataset pieDataset;

    public InventoryManagementFrame() {
        setTitle("Blood Bank Inventory & Analytics Panel");
        setSize(850, 650); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 13));

        // =====================================================================
        // TAB 1: Add Blood
        // =====================================================================
        JPanel pnlAddBlood = new JPanel(new GridBagLayout());
        pnlAddBlood.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));
        GridBagConstraints gbcAdd = new GridBagConstraints();
        gbcAdd.fill = GridBagConstraints.HORIZONTAL;
        gbcAdd.insets = new Insets(10, 0, 10, 0);
        gbcAdd.weightx = 1.0;

        JLabel lblAddTitle = new JLabel("Log New Biological Asset", SwingConstants.CENTER);
        lblAddTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblAddTitle.setForeground(new Color(41, 128, 185));

        JComboBox<String> cmbBloodGroup = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        cmbBloodGroup.setPreferredSize(new Dimension(300, 35));
        
        JButton btnAdd = new JButton("Add to Inventory (Send to Lab)");
        stylePrimaryButton(btnAdd);

        gbcAdd.gridy = 0; pnlAddBlood.add(lblAddTitle, gbcAdd);
        gbcAdd.gridy = 1; pnlAddBlood.add(new JLabel("Acquired Blood Group:"), gbcAdd);
        gbcAdd.gridy = 2; pnlAddBlood.add(cmbBloodGroup, gbcAdd);
        gbcAdd.gridy = 3; pnlAddBlood.add(new JLabel(" "), gbcAdd); 
        gbcAdd.gridy = 4; pnlAddBlood.add(btnAdd, gbcAdd);

        btnAdd.addActionListener(e -> {
            BloodBankController controller = new BloodBankController();
            controller.addBloodBag(cmbBloodGroup.getSelectedItem().toString());
            JOptionPane.showMessageDialog(this, "Asset logged successfully. Sent to Screening Lab.");
            loadTables(); 
            loadChartData();
        });

        // =====================================================================
        // TAB 2: Lab Screening
        // =====================================================================
        JPanel pnlScreening = new JPanel(new BorderLayout(10, 10));
        pnlScreening.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        pnlScreening.add(createSearchBar("Search Bag ID or Group:", "screening"), BorderLayout.NORTH);

        screeningModel = new DefaultTableModel(new String[]{"Bag ID", "Blood Group", "Status"}, 0);
        screeningTable = new JTable(screeningModel);
        screeningTable.setRowHeight(25);
        screeningSorter = new TableRowSorter<>(screeningModel);
        screeningTable.setRowSorter(screeningSorter);
        pnlScreening.add(new JScrollPane(screeningTable), BorderLayout.CENTER);

        JPanel pnlScreeningBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        JButton btnPass = new JButton("✔ Mark Safe (Pass)");
        styleSuccessButton(btnPass);
        JButton btnFail = new JButton("✖ Mark Unsafe (Discard)");
        styleDangerButton(btnFail);
        
        pnlScreeningBtns.add(btnPass);
        pnlScreeningBtns.add(btnFail);
        pnlScreening.add(pnlScreeningBtns, BorderLayout.SOUTH);

        btnPass.addActionListener(e -> processScreening(true));
        btnFail.addActionListener(e -> processScreening(false));
        
        // =====================================================================
        // TAB 3: Fulfill Requests
        // =====================================================================
        JPanel pnlRequests = new JPanel(new BorderLayout(10, 10));
        pnlRequests.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        pnlRequests.add(createSearchBar("Search Hospital or Blood Group:", "request"), BorderLayout.NORTH);

        requestModel = new DefaultTableModel(new String[]{"Req ID", "Requester Facility", "Blood Group", "Urgency", "Quantity"}, 0);
        requestTable = new JTable(requestModel);
        requestTable.setRowHeight(25);
        requestSorter = new TableRowSorter<>(requestModel);
        requestTable.setRowSorter(requestSorter);
        pnlRequests.add(new JScrollPane(requestTable), BorderLayout.CENTER);

        JPanel pnlReqBtn = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnFulfill = new JButton("Allocate Blood & Dispatch Courier");
        stylePrimaryButton(btnFulfill);
        pnlReqBtn.add(btnFulfill);
        pnlRequests.add(pnlReqBtn, BorderLayout.SOUTH);

        btnFulfill.addActionListener(e -> {
            int selectedRow = requestTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select a request from the table first.");
                return;
            }
            int modelRow = requestTable.convertRowIndexToModel(selectedRow);
            
            int reqId = (Integer) requestModel.getValueAt(modelRow, 0);
            String bg = (String) requestModel.getValueAt(modelRow, 2);
            int qty = (Integer) requestModel.getValueAt(modelRow, 4);

            BloodBankController controller = new BloodBankController();
            boolean success = controller.allocateBlood(reqId, bg, qty);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "SUCCESS: Blood allocated safely. Delivery dispatched.");
            } else {
                JOptionPane.showMessageDialog(this, "ERROR: Not enough safe blood in inventory or bags were locked.", "Concurrency Error", JOptionPane.ERROR_MESSAGE);
            }
            loadTables();
            loadChartData();
        });

        // =====================================================================
        // TAB 4: View Stock
        // =====================================================================
        JPanel pnlViewInventory = new JPanel(new BorderLayout(10, 10));
        pnlViewInventory.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        pnlViewInventory.add(createSearchBar("Search ID, Group, or Status:", "stock"), BorderLayout.NORTH);

        stockModel = new DefaultTableModel(new String[]{"Bag ID", "Blood Group", "Expiry Date", "Status", "Screening"}, 0);
        inventoryTable = new JTable(stockModel);
        inventoryTable.setRowHeight(25);
        stockSorter = new TableRowSorter<>(stockModel);
        inventoryTable.setRowSorter(stockSorter);
        pnlViewInventory.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        JPanel pnlStockBtn = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnRefresh = new JButton("Refresh System Data");
        stylePrimaryButton(btnRefresh);
        btnRefresh.addActionListener(e -> loadTables());
        pnlStockBtn.add(btnRefresh);
        pnlViewInventory.add(pnlStockBtn, BorderLayout.SOUTH);

        // =====================================================================
        // TAB 5: Analytics
        // =====================================================================
        JPanel pnlAnalytics = new JPanel(new BorderLayout(10, 10));
        pnlAnalytics.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        pieDataset = new DefaultPieDataset();
        JFreeChart pieChart = ChartFactory.createPieChart(
                "Live Capacity Breakdown (Safe & Available)", 
                pieDataset,  
                true, true, false        
        );
        ChartPanel chartPanel = new ChartPanel(pieChart);
        chartPanel.setBackground(Color.WHITE); 
        pnlAnalytics.add(chartPanel, BorderLayout.CENTER);

        JPanel pnlChartBtn = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnRefreshChart = new JButton("Refresh Analytics Data");
        stylePrimaryButton(btnRefreshChart);
        btnRefreshChart.addActionListener(e -> loadChartData());
        pnlChartBtn.add(btnRefreshChart);
        pnlAnalytics.add(pnlChartBtn, BorderLayout.SOUTH);

        // =====================================================================
        // ASSEMBLE TABS AND GLOBAL BACK BUTTON
        // =====================================================================
        tabbedPane.add("Add Stock", new JScrollPane(pnlAddBlood));
        tabbedPane.add("Lab Screening", pnlScreening);
        tabbedPane.add("Fulfill Requests", pnlRequests);
        tabbedPane.add("View Stock", pnlViewInventory);
        tabbedPane.add("Analytics", pnlAnalytics); 
        add(tabbedPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
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
        loadChartData();
    }

    // --- HELPER METHODS ---
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
                if (targetTable.equals("screening")) activeSorter = screeningSorter;
                if (targetTable.equals("request")) activeSorter = requestSorter;
                if (targetTable.equals("stock")) activeSorter = stockSorter;

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

    private void stylePrimaryButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(41, 128, 185));
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(250, 40)); 
    }

    private void styleSuccessButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(39, 174, 96)); 
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(220, 40)); 
    }

    private void styleDangerButton(JButton btn) {
        btn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btn.setBackground(new Color(231, 76, 60)); 
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(220, 40)); 
    }

    private void processScreening(boolean isSafe) {
        int selectedRow = screeningTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a bag from the table first.");
            return;
        }
        int modelRow = screeningTable.convertRowIndexToModel(selectedRow);
        String bagId = screeningModel.getValueAt(modelRow, 0).toString();
        
        new BloodBankController().screenBloodBag(bagId, isSafe);
        JOptionPane.showMessageDialog(this, isSafe ? "Bag approved for system release." : "Bag flagged for hazardous waste disposal.");
        loadTables();
        loadChartData(); 
    }

    private void loadTables() {
        stockModel.setRowCount(0);
        screeningModel.setRowCount(0);
        requestModel.setRowCount(0);
        
        try (Connection conn = databaseConnectors.getConnection();
             Statement stmt = conn.createStatement()) {
            
            ResultSet rs1 = stmt.executeQuery("SELECT bag_id, blood_group, status FROM Inventory WHERE status = 'Testing'");
            while (rs1.next()) {
                screeningModel.addRow(new Object[]{rs1.getInt("bag_id"), rs1.getString("blood_group"), rs1.getString("status")});
            }

            ResultSet rs2 = stmt.executeQuery("SELECT request_id, hospital_name, blood_group, urgency_level, quantity FROM Requests WHERE status = 'Pending'");
            while (rs2.next()) {
                requestModel.addRow(new Object[]{rs2.getInt("request_id"), rs2.getString("hospital_name"), rs2.getString("blood_group"), rs2.getString("urgency_level"), rs2.getInt("quantity")});
            }

            ResultSet rs3 = stmt.executeQuery("SELECT bag_id, blood_group, expiry_date, status, screening_status FROM Inventory ORDER BY expiry_date ASC");
            while (rs3.next()) {
                stockModel.addRow(new Object[]{rs3.getInt("bag_id"), rs3.getString("blood_group"), rs3.getDate("expiry_date"), rs3.getString("status"), rs3.getString("screening_status")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadChartData() {
        pieDataset.clear(); 
        String sql = "SELECT blood_group, COUNT(*) as amount FROM Inventory WHERE status = 'Available' GROUP BY blood_group";
        
        try (Connection conn = databaseConnectors.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String bg = rs.getString("blood_group");
                int amount = rs.getInt("amount");
                pieDataset.setValue(bg + " [" + amount + " Bags]", amount);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}