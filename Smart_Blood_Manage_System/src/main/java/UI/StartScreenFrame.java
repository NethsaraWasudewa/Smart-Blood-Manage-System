package UI; 

import inventory.ExpiryTask;
import javax.swing.*;
import java.awt.*;
import java.util.Timer; 

public class StartScreenFrame extends JFrame {

    public StartScreenFrame() {
        setTitle("Smart Blood Allocation System");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); 
        setLayout(new GridLayout(5, 1, 10, 10));

        JLabel lblTitle = new JLabel("Select Your Portal", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblTitle);

        JButton btnDonor = new JButton("Donor Portal (Login / Register)");
        JButton btnHospital = new JButton("Hospital Portal");
        JButton btnBloodBank = new JButton("Blood Bank Portal");
        
        // Renamed to clarify it requires login
        JButton btnAdmin = new JButton("Admin Portal (Restricted)"); 

        // Button Actions
        btnDonor.addActionListener(e -> { new DonorLoginFrame().setVisible(true); this.dispose(); });
        btnHospital.addActionListener(e -> { new HospitalRequestFrame().setVisible(true); this.dispose(); });
        btnBloodBank.addActionListener(e -> { new InventoryManagementFrame().setVisible(true); this.dispose(); });
        
        // --- SECURED ROUTE: Now goes to AdminLoginFrame first! ---
        btnAdmin.addActionListener(e -> { new AdminLoginFrame().setVisible(true); this.dispose(); });

        add(btnDonor); 
        add(btnHospital); 
        add(btnBloodBank); 
        add(btnAdmin);
    }

    public static void main(String[] args) {
        // Starts the 24-hour background cleanup system
        Timer timer = new Timer(true); 
        timer.scheduleAtFixedRate(new ExpiryTask(), 0, 86400000); 

        SwingUtilities.invokeLater(() -> {
            new StartScreenFrame().setVisible(true);
        });
    }
}