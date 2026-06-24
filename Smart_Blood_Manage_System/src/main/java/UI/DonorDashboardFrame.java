package UI;

import user.DonorController;
import database.databaseConnectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class DonorDashboardFrame extends JFrame {
    private int loggedInDonorId;
    private String loggedInEmail;
    
    private DefaultTableModel eventsModel;
    private DefaultTableModel myEventsModel;
    private JTable eventsTable;
    private JTable myEventsTable;

    public DonorDashboardFrame(int donorId, String email) {
        this.loggedInDonorId = donorId;
        this.loggedInEmail = email;

        setTitle("My Donor Dashboard");
        setSize(750, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        JTabbedPane tabbedPane = new JTabbedPane();
        DonorController controller = new DonorController();
        String[] details = controller.getDonorDetails(loggedInDonorId);

        // --- TAB 1: PROFILE ---
        JPanel pnlProfile = new JPanel(new GridLayout(5, 1, 10, 10));
        pnlProfile.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JLabel lblWelcome = new JLabel("Welcome back, " + details[0] + "!", SwingConstants.CENTER);
        lblWelcome.setFont(new Font("Arial", Font.BOLD, 20));
        pnlProfile.add(lblWelcome);
        pnlProfile.add(new JLabel("Donor ID: " + loggedInDonorId));
        pnlProfile.add(new JLabel("Email: " + loggedInEmail));
        pnlProfile.add(new JLabel("Blood Group: " + details[1]));
        pnlProfile.add(new JLabel("Last Recorded Donation: " + details[2]));

        // --- TAB 2: BROWSE UPCOMING EVENTS ---
        JPanel pnlEvents = new JPanel(new BorderLayout());
        eventsModel = new DefaultTableModel(new String[]{"Event ID", "Event Name", "Location", "Date"}, 0);
        eventsTable = new JTable(eventsModel);
        pnlEvents.add(new JScrollPane(eventsTable), BorderLayout.CENTER);
        
        setupSearchPanel(pnlEvents, eventsModel, eventsTable); 
        
        JButton btnRegisterEvent = new JButton("RSVP / Register for Selected Event");
        pnlEvents.add(btnRegisterEvent, BorderLayout.SOUTH);

        btnRegisterEvent.addActionListener(e -> {
            int selectedRow = eventsTable.getSelectedRow();
            if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Select an event from the list."); return; }
            
            int modelRow = eventsTable.convertRowIndexToModel(selectedRow); 
            int eventId = (Integer) eventsModel.getValueAt(modelRow, 0);
            
            String eventName = (String) eventsModel.getValueAt(modelRow, 1);
            String eventLocation = (String) eventsModel.getValueAt(modelRow, 2);
            java.sql.Date sqlDate = (java.sql.Date) eventsModel.getValueAt(modelRow, 3);
            LocalDate eventDate = sqlDate.toLocalDate();
            
            if (controller.checkEventEligibility(loggedInDonorId, eventDate)) {
                if (controller.registerForEvent(loggedInDonorId, eventId, loggedInEmail, eventName, eventDate, eventLocation)) {
                    JOptionPane.showMessageDialog(this, "Successfully registered! A confirmation email has been sent to your inbox.");
                    loadMyEvents(); 
                } else {
                    JOptionPane.showMessageDialog(this, "You are already registered for this event.");
                }
            } else {
                JOptionPane.showMessageDialog(this, "Registration Blocked:\nYou are already registered for another event within 6 months of this date, or it is too soon after your last physical donation.", "Health & Safety Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        // --- TAB 3: MY REGISTERED EVENTS ---
        JPanel pnlMyEvents = new JPanel(new BorderLayout());
        myEventsModel = new DefaultTableModel(new String[]{"Event Name", "Location", "Date & Time"}, 0);
        myEventsTable = new JTable(myEventsModel);
        pnlMyEvents.add(new JScrollPane(myEventsTable), BorderLayout.CENTER);
        
        JPanel pnlMyEventsBtns = new JPanel();
        JButton btnRefreshMyEvents = new JButton("Refresh My Schedule");
        JButton btnCancelEvent = new JButton("Cancel Selected Registration");
        
        btnCancelEvent.addActionListener(e -> {
            int selectedRow = myEventsTable.getSelectedRow();
            if (selectedRow == -1) { JOptionPane.showMessageDialog(this, "Select a registration to cancel."); return; }
            
            String eventName = myEventsModel.getValueAt(selectedRow, 0).toString();
            
            if (JOptionPane.showConfirmDialog(this, "Are you sure you want to cancel your registration for: " + eventName + "?", "Confirm Cancellation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                if (controller.cancelRegistration(loggedInDonorId, eventName)) {
                    JOptionPane.showMessageDialog(this, "Registration Cancelled Successfully.");
                    loadMyEvents();
                } else {
                    JOptionPane.showMessageDialog(this, "Error cancelling registration.");
                }
            }
        });
        
        pnlMyEventsBtns.add(btnRefreshMyEvents);
        pnlMyEventsBtns.add(btnCancelEvent);
        pnlMyEvents.add(pnlMyEventsBtns, BorderLayout.SOUTH);

        btnRefreshMyEvents.addActionListener(e -> loadMyEvents());

        // Build Tabs
        tabbedPane.add("My Profile", pnlProfile);
        tabbedPane.add("Browse Events", pnlEvents);
        tabbedPane.add("My Registrations", pnlMyEvents); 
        add(tabbedPane, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        JButton btnLogout = new JButton("Logout");
        btnLogout.addActionListener(e -> { new StartScreenFrame().setVisible(true); this.dispose(); });
        bottomPanel.add(btnLogout);
        add(bottomPanel, BorderLayout.SOUTH);

        loadUpcomingEvents();
        loadMyEvents(); 
    }

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

    private void loadMyEvents() {
        myEventsModel.setRowCount(0);
        String sql = "SELECT e.event_name, e.location, e.event_date FROM Events e JOIN Event_Registrations r ON e.event_id = r.event_id WHERE r.donor_id = ? ORDER BY e.event_date DESC";
        try (Connection conn = databaseConnectors.getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, loggedInDonorId);
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                myEventsModel.addRow(new Object[]{ rs.getString("event_name"), rs.getString("location"), rs.getDate("event_date") });
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }
}