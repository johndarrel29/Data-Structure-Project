package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static Connection connection = null; // Maintain a single connection instance

    public static Connection DBConnect() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection("jdbc:mysql://localhost/momentum", "root", "");
                System.out.println("Connected to the database");
            }
            return connection;
        } catch (SQLException e) {
            e.printStackTrace();
            return null; // Return null if an exception occurs
        }
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
