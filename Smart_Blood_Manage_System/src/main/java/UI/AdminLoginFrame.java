package UI;

import Admin.AdminController;
import javax.swing.*;
import java.awt.*;

public class AdminLoginFrame extends JFrame {

    // Modern Theme Color matching your dashboard
    private final Color primaryBlue = new Color(41, 128, 185);

    public AdminLoginFrame() {
        setTitle("Admin Security Portal - Login");
        // INCREASED SIZE to give the text fields plenty of room to breathe
        setSize(450, 400); 
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Master Panel with clean padding
        JPanel masterPanel = new JPanel();
        masterPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));
        masterPanel.setLayout(new GridLayout(4, 1, 10, 10));
        masterPanel.setBackground(Color.WHITE);
        getContentPane().setBackground(Color.WHITE);

        // 1. Hero Title
        JLabel lblTitle = new JLabel("System Administrator", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(primaryBlue);
        masterPanel.add(lblTitle);

        // 2. Input Fields (Stacked layout so text boxes get full width)
        JPanel pnlInput = new JPanel(new GridLayout(4, 1, 5, 5)); 
        pnlInput.setBackground(Color.WHITE);
        
        JLabel lblEmail = new JLabel("Admin Email:");
        lblEmail.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JTextField txtEmail = new JTextField();
        
        JLabel lblPassword = new JLabel("Password:");
        lblPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JPasswordField txtPassword = new JPasswordField();
        
        pnlInput.add(lblEmail);
        pnlInput.add(txtEmail);
        pnlInput.add(lblPassword);
        pnlInput.add(txtPassword);
        
        masterPanel.add(pnlInput);

        // 3. Login Button (Full width and styled)
        JPanel pnlButtons = new JPanel(new BorderLayout());
        pnlButtons.setBackground(Color.WHITE);
        pnlButtons.setBorder(BorderFactory.createEmptyBorder(15, 0, 0, 0)); // Space above button
        
        JButton btnLogin = new JButton("Secure Login");
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setBackground(primaryBlue);
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnLogin.setPreferredSize(new Dimension(0, 45)); // Tall, clickable button
        
        pnlButtons.add(btnLogin, BorderLayout.CENTER);
        masterPanel.add(pnlButtons);

        // 4. Clean Back Button
        JPanel pnlBack = new JPanel();
        pnlBack.setBackground(Color.WHITE);
        
        JButton btnBack = new JButton("← Cancel / Back to Home");
        btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnBack.setForeground(new Color(120, 120, 120)); // Subtle gray
        btnBack.setContentAreaFilled(false);
        btnBack.setBorderPainted(false);
        btnBack.setFocusPainted(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        pnlBack.add(btnBack);
        masterPanel.add(pnlBack);

        add(masterPanel);

        // --- THE LOGIN LOGIC ---
        btnLogin.addActionListener(e -> {
            String email = txtEmail.getText();
            String password = new String(txtPassword.getPassword());

            AdminController controller = new AdminController();

            if (controller.loginAdmin(email, password)) {
                new AdminDashboardFrame().setVisible(true);
                this.dispose();
            } else {
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