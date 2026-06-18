package ui;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;

public class AdminDashboardFrame extends JFrame {

    public AdminDashboardFrame() {
        setTitle("Admin - Manage Events");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(6, 2, 5, 5));

        add(new JLabel("Event Name:"));
        JTextField txtEvent = new JTextField();
        add(txtEvent);

        add(new JLabel("Location:"));
        JTextField txtLocation = new JTextField();
        add(txtLocation);

        add(new JLabel("Date (YYYY-MM-DD):"));
        JTextField txtDate = new JTextField();
        add(txtDate);

        add(new JLabel("Target Capacity:"));
        JSpinner spnCapacity = new JSpinner(new SpinnerNumberModel(50, 10, 1000, 10));
        add(spnCapacity);

        JButton btnCreate = new JButton("Create Event");
        JButton btnBack = new JButton("Back to Home");

        btnCreate.addActionListener(e -> {
            try {
                String name = txtEvent.getText();
                String location = txtLocation.getText();
                LocalDate date = LocalDate.parse(txtDate.getText());
                int capacity = (Integer) spnCapacity.getValue();

                Admin.AdminController controller = new Admin.AdminController();
                controller.createDonationEvent(name, location, date, capacity);
                
                JOptionPane.showMessageDialog(this, "Donation Event Created!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: Please check inputs. Date must be YYYY-MM-DD.");
            }
        });

        btnBack.addActionListener(e -> {
            new StartScreenFrame().setVisible(true);
            this.dispose();
        });

        add(btnCreate);
        add(btnBack);
    }
}