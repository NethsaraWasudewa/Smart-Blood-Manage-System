package UI;

import user.DonorController;
import database.databaseConnectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class DonorDashboardFrame extends JFrame {
    private int loggedInDonorId;
    private String loggedInEmail;
    private DefaultTableModel eventsModel;

    public DonorDashboardFrame(int donorId, String email) {
        this.loggedInDonorId = donorId;
        this.loggedInEmail = email;

        setTitle("Personal Donor Dashboard Console");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel pnlProfile = new JPanel(new GridLayout(5, 1, 10, 10));
        pnlProfile.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        DonorController controller = new DonorController();
        String[] details = controller.getDonorDetails(loggedInDonorId);

        JLabel lblWelcome = new JLabel("System Clearance Session: " + details[0], SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 16));
        pnlProfile.add(lblWelcome);
        pnlProfile.add(new JLabel("System Architecture Global Identifier ID: " + loggedInDonorId));
        pnlProfile.add(new JLabel("Primary Endpoint Email: " + loggedInEmail));
        pnlProfile.add(new JLabel("Registered Biological Group Designation: " + details[1]));
        pnlProfile.add(new JLabel("Chronological Last Donation Event: " + details[2]));

        JPanel pnlEvents = new JPanel(new BorderLayout());
        eventsModel = new DefaultTableModel(new String[]{"Event ID", "Operational Manifest Name", "Geographic Sector", "Target Date"}, 0);
        JTable eventsTable = new JTable(eventsModel);
        pnlEvents.add(new JScrollPane(eventsTable), BorderLayout.CENTER);

        JButton btnRegisterEvent = new JButton("Commit RSVP Event Schedule Match");
        pnlEvents.add(btnRegisterEvent, BorderLayout.SOUTH);

        btnRegisterEvent.addActionListener(e -> {
            int selectedRow = eventsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Operational Selection Missing: Highlight target event drive location.");
                return;
            }

            if (!controller.isEligibleToDonate(loggedInEmail)) {
                JOptionPane.showMessageDialog(this, "CRITICAL PROTECTION BLOCK: Minimum safety threshold gap violated. 6-month biological regeneration runtime required between extraction events.", "Asset Protection Alert", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int eventId = (Integer) eventsModel.getValueAt(selectedRow, 0);
            boolean success = controller.registerForEvent(loggedInDonorId, eventId);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "RSVP verification recorded.");
            } else {
                JOptionPane.showMessageDialog(this, "Validation Duplication Exception: Entity registration already exists on targeted campaign event vector.");
            }
        });

        tabbedPane.add("System Clearance Profile", pnlProfile);
        tabbedPane.add("Active Deployment Campaigns", pnlEvents);
        add(tabbedPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JButton btnLogout = new JButton("Terminate Session (Logout)");
        btnLogout.addActionListener(e -> {
            new StartScreenFrame().setVisible(true);
            this.dispose();
        });
        bottomPanel.add(btnLogout);
        add(bottomPanel, BorderLayout.SOUTH);

        // This calls the method below!
        loadUpcomingEvents();
    }

    // FIXED: The missing method is now correctly placed here inside the class!
    private void loadUpcomingEvents() {
        eventsModel.setRowCount(0);
        String sql = "SELECT event_id, event_name, location, event_date FROM Events WHERE event_date >= CURDATE() ORDER BY event_date ASC";
        
        try (Connection conn = databaseConnectors.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                eventsModel.addRow(new Object[]{
                    rs.getInt("event_id"),
                    rs.getString("event_name"),
                    rs.getString("location"),
                    rs.getDate("event_date")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}