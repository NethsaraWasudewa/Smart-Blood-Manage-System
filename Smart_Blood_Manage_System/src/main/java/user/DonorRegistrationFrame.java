// Inside DonorRegistrationFrame.java
private void btnRegisterActionPerformed(java.awt.event.ActionEvent evt) {
    // 1. Get text from your Swing inputs
    String name = txtDonorName.getText();
    String email = txtEmail.getText();
    String bloodGroup = cmbBloodGroup.getSelectedItem().toString();
    String location = txtLocation.getText();
    
    // Parse the date (assuming format YYYY-MM-DD for simplicity)
    java.time.LocalDate lastDonation = java.time.LocalDate.parse(txtDate.getText());

    // 2. Call the Controller
    user.DonorController controller = new user.DonorController();
    
    // Check eligibility first
    if (controller.isEligibleToDonate(email)) {
        boolean success = controller.registerDonor(name, email, bloodGroup, location, lastDonation);
        if (success) {
            javax.swing.JOptionPane.showMessageDialog(this, "Registration Successful!");
            this.dispose(); // Close this window
        } else {
            javax.swing.JOptionPane.showMessageDialog(this, "Error saving to database.", "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    } else {
        javax.swing.JOptionPane.showMessageDialog(this, "You must wait 6 months between donations.", "Ineligible", javax.swing.JOptionPane.WARNING_MESSAGE);
    }
}