package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central place for the MySQL connection.
 * Every DAO class calls DBConnection.getConnection().
 */
public class DBConnection {

    // ==========================================================
    //  >>> ENTER YOUR MYSQL DETAILS HERE <<<
    // ==========================================================
    private static final String DB_USER     = "root";           // <-- your MySQL username
    private static final String DB_PASSWORD = "sanjana1948";  // <-- your MySQL password
    // ==========================================================

    private static final String DB_URL =
            "jdbc:mysql://localhost:3306/project_team_finder"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    // Returns a new connection each time (callers close it with try-with-resources)
    public static Connection getConnection() throws SQLException {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // load the JDBC driver
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL JDBC driver not found. Add the connector .jar to the lib folder.", e);
        }
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    // Quick check used by Main to test the connection at startup
    public static boolean testConnection() {
        try (Connection con = getConnection()) {
            return con != null && !con.isClosed();
        } catch (SQLException e) {
            System.out.println("Database connection failed: " + e.getMessage());
            return false;
        }
    }
}