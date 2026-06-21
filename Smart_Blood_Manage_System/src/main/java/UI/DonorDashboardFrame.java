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

    // Notice the constructor requires the Donor's ID and Email!
    public DonorDashboardFrame(int donorId, String email) {
        this.loggedInDonorId = donorId;
        this.loggedInEmail = email;

        setTitle("My Donor Dashboard");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();

        // --- TAB 1: Profile View ---
        JPanel pnlProfile = new JPanel(new GridLayout(5, 1, 10, 10));
        pnlProfile.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        DonorController controller = new DonorController();
        String[] details = controller.getDonorDetails(loggedInDonorId);

        JLabel lblWelcome = new JLabel("Welcome back, " + details[0] + "!", SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 20));
        pnlProfile.add(lblWelcome);
        pnlProfile.add(new JLabel("Donor ID: " + loggedInDonorId));
        pnlProfile.add(new JLabel("Email: " + loggedInEmail));
        pnlProfile.add(new JLabel("Blood Group: " + details[1]));
        pnlProfile.add(new JLabel("Last Recorded Donation: " + details[2]));

        // --- TAB 2: Upcoming Events ---
        JPanel pnlEvents = new JPanel(new BorderLayout());
        eventsModel = new DefaultTableModel(new String[]{"Event ID", "Event Name", "Location", "Date"}, 0);
        JTable eventsTable = new JTable(eventsModel);
        pnlEvents.add(new JScrollPane(eventsTable), BorderLayout.CENTER);

        JButton btnRegisterEvent = new JButton("RSVP / Register for Selected Event");
        pnlEvents.add(btnRegisterEvent, BorderLayout.SOUTH);

        btnRegisterEvent.addActionListener(e -> {
            int selectedRow = eventsTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "Please select an event from the list.");
                return;
            }

            // 1. Check Eligibility (6 Month Rule)
            if (!controller.isEligibleToDonate(loggedInEmail)) {
                JOptionPane.showMessageDialog(this, "You are ineligible to register. You must wait 6 months between donations.", "Safety Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // 2. Process Registration
            int eventId = (Integer) eventsModel.getValueAt(selectedRow, 0);
            boolean success = controller.registerForEvent(loggedInDonorId, eventId);
            
            if (success) {
                JOptionPane.showMessageDialog(this, "Successfully registered for the event! We will see you there.");
            } else {
                JOptionPane.showMessageDialog(this, "You are already registered for this event.");
            }
        });

        tabbedPane.add("My Profile", pnlProfile);
        tabbedPane.add("Upcoming Donation Drives", pnlEvents);
        add(tabbedPane, BorderLayout.CENTER);

        // --- GLOBAL LOGOUT BUTTON ---
        JPanel bottomPanel = new JPanel();
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> {
            new StartScreenFrame().setVisible(true);
            this.dispose();
        });
        bottomPanel.add(btnLogout);
        add(bottomPanel, BorderLayout.SOUTH);

        loadUpcomingEvents();
    }

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