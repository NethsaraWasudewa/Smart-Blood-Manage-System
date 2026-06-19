package UI; 

import inventory.ExpiryTask;
import javax.swing.*;
import java.awt.*;
import java.util.Timer; // Needed for background task

public class StartScreenFrame extends JFrame {

    public StartScreenFrame() {
        setTitle("Smart Blood Allocation System");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(new GridLayout(5, 1, 10, 10));

        JLabel lblTitle = new JLabel("Select Your Portal", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));

        JButton btnDonor = new JButton("Donor Portal (Login / Register)");
        JButton btnHospital = new JButton("Hospital Portal");
        JButton btnBloodBank = new JButton("Blood Bank Portal");
        JButton btnAdmin = new JButton("Admin Dashboard");

        btnDonor.addActionListener(e -> {
            new DonorRegistrationFrame().setVisible(true); 
            this.dispose(); 
        });

        btnHospital.addActionListener(e -> {
            new HospitalRequestFrame().setVisible(true);
            this.dispose();
        });

        btnBloodBank.addActionListener(e -> {
            new InventoryManagementFrame().setVisible(true);
            this.dispose();
        });

        btnAdmin.addActionListener(e -> {
            new AdminDashboardFrame().setVisible(true);
            this.dispose();
        });

        add(lblTitle);
        add(btnDonor);
        add(btnHospital);
        add(btnBloodBank);
        add(btnAdmin);
    }

    public static void main(String[] args) {
        // --- START BACKGROUND AUTOMATION ---
        // 'true' makes it a daemon thread (runs silently in the background)
        Timer timer = new Timer(true); 
        
        // Schedule it to run instantly (0ms delay), and repeat every 24 hours (86400000 ms)
        timer.scheduleAtFixedRate(new ExpiryTask(), 0, 86400000);
        // -----------------------------------

        SwingUtilities.invokeLater(() -> {
            new StartScreenFrame().setVisible(true);
        });
    }
}