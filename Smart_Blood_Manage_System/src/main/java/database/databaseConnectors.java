package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class databaseConnectors {
    private static final String URL = "jdbc:mysql://localhost:3306/BloodSystem";
    private static final String USER = "root"; // Update if your MySQL username is different
    private static final String PASSWORD = "@2026"; // Update to your MySQL password

    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.out.println("MySQL Driver not found!");
            e.printStackTrace();
            return null;
        }
    }
}