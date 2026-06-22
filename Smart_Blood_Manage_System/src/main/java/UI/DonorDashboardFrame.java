package UI;

import user.DonorController;
import database.databaseConnectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;

public class DonorDashboardFrame extends JFrame {
    private int loggedInDonorId;
    private String loggedInEmail;
    private DefaultTableModel eventsModel;
    private JTable eventsTable;

    public DonorDashboardFrame(int donorId, String email) {
        this.loggedInDonorId = donorId;
        this.loggedInEmail = email;

        setTitle("My Donor Dashboard");
        setSize(650, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        DonorController controller = new DonorController();
        String[] details = controller.getDonorDetails(loggedInDonorId);

        JPanel pnlProfile = new JPanel(new GridLayout(5, 1, 10, 10));
        pnlProfile.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel lblWelcome = new JLabel("Welcome back, " + details[0] + "!", SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 20));
        pnlProfile.add(lblWelcome);
        pnlProfile.add(new JLabel("Donor ID: " + loggedInDonorId));
        pnlProfile.add(new JLabel("Email: " + loggedInEmail));
        pnlProfile.add(new JLabel("Blood Group: " + details[1]));
        pnlProfile.add(new JLabel("Last Recorded Donation: " + details[2]));

        // --- UPCOMING EVENTS TAB ---
        JPanel pnlEvents = new JPanel(new BorderLayout());
        eventsModel = new DefaultTableModel(new String[]{"Event ID", "Event Name", "Location", "Date"}, 0);
        eventsTable = new JTable(eventsModel);
        pnlEvents.add(new JScrollPane(eventsTable), BorderLayout.CENTER);
        
        setupSearchPanel(pnlEvents, eventsModel, eventsTable); // SEARCH
        
        JButton btnRegisterEvent = new JButton("RSVP / Register for Selected Event");
        pnlEvents.add(btnRegisterEvent, BorderLayout.SOUTH);

        btnRegisterEvent.addActionListener(e -> {
            int selectedRow = eventsTable.getSelectedRow();
            if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Select an event."); return; }
            if (!controller.isEligibleToDonate(loggedInEmail)) {
                JOptionPane.showMessageDialog(this, "You are ineligible to register. You must wait 6 months between donations.", "Safety Warning", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int modelRow = eventsTable.convertRowIndexToModel(selectedRow); // SAFE INDEX
            int eventId = (Integer) eventsModel.getValueAt(modelRow, 0);
            
            if (controller.registerForEvent(loggedInDonorId, eventId)) {
                JOptionPane.showMessageDialog(this, "Successfully registered for the event!");
            } else {
                JOptionPane.showMessageDialog(this, "You are already registered for this event.");
            }
        });

        tabbedPane.add("My Profile", pnlProfile);
        tabbedPane.add("Upcoming Donation Drives", pnlEvents);
        add(tabbedPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> { new StartScreenFrame().setVisible(true); this.dispose(); });
        bottomPanel.add(btnLogout);
        add(bottomPanel, BorderLayout.SOUTH);

        loadUpcomingEvents();
    }

    // SEARCH ENGINE
    private void setupSearchPanel(JPanel panel, DefaultTableModel model, JTable table) {
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Live Search Filter: "));
        JTextField txtSearch = new JTextField(25);
        searchPanel.add(txtSearch);

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        txtSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            private void filter() {
                String text = txtSearch.getText();
                if (text.trim().length() == 0) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
            }
        });
        panel.add(searchPanel, BorderLayout.NORTH);
    }

    private void loadUpcomingEvents() {
        eventsModel.setRowCount(0);
        String sql = "SELECT event_id, event_name, location, event_date FROM Events WHERE event_date >= CURDATE() ORDER BY event_date ASC";
        try (Connection conn = databaseConnectors.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                eventsModel.addRow(new Object[]{ rs.getInt("event_id"), rs.getString("event_name"), rs.getString("location"), rs.getDate("event_date") });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}