package UI; 

import inventory.BloodBankController;
import database.databaseConnectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class InventoryManagementFrame extends JFrame {
    private DefaultTableModel tableModel;

    public InventoryManagementFrame() {
        setTitle("Blood Bank Inventory");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        // --- TAB 1: Add Blood ---
        JPanel pnlAddBlood = new JPanel(new GridLayout(3, 2, 5, 5));
        pnlAddBlood.add(new JLabel("Blood Group Received:"));
        JComboBox<String> cmbBloodGroup = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        pnlAddBlood.add(cmbBloodGroup);

        JButton btnAdd = new JButton("Add to Inventory");
        pnlAddBlood.add(btnAdd);

        btnAdd.addActionListener(e -> {
            BloodBankController controller = new BloodBankController();
            controller.addBloodBag(cmbBloodGroup.getSelectedItem().toString());
            JOptionPane.showMessageDialog(this, "Bag added to inventory.");
            loadInventoryData(); 
        });

        // --- TAB 2: View Inventory Table ---
        JPanel pnlViewInventory = new JPanel(new BorderLayout());
        tableModel = new DefaultTableModel(new String[]{"Bag ID", "Blood Group", "Collection Date", "Expiry Date", "Status"}, 0);
        JTable inventoryTable = new JTable(tableModel);
        pnlViewInventory.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Refresh Inventory");
        btnRefresh.addActionListener(e -> loadInventoryData());
        pnlViewInventory.add(btnRefresh, BorderLayout.SOUTH);

        tabbedPane.add("Add Stock", pnlAddBlood);
        tabbedPane.add("View Stock", pnlViewInventory);
        
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

        loadInventoryData();
    }

    private void loadInventoryData() {
        tableModel.setRowCount(0);
        String sql = "SELECT bag_id, blood_group, collection_date, expiry_date, status FROM Inventory ORDER BY expiry_date ASC";
        
        try (Connection conn = databaseConnectors.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("bag_id"), rs.getString("blood_group"), rs.getDate("collection_date"), rs.getDate("expiry_date"), rs.getString("status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}