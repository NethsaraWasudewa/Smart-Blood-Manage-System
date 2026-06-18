package ui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class DonorRegistrationFrame extends JFrame {

    public DonorRegistrationFrame() {
        setTitle("Donor Registration");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(7, 2, 5, 5));

        // UI Components
        add(new JLabel("Full Name:"));
        JTextField txtName = new JTextField();
        add(txtName);

        add(new JLabel("Email Address:"));
        JTextField txtEmail = new JTextField();
        add(txtEmail);

        add(new JLabel("Blood Group:"));
        JComboBox<String> cmbBloodGroup = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        add(cmbBloodGroup);

        add(new JLabel("City/Location:"));
        JTextField txtLocation = new JTextField();
        add(txtLocation);

        add(new JLabel("Last Donation (YYYY-MM-DD):"));
        JTextField txtDate = new JTextField();
        add(txtDate);

        JButton btnRegister = new JButton("Register");
        JButton btnBack = new JButton("Back to Home");

        // Register Logic
        btnRegister.addActionListener(e -> {
            try {
                String name = txtName.getText();
                String email = txtEmail.getText();
                String bloodGroup = cmbBloodGroup.getSelectedItem().toString();
                String location = txtLocation.getText();
                LocalDate lastDonation = LocalDate.parse(txtDate.getText());

                user.DonorController controller = new user.DonorController();
                
                if (controller.isEligibleToDonate(email)) {
                    controller.registerDonor(name, email, bloodGroup, location, lastDonation);
                    JOptionPane.showMessageDialog(this, "Registration Successful!");
                } else {
                    JOptionPane.showMessageDialog(this, "Ineligible: Must wait 6 months between donations.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: Please ensure date format is YYYY-MM-DD");
            }
        });

        // Back Logic
        btnBack.addActionListener(e -> {
            new StartScreenFrame().setVisible(true);
            this.dispose();
        });

        add(btnRegister);
        add(btnBack);
    }
}