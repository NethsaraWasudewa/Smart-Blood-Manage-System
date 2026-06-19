package inventory;

import database.databaseConnectors;
import java.sql.*;
import java.util.TimerTask;

public class ExpiryTask extends TimerTask {

    @Override
    public void run() {
        // This SQL checks for bags where the expiry date has passed and marks them as Expired
        String sql = "UPDATE Inventory SET status = 'Expired' WHERE expiry_date < CURDATE() AND status IN ('Available', 'Testing')";
        
        try (Connection conn = databaseConnectors.getConnection();
             Statement stmt = conn.createStatement()) {
            
            int updatedBags = stmt.executeUpdate(sql);
            
            // This prints to the NetBeans console so you know the silent background task is working!
            System.out.println("--- BACKGROUND SYSTEM CHECK ---");
            System.out.println("Scanning for expired blood...");
            if (updatedBags > 0) {
                System.out.println("ALERT: " + updatedBags + " blood bag(s) have expired and were removed from availability.");
            } else {
                System.out.println("STATUS: All current inventory is safe and unexpired.");
            }
            System.out.println("-------------------------------");
            
        } catch (SQLException e) {
            System.out.println("Background Task Error: Database connection failed.");
            e.printStackTrace();
        }
    }
}