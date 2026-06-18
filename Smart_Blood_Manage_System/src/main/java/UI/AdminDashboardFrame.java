package ui; 

import Admin.AdminController;
import database.databaseConnectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class AdminDashboardFrame extends JFrame {
    private JTable donorTable;
    private DefaultTableModel tableModel;

    public AdminDashboardFrame() {
        setTitle("Admin Dashboard - Manage System");
        setSize(600, 450); // Slightly taller to fit the back button
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout()); // Main layout

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

        tabbedPane.add("Create Event", pnlCreateEvent);
        tabbedPane.add("Manage Donors", pnlManageDonors);
        
        add(tabbedPane, BorderLayout.CENTER); // Add tabs to center

        // --- GLOBAL BACK BUTTON ---
        JPanel bottomPanel = new JPanel();
        JButton btnBack = new JButton("Back to Home");
        btnBack.addActionListener(e -> {
            new StartScreenFrame().setVisible(true);
            this.dispose();
        });
        bottomPanel.add(btnBack);
        add(bottomPanel, BorderLayout.SOUTH); // Add back button to bottom

        loadDonorData();
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
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}