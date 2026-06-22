package UI;

import Admin.AdminController;
import javax.swing.*;
import java.awt.*;

public class AdminLoginFrame extends JFrame {

    public AdminLoginFrame() {
        setTitle("Admin Security Portal - Login");
        setSize(350, 250);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 1, 10, 10));

        // Title
        JLabel lblTitle = new JLabel("System Administrator", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblTitle);

        // Input Fields (Note the use of JPasswordField for security)
        JPanel pnlInput = new JPanel(new GridLayout(2, 2, 5, 5));
        pnlInput.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        
        pnlInput.add(new JLabel("Admin Email:"));
        JTextField txtEmail = new JTextField();
        pnlInput.add(txtEmail);
        
        pnlInput.add(new JLabel("Password:"));
        JPasswordField txtPassword = new JPasswordField();
        pnlInput.add(txtPassword);
        
        add(pnlInput);

        // Login Button
        JPanel pnlButtons = new JPanel(new FlowLayout());
        JButton btnLogin = new JButton("Secure Login");
        pnlButtons.add(btnLogin);
        add(pnlButtons);

        // Back Button
        JButton btnBack = new JButton("Cancel / Back to Home");
        add(btnBack);

        // --- THE LOGIN LOGIC ---
        btnLogin.addActionListener(e -> {
            String email = txtEmail.getText();
            // Convert the secure password array to a string
            String password = new String(txtPassword.getPassword()); 
            
            AdminController controller = new AdminController();
            
            if (controller.loginAdmin(email, password)) {
                // Password is correct! Open the Dashboard.
                new AdminDashboardFrame().setVisible(true);
                this.dispose(); 
            } else {
                // Password failed! Block entry.
                JOptionPane.showMessageDialog(this, "ACCESS DENIED: Invalid Email or Password.", "Security Alert", JOptionPane.ERROR_MESSAGE);
            }
        });

        // --- THE BACK LOGIC ---
        btnBack.addActionListener(e -> {
            new StartScreenFrame().setVisible(true);
            this.dispose();
        });
    }
}