package UI; 

import user.DonorController;
import com.toedter.calendar.JDateChooser; 
import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;

public class DonorRegistrationFrame extends JFrame {
    
    public DonorRegistrationFrame() {
        setTitle("Donor Registration");
        setSize(400, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(8, 2, 5, 5));

        add(new JLabel("Full Name:")); 
        JTextField txtName = new JTextField(); 
        add(txtName);
        
        add(new JLabel("Email Address:")); 
        JTextField txtEmail = new JTextField(); 
        add(txtEmail);
        
        add(new JLabel("Create Password:")); 
        JPasswordField txtPassword = new JPasswordField(); 
        add(txtPassword);
        
        add(new JLabel("Blood Group:")); 
        JComboBox<String> cmbBloodGroup = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"}); 
        add(cmbBloodGroup);
        
        add(new JLabel("City/Location:")); 
        JTextField txtLocation = new JTextField(); 
        add(txtLocation);
        
        add(new JLabel("Last Donation (Leave blank if never):")); 
        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd"); 
        add(dateChooser);

        JButton btnRegister = new JButton("Register");
        JButton btnBack = new JButton("Cancel"); 
        
        btnRegister.addActionListener(e -> {
            try {
                String email = txtEmail.getText();
                String password = new String(txtPassword.getPassword());
                
                if(email.isEmpty() || password.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Email and Password are required.");
                    return;
                }

                DonorController controller = new DonorController();
                LocalDate lastDonationDate = null;
                if (dateChooser.getDate() != null) {
                    lastDonationDate = dateChooser.getDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                }

                if (controller.isEligibleToDonateFormDate(lastDonationDate)) {
                    boolean success = controller.registerDonor(txtName.getText(), email, password, cmbBloodGroup.getSelectedItem().toString(), txtLocation.getText(), lastDonationDate);
                    if (success) {
                        JOptionPane.showMessageDialog(this, "Registration Successful! You can now log in.");
                        new DonorLoginFrame().setVisible(true); 
                        this.dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Error: Email might already be registered.");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Registration Blocked: For your safety, you must wait 6 months between donations.", "Eligibility Error", JOptionPane.WARNING_MESSAGE);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "An unexpected error occurred.");
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