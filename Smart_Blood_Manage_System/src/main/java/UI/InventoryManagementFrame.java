package UI; 

import inventory.BloodBankController;
import database.databaseConnectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class InventoryManagementFrame extends JFrame {
    private DefaultTableModel stockModel;
    private DefaultTableModel screeningModel;
    private JTable screeningTable;

    public InventoryManagementFrame() {
        setTitle("Blood Bank Inventory & Quality Control");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        // --- TAB 1: Add Blood ---
        JPanel pnlAddBlood = new JPanel(new GridLayout(3, 2, 5, 5));
        pnlAddBlood.add(new JLabel("Blood Group Received:"));
        JComboBox<String> cmbBloodGroup = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        pnlAddBlood.add(cmbBloodGroup);
        JButton btnAdd = new JButton("Add to Inventory (Send to Testing)");
        pnlAddBlood.add(btnAdd);

        btnAdd.addActionListener(e -> {
            BloodBankController controller = new BloodBankController();
            controller.addBloodBag(cmbBloodGroup.getSelectedItem().toString());
            JOptionPane.showMessageDialog(this, "Bag added. Sent to Screening Lab.");
            loadTables(); 
        });

        // --- TAB 2: Screen Blood (NEW) ---
        JPanel pnlScreening = new JPanel(new BorderLayout());
        screeningModel = new DefaultTableModel(new String[]{"Bag ID", "Blood Group", "Status"}, 0);
        screeningTable = new JTable(screeningModel);
        pnlScreening.add(new JScrollPane(screeningTable), BorderLayout.CENTER);

        JPanel pnlScreeningBtns = new JPanel();
        JButton btnPass = new JButton("Mark Safe (Passed)");
        JButton btnFail = new JButton("Mark Unsafe (Discard)");
        pnlScreeningBtns.add(btnPass);
        pnlScreeningBtns.add(btnFail);
        pnlScreening.add(pnlScreeningBtns, BorderLayout.SOUTH);

        btnPass.addActionListener(e -> processScreening(true));
        btnFail.addActionListener(e -> processScreening(false));

        // --- TAB 3: View Stock ---
        JPanel pnlViewInventory = new JPanel(new BorderLayout());
        stockModel = new DefaultTableModel(new String[]{"Bag ID", "Blood Group", "Expiry Date", "Status", "Screening"}, 0);
        JTable inventoryTable = new JTable(stockModel);
        pnlViewInventory.add(new JScrollPane(inventoryTable), BorderLayout.CENTER);

        JButton btnRefresh = new JButton("Refresh Data");
        btnRefresh.addActionListener(e -> loadTables());
        pnlViewInventory.add(btnRefresh, BorderLayout.SOUTH);

        tabbedPane.add("Add Stock", pnlAddBlood);
        tabbedPane.add("Lab Screening", pnlScreening);
        tabbedPane.add("View Stock", pnlViewInventory);
        add(tabbedPane, BorderLayout.CENTER);

        // Global Back Button
        JPanel bottomPanel = new JPanel();
        JButton btnBack = new JButton("Back to Home");
        btnBack.addActionListener(e -> {
            new StartScreenFrame().setVisible(true);
            this.dispose();
        });
        bottomPanel.add(btnBack);
        add(bottomPanel, BorderLayout.SOUTH);

        loadTables();
    }

    private void processScreening(boolean isSafe) {
        int selectedRow = screeningTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Select a bag from the table first.");
            return;
        }
        String bagId = screeningModel.getValueAt(selectedRow, 0).toString();
        new BloodBankController().screenBloodBag(bagId, isSafe);
        JOptionPane.showMessageDialog(this, isSafe ? "Bag approved for use." : "Bag discarded.");
        loadTables();
    }

    private void loadTables() {
        stockModel.setRowCount(0);
        screeningModel.setRowCount(0);
        
        try (Connection conn = databaseConnectors.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Load Testing Bags
            ResultSet rs1 = stmt.executeQuery("SELECT bag_id, blood_group, status FROM Inventory WHERE status = 'Testing'");
            while (rs1.next()) {
                screeningModel.addRow(new Object[]{rs1.getInt("bag_id"), rs1.getString("blood_group"), rs1.getString("status")});
            }

            // Load All Bags
            ResultSet rs2 = stmt.executeQuery("SELECT bag_id, blood_group, expiry_date, status, screening_status FROM Inventory ORDER BY expiry_date ASC");
            while (rs2.next()) {
                stockModel.addRow(new Object[]{rs2.getInt("bag_id"), rs2.getString("blood_group"), rs2.getDate("expiry_date"), rs2.getString("status"), rs2.getString("screening_status")});
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}