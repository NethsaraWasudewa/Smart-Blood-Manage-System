package inventory;

import database.databaseConnectors;
import java.sql.*;
import java.util.TimerTask;

public class ExpiryTask extends TimerTask {
    @Override
    public void run() {
        String sql = "UPDATE Inventory SET status = 'Expired' WHERE expiry_date < CURDATE() AND status IN ('Available', 'Testing')";
        try (Connection conn = databaseConnectors.getConnection();
             Statement stmt = conn.createStatement()) {
            int updatedBags = stmt.executeUpdate(sql);
            System.out.println("--- BACKGROUND SYSTEM CHECK ---");
            if (updatedBags > 0) {
                System.out.println("ALERT: " + updatedBags + " blood bag(s) have expired.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}