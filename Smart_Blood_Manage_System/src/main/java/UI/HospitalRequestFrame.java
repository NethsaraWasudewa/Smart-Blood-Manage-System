package ui;

import javax.swing.*;
import java.awt.*;

public class HospitalRequestFrame extends JFrame {

    public HospitalRequestFrame() {
        setTitle("Hospital Blood Request");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 2, 5, 5));

        add(new JLabel("Hospital Name:"));
        JTextField txtHospital = new JTextField();
        add(txtHospital);

        add(new JLabel("Required Blood Group:"));
        JComboBox<String> cmbBloodGroup = new JComboBox<>(new String[]{"A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"});
        add(cmbBloodGroup);

        add(new JLabel("Quantity (Bags):"));
        JSpinner spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 50, 1));
        add(spnQuantity);

        add(new JLabel("Urgency Level:"));
        JComboBox<String> cmbUrgency = new JComboBox<>(new String[]{"Standard", "Emergency"});
        add(cmbUrgency);

        JButton btnSubmit = new JButton("Submit Request");
        JButton btnBack = new JButton("Back to Home");

        btnSubmit.addActionListener(e -> {
            String hospital = txtHospital.getText();
            String blood = cmbBloodGroup.getSelectedItem().toString();
            int qty = (Integer) spnQuantity.getValue();
            String urgency = cmbUrgency.getSelectedItem().toString();

            hospital.HospitalController controller = new hospital.HospitalController();
            controller.requestBlood(hospital, blood, urgency, qty);
            
            JOptionPane.showMessageDialog(this, "Request Submitted Successfully.");

            if (urgency.equals("Emergency")) {
                emergency.EmergencyEngine engine = new emergency.EmergencyEngine();
                // Passing a default location for testing, update this to read from hospital location later
                engine.triggerEmergencyEmails(blood, "Colombo"); 
                JOptionPane.showMessageDialog(this, "CRITICAL: Emergency Emails Dispatched to Donors!");
            }
        });

        btnBack.addActionListener(e -> {
            new StartScreenFrame().setVisible(true);
            this.dispose();
        });

        add(btnSubmit);
        add(btnBack);
    }
}