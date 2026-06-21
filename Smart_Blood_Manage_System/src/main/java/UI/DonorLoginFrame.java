package UI;

import user.DonorController;
import javax.swing.*;
import java.awt.*;

public class DonorLoginFrame extends JFrame {

    public DonorLoginFrame() {
        setTitle("Donor Portal - Login");
        setSize(350, 250);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 1, 10, 10));

        JLabel lblTitle = new JLabel("Donor Login", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 18));
        add(lblTitle);

        JPanel pnlInput = new JPanel(new FlowLayout());
        pnlInput.add(new JLabel("Email Address:"));
        JTextField txtEmail = new JTextField(15);
        pnlInput.add(txtEmail);
        add(pnlInput);

        JPanel pnlButtons = new JPanel(new FlowLayout());
        JButton btnLogin = new JButton("Login");
        JButton btnRegister = new JButton("Create Account");
        pnlButtons.add(btnLogin);
        pnlButtons.add(btnRegister);
        add(pnlButtons);

        JButton btnBack = new JButton("Back to Home");
        add(btnBack);

        // --- Action Listeners ---
        btnLogin.addActionListener(e -> {
            String email = txtEmail.getText();
            DonorController controller = new DonorController();
            int donorId = controller.loginDonor(email);
            
            if (donorId != -1) {
                // Login successful, open dashboard and pass the ID and Email
                new DonorDashboardFrame(donorId, email).setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Email not found. Please create an account.", "Login Failed", JOptionPane.ERROR_MESSAGE);
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
    }
}