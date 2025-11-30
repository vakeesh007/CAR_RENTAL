package dao;
import java.sql.*;

public class DBUtil {
    private static final String URL  = "jdbc:mysql://localhost:3306/carrental_ultra?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";   // <-- change if needed
    private static final String PASS = "Sughavan8604";   // <-- change if needed

    public static Connection getConn() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
