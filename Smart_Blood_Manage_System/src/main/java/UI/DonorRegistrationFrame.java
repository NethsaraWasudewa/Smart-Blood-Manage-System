package UI;

import user.DonorController;
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class DonorRegistrationFrame extends JFrame {

    public DonorRegistrationFrame() {
        setTitle("Donor Enrollment System Terminal");
        setSize(450, 380);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(7, 2, 5, 5));

        add(new JLabel("Full Name Ledger Entry:"));
        JTextField txtName = new JTextField();
        add(txtName);

        add(new JLabel("Communications Interface Email:"));
        JTextField txtEmail = new JTextField();
        add(txtEmail);

        add(new JLabel("Antigen Vector Category:"));
        JComboBox<String> cmbBloodGroup = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        add(cmbBloodGroup);

        add(new JLabel("Primary Core Geographic Location:"));
        JTextField txtLocation = new JTextField();
        add(txtLocation);

        add(new JLabel("Last Active Extraction Date (YYYY-MM-DD):"));
        JTextField txtDate = new JTextField();
        add(txtDate);

        JButton btnRegister = new JButton("Commit Ledger Profile");
        JButton btnBack = new JButton("Cancel Profile Creation"); 
        
        btnRegister.addActionListener(e -> {
            try {
                String name = txtName.getText();
                String email = txtEmail.getText();
                String bloodGroup = cmbBloodGroup.getSelectedItem().toString();
                String location = txtLocation.getText();
                LocalDate lastDonation = LocalDate.parse(txtDate.getText());

                DonorController controller = new DonorController();
                
                if (controller.isEligibleToDonate(email)) {
                    // FIXED: Now passing exactly 5 arguments to match the controller
                    controller.registerDonor(name, email, bloodGroup, location, lastDonation);
                    JOptionPane.showMessageDialog(this, "Profile committed safely.");
                    new DonorLoginFrame().setVisible(true);
                    this.dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Biomedical Constraint Error: Minimum 6-month safety gap required.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Validation Fault: Please ensure date is formatted as YYYY-MM-DD.");
            }
        });

        btnBack.addActionListener(e -> {
            new DonorLoginFrame().setVisible(true); 
            this.dispose();
        });

        add(btnRegister);
        add(btnBack); 
    }
}