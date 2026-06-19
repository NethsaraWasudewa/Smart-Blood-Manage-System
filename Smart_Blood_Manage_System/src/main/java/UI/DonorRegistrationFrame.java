package UI; 

import user.DonorController;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class DonorRegistrationFrame extends JFrame {

    public DonorRegistrationFrame() {
        setTitle("Donor Registration");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(7, 2, 5, 5));

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
        JButton btnBack = new JButton("Back to Home"); // NEW BUTTON
        
        btnRegister.addActionListener(e -> {
            try {
                String name = txtName.getText();
                String email = txtEmail.getText();
                String bloodGroup = cmbBloodGroup.getSelectedItem().toString();
                String location = txtLocation.getText();
                LocalDate lastDonation = LocalDate.parse(txtDate.getText());

                DonorController controller = new DonorController();
                
                if (controller.isEligibleToDonate(email)) {
                    controller.registerDonor(name, email, bloodGroup, location, lastDonation);
                    JOptionPane.showMessageDialog(this, "Registration Successful!");
                } else {
                    JOptionPane.showMessageDialog(this, "Ineligible: Must wait 6 months.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: Check date format (YYYY-MM-DD)");
            }
        });

        // Navigation Logic
        btnBack.addActionListener(e -> {
            new StartScreenFrame().setVisible(true);
            this.dispose();
        });

        add(btnRegister);
        add(btnBack); // Added to the grid
    }
}