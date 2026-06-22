package UI;

import user.DonorController;
import javax.swing.*;
import java.awt.*;

public class DonorLoginFrame extends JFrame {

    public DonorLoginFrame() {
        setTitle("Donor Portal - Identity Validation");
        setSize(350, 250);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(4, 1, 10, 10));

        JLabel lblTitle = new JLabel("Donor Portal Login Gateway", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Arial", Font.BOLD, 16));
        add(lblTitle);

        JPanel pnlInput = new JPanel(new FlowLayout());
        pnlInput.add(new JLabel("Registered Communications Email:"));
        JTextField txtEmail = new JTextField(15);
        pnlInput.add(txtEmail);
        add(pnlInput);

        JPanel pnlButtons = new JPanel(new FlowLayout());
        JButton btnLogin = new JButton("Authorize Session");
        JButton btnRegister = new JButton("Register Profile Account");
        pnlButtons.add(btnLogin);
        pnlButtons.add(btnRegister);
        add(pnlButtons);

        JButton btnBack = new JButton("Return to System Hub");
        add(btnBack);

        // --- Action Listeners ---
        btnLogin.addActionListener(e -> {
            String email = txtEmail.getText();
            DonorController controller = new DonorController();
            
            // FIXED: We are now passing exactly ONE argument (email) 
            // to match what DonorController is expecting.
            int donorId = controller.loginDonor(email);
            
            if (donorId != -1) {
                // Login successful, open dashboard and pass the ID and Email
                new DonorDashboardFrame(donorId, email).setVisible(true);
                this.dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Identity Resolution Error: Entered communication email does not match registered ledger parameters.", "Authentication Exception", JOptionPane.ERROR_MESSAGE);
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