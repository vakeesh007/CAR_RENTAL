package app;

import service.RentalService;
import java.sql.Date;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) throws Exception {
        RentalService rs = new RentalService();
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println("\n=== Car Rental Ultra-Lite ===");
            System.out.println("1) Search availability & Book");
            System.out.println("2) Calculate total cost (no booking)");
            System.out.println("3) List cars by type (SUV/SEDAN/HATCHBACK)");
            System.out.println("0) Exit");
            System.out.print("Choose: ");

            String line = sc.nextLine().trim();
            if (line.isEmpty()) continue;

            int choice;
            try {
                choice = Integer.parseInt(line);
            } catch (NumberFormatException e) {
                System.out.println("Invalid choice.");
                continue;
            }

            if (choice == 0) {
                System.out.println("Goodbye!");
                break;
            }

            try {
                switch (choice) {
                    case 1 -> {
                        System.out.print("Start date (YYYY-MM-DD): ");
                        Date s = Date.valueOf(sc.nextLine().trim());
                        System.out.print("End date   (YYYY-MM-DD): ");
                        Date e = Date.valueOf(sc.nextLine().trim());

                        rs.listAvailable(s, e);

                        System.out.print("\nEnter car_id to book (or 0 to cancel): ");
                        int carId = Integer.parseInt(sc.nextLine().trim());
                        if (carId == 0) break;

                        double cost = rs.calculateCost(carId, s, e);
                        System.out.println("Total rental cost: ₹" + String.format("%.2f", cost));

                        System.out.print("Proceed to book? (y/n): ");
                        if (!sc.nextLine().trim().equalsIgnoreCase("y")) break;

                        System.out.print("Customer name: ");
                        String name = sc.nextLine().trim();

                        int bookingId = rs.bookCar(carId, name, s, e);
                        System.out.println("Booking confirmed! ID = " + bookingId);
                    }

                    case 2 -> {
                        System.out.print("Car ID: ");
                        int carId = Integer.parseInt(sc.nextLine().trim());
                        System.out.print("Start date (YYYY-MM-DD): ");
                        Date s = Date.valueOf(sc.nextLine().trim());
                        System.out.print("End date   (YYYY-MM-DD): ");
                        Date e = Date.valueOf(sc.nextLine().trim());
                        double cost = rs.calculateCost(carId, s, e);
                        System.out.println("Total rental cost: ₹" + String.format("%.2f", cost));
                    }

                    case 3 -> {
                        System.out.print("Enter type (SUV/SEDAN/HATCHBACK): ");
                        String t = sc.nextLine().trim();
                        rs.listCarsByType(t);
                    }

                    default -> System.out.println("Invalid choice.");
                }
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        }
    }
}
