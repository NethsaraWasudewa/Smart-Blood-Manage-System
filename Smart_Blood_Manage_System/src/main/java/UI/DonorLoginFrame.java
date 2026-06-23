package UI;

import user.DonorController;
import javax.swing.*;
import java.awt.*;

public class DonorLoginFrame extends JFrame {

    public DonorLoginFrame() {
        setTitle("Donor Portal - Secure Login");
        setSize(400, 420); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false); 

        // 1. Master Panel with Professional Padding
        JPanel masterPanel = new JPanel();
        masterPanel.setLayout(new BorderLayout(10, 20));
        masterPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40)); 

        // --- TITLE ---
        JLabel lblTitle = new JLabel("Donor Login", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblTitle.setForeground(new Color(41, 128, 185)); // Medical Blue color
        masterPanel.add(lblTitle, BorderLayout.NORTH);

        // --- FORM PANEL (Perfect Alignment) ---
        JPanel pnlForm = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 0, 5, 0); 
        gbc.weightx = 1.0;

        JLabel lblEmail = new JLabel("Email Address:");
        lblEmail.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblEmail.setForeground(Color.DARK_GRAY);
        JTextField txtEmail = new JTextField();
        txtEmail.setPreferredSize(new Dimension(200, 30)); 

        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPassword.setForeground(Color.DARK_GRAY);
        JPasswordField txtPassword = new JPasswordField();
        txtPassword.setPreferredSize(new Dimension(200, 30)); 

        gbc.gridy = 0; pnlForm.add(lblEmail, gbc);
        gbc.gridy = 1; pnlForm.add(txtEmail, gbc);
        gbc.gridy = 2; pnlForm.add(new JLabel(" "), gbc); // Spacer
        gbc.gridy = 3; pnlForm.add(lblPassword, gbc);
        gbc.gridy = 4; pnlForm.add(txtPassword, gbc);

        masterPanel.add(pnlForm, BorderLayout.CENTER);

        // --- BUTTON PANEL ---
        JPanel pnlButtons = new JPanel(new GridLayout(3, 1, 10, 10)); 

        // Primary Button (Blue)
        JButton btnLogin = new JButton("Secure Login");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(new Color(41, 128, 185)); 
        btnLogin.setForeground(Color.WHITE); 
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Secondary Button (White/Gray)
        JButton btnRegister = new JButton("Create New Account");
        btnRegister.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnRegister.setFocusPainted(false);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Subtle Back Link
        JButton btnBack = new JButton("← Back to Home");
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        btnBack.setContentAreaFilled(false); 
        btnBack.setBorderPainted(false); 
        btnBack.setForeground(Color.GRAY);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));

        pnlButtons.add(btnLogin);
        pnlButtons.add(btnRegister);
        pnlButtons.add(btnBack);

        masterPanel.add(pnlButtons, BorderLayout.SOUTH);

        // --- ACTION LISTENERS ---
        btnLogin.addActionListener(e -> {
            String email = txtEmail.getText();
            // Securely extract the password from the JPasswordField
            String password = new String(txtPassword.getPassword());
            
            DonorController controller = new DonorController();
            
            // FIXED: We are now passing BOTH the email and the password!
            int donorId = controller.loginDonor(email, password);
            
            if (donorId != -1) {
                new DonorDashboardFrame(donorId, email).setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Invalid Email or Password.", "Login Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        btnRegister.addActionListener(e -> {
            new DonorRegistrationFrame().setVisible(true);
            this.dispose();
        });

        btnBack.addActionListener(e -> {
            new StartScreenFrame().setVisible(true);
            this.dispose();
        });

        add(masterPanel);
    }
}