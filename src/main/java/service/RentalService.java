package service;

import dao.DBUtil;
import java.sql.*;

public class RentalService {

    // List cars that are available between [start, end] (no overlapping bookings)
    public void listAvailable(Date start, Date end) throws Exception {
        String sql =
                "SELECT car_id, model, type, price_per_day " +
                        "FROM cars c " +
                        "WHERE c.available = 1 AND NOT EXISTS ( " +
                        "  SELECT 1 FROM bookings b " +
                        "  WHERE b.car_id = c.car_id " +
                        "    AND b.start_date <= ? " +
                        "    AND b.end_date   >= ? " +
                        ")";
        try (Connection con = DBUtil.getConn();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setDate(1, end);
            ps.setDate(2, start);
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("\nAvailable cars:");
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    System.out.printf("#%d  %-10s  %-9s  ₹%.2f/day%n",
                            rs.getInt("car_id"),
                            rs.getString("model"),
                            rs.getString("type"),
                            rs.getDouble("price_per_day"));
                }
                if (!any) System.out.println("(none)");
            }
        }
    }

    // Book a car if no overlap exists in the chosen range
    public int bookCar(int carId, String customer, Date start, Date end) throws Exception {
        try (Connection con = DBUtil.getConn()) {
            con.setAutoCommit(false);
            try {
                // Re-check overlap inside transaction
                String overlap =
                        "SELECT 1 FROM bookings WHERE car_id=? AND start_date <= ? AND end_date >= ? LIMIT 1";
                try (PreparedStatement chk = con.prepareStatement(overlap)) {
                    chk.setInt(1, carId);
                    chk.setDate(2, end);
                    chk.setDate(3, start);
                    if (chk.executeQuery().next()) {
                        throw new Exception("Car already booked for these dates");
                    }
                }
                // Insert booking
                String ins =
                        "INSERT INTO bookings (car_id, customer, start_date, end_date) VALUES (?,?,?,?)";
                try (PreparedStatement ps = con.prepareStatement(ins, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, carId);
                    ps.setString(2, customer);
                    ps.setDate(3, start);
                    ps.setDate(4, end);
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        int id = keys.getInt(1);
                        con.commit();
                        return id;
                    }
                }
            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
            }
        }
    }

    // ===== Feature 1: Calculate total rental cost (days * price_per_day) =====
    public double calculateCost(int carId, Date start, Date end) throws Exception {
        String sql = "SELECT price_per_day FROM cars WHERE car_id = ?";
        try (Connection con = DBUtil.getConn();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, carId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new Exception("Car not found");
                double price = rs.getDouble("price_per_day");
                long millis = end.getTime() - start.getTime();
                long days = Math.max(1, millis / (1000L * 60 * 60 * 24));
                return days * price;
            }
        }
    }

    // ===== Feature 2: List cars by type (SUV/SEDAN/HATCHBACK) =====
    public void listCarsByType(String type) throws Exception {
        String sql = "SELECT car_id, model, price_per_day, available FROM cars WHERE type = ?";
        try (Connection con = DBUtil.getConn();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, type.toUpperCase());
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("\nCars of type: " + type.toUpperCase());
                boolean any = false;
                while (rs.next()) {
                    any = true;
                    System.out.printf("#%d | %-10s | ₹%.2f/day | Available: %d%n",
                            rs.getInt("car_id"),
                            rs.getString("model"),
                            rs.getDouble("price_per_day"),
                            rs.getInt("available"));
                }
                if (!any) System.out.println("(none)");
            }
        }
    }
}
