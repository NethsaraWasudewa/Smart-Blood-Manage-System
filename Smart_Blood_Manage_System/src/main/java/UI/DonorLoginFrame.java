package UI;

import user.DonorController;
import javax.swing.*;
import java.awt.*;

public class DonorLoginFrame extends JFrame {
    
    public DonorLoginFrame() {
        setTitle("Donor Portal - Secure Login");
        setSize(350, 250);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 1, 10, 10));

        JLabel lblTitle = new JLabel("Donor Login", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblTitle);

        JPanel pnlInput = new JPanel(new GridLayout(2, 2, 5, 5));
        pnlInput.add(new JLabel("Email:")); 
        JTextField txtEmail = new JTextField(); 
        pnlInput.add(txtEmail);
        
        pnlInput.add(new JLabel("Password:")); 
        JPasswordField txtPassword = new JPasswordField(); 
        pnlInput.add(txtPassword);
        
        add(pnlInput);

        JPanel pnlButtons = new JPanel(new FlowLayout());
        JButton btnLogin = new JButton("Login");
        JButton btnRegister = new JButton("Create Account");
        pnlButtons.add(btnLogin); 
        pnlButtons.add(btnRegister);
        add(pnlButtons);

        JButton btnBack = new JButton("Back to Home");
        add(btnBack);

        btnLogin.addActionListener(e -> {
            String email = txtEmail.getText();
            String password = new String(txtPassword.getPassword());
            
            int donorId = new DonorController().loginDonor(email, password);
            if (donorId != -1) {
                new DonorDashboardFrame(donorId, email).setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Email or Password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRegister.addActionListener(e -> { new DonorRegistrationFrame().setVisible(true); this.dispose(); });
        btnBack.addActionListener(e -> { new StartScreenFrame().setVisible(true); this.dispose(); });
    }
}