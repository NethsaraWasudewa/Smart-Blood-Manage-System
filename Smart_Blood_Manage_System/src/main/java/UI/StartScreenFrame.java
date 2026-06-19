package UI;

import javax.swing.*;
import java.awt.*;

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
            // FIXED: It now calls DonorRegistrationFrame directly since they are in the same package
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
        SwingUtilities.invokeLater(() -> {
            new StartScreenFrame().setVisible(true);
        });
    }
}