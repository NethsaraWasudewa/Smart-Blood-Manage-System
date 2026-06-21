package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class databaseConnectors {
    // Make sure your MySQL username and password match your local machine
    private static final String URL = "jdbc:mysql://localhost:3306/BloodSystem";
    private static final String USER = "root";
    private static final String PASSWORD = "@2026"; // Change to your password

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}