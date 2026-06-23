package UI;

import inventory.ExpiryTask;
// Import the new modern theme library
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.util.Timer;

public class StartScreenFrame extends JFrame {

    public StartScreenFrame() {
        setTitle("Smart Blood Allocation System");
        setSize(450, 400); // Made slightly larger for better proportions
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        
        // 1. Create a Master Panel to hold everything and add professional padding (Negative Space)
        JPanel masterPanel = new JPanel();
        // This adds 30 pixels of empty space on the top/bottom, and 50 pixels on the left/right
        masterPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        masterPanel.setLayout(new GridLayout(6, 1, 15, 15)); // 15px gap between buttons

        // 2. Styled Title
        JLabel lblTitle = new JLabel("Select Your System Portal", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22)); // Cleaner, modern font
        lblTitle.setForeground(new Color(41, 128, 185)); // A professional medical blue color

        // 3. Styled Buttons
        JButton btnDonor = new JButton("Donor Portal (Login / Register)");
        JButton btnHospital = new JButton("Hospital Portal");
        JButton btnBloodBank = new JButton("Blood Bank Portal");
        JButton btnAdmin = new JButton("Admin Portal (Restricted)");
        
        // Remove the ugly dotted focus boxes around clicked text
        btnDonor.setFocusPainted(false);
        btnHospital.setFocusPainted(false);
        btnBloodBank.setFocusPainted(false);
        btnAdmin.setFocusPainted(false);

        // --- Button Actions ---
        btnDonor.addActionListener(e -> {
            new DonorLoginFrame().setVisible(true); 
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

        // --- THE SECURITY FIX ---
        // Changed to open AdminLoginFrame instead of AdminDashboardFrame
        btnAdmin.addActionListener(e -> {
            new AdminLoginFrame().setVisible(true);
            this.dispose();
        });

        // Add components to the padded master panel
        masterPanel.add(lblTitle);
        masterPanel.add(new JLabel()); // An empty label just to add an extra visual gap under the title
        masterPanel.add(btnDonor);
        masterPanel.add(btnHospital);
        masterPanel.add(btnBloodBank);
        masterPanel.add(btnAdmin);

        // Add the master panel to the window
        add(masterPanel);
    }

    public static void main(String[] args) {
        
        // --- APPLY THE MODERN THEME HERE ---
        try {
            // Apply the Flat Light Look and Feel
            UIManager.setLookAndFeel(new FlatLightLaf());
            
            // Optional Global UI Tweaks to make it look even better
            UIManager.put("Button.arc", 15); // Rounds the corners of all buttons
            UIManager.put("Component.arc", 15); // Rounds text fields and drop-downs
            UIManager.put("Button.font", new Font("Segoe UI", Font.PLAIN, 14)); // Better default button font
        } catch (Exception ex) {
            System.err.println("Failed to initialize modern theme.");
        }
        // -----------------------------------

        Timer timer = new Timer(true); 
        timer.scheduleAtFixedRate(new ExpiryTask(), 0, 86400000); 

        SwingUtilities.invokeLater(() -> {
            new StartScreenFrame().setVisible(true);
        });
    }
}