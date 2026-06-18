package ui; // Or package inventory; depending on where you placed it

import javax.swing.*;
import java.awt.*;

public class InventoryManagementFrame extends JFrame {

    public InventoryManagementFrame() {
        setTitle("Blood Bank Inventory");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 2, 5, 5));

        add(new JLabel("Blood Group Received:"));
        JComboBox<String> cmbBloodGroup = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        add(cmbBloodGroup);

        JButton btnAdd = new JButton("Add to Inventory");
        JButton btnBack = new JButton("Back to Home");

        btnAdd.addActionListener(e -> {
            String blood = cmbBloodGroup.getSelectedItem().toString();
            
            // Assuming your controller is set up correctly in the inventory package
            inventory.BloodBankController controller = new inventory.BloodBankController();
            controller.addBloodBag(blood);
            
            JOptionPane.showMessageDialog(this, blood + " Bag added. Expiry auto-set to 42 days.");
            
            // I removed the refreshInventoryTable() line here so it compiles!
        });

        btnBack.addActionListener(e -> {
            new StartScreenFrame().setVisible(true);
            this.dispose();
        });

        add(btnAdd);
        add(btnBack);
    }
}