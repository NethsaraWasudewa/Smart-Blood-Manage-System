package ui; 

import hospital.HospitalController;
import database.databaseConnectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class HospitalRequestFrame extends JFrame {
    private DefaultTableModel tableModel;

    public HospitalRequestFrame() {
        setTitle("Hospital Blood Request");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        // --- TAB 1: Live Blood Capacity ---
        JPanel pnlCapacity = new JPanel(new BorderLayout());
        tableModel = new DefaultTableModel(new String[]{"Blood Group", "Total Available Bags"}, 0);
        JTable capacityTable = new JTable(tableModel);
        pnlCapacity.add(new JScrollPane(capacityTable), BorderLayout.CENTER);
        
        JButton btnRefresh = new JButton("Check Current Stock");
        btnRefresh.addActionListener(e -> loadCapacityData());
        pnlCapacity.add(btnRefresh, BorderLayout.SOUTH);

        // --- TAB 2: Make a Request ---
        JPanel pnlRequest = new JPanel(new GridLayout(6, 2, 5, 5));
        pnlRequest.add(new JLabel("Hospital Name:"));
        JTextField txtHospital = new JTextField();
        pnlRequest.add(txtHospital);

        pnlRequest.add(new JLabel("Required Blood Group:"));
        JComboBox<String> cmbBloodGroup = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        pnlRequest.add(cmbBloodGroup);

        pnlRequest.add(new JLabel("Quantity (Bags):"));
        JSpinner spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        pnlRequest.add(spnQuantity);

        pnlRequest.add(new JLabel("Urgency Level:"));
        JComboBox<String> cmbUrgency = new JComboBox<>(new String[]{"Standard", "Emergency"});
        pnlRequest.add(cmbUrgency);

        JButton btnSubmit = new JButton("Submit Request");
        pnlRequest.add(btnSubmit);

        btnSubmit.addActionListener(e -> {
            HospitalController controller = new HospitalController();
            controller.requestBlood(txtHospital.getText(), cmbBloodGroup.getSelectedItem().toString(), cmbUrgency.getSelectedItem().toString(), (Integer) spnQuantity.getValue());
            JOptionPane.showMessageDialog(this, "Request Submitted to Blood Bank.");
        });

        tabbedPane.add("View Live Capacity", pnlCapacity);
        tabbedPane.add("Send Request", pnlRequest);
        
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

        loadCapacityData();
    }

    private void loadCapacityData() {
        tableModel.setRowCount(0);
        String sql = "SELECT blood_group, COUNT(*) as amount FROM Inventory WHERE status = 'Available' GROUP BY blood_group";
        
        try (Connection conn = databaseConnectors.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getString("blood_group"), rs.getInt("amount")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}