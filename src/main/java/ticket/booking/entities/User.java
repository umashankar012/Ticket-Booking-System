package ticket.booking.entities;

import java.util.ArrayList;
import java.util.List;

public class User {
    private String name;
    private String password;
    private String hashedPassword;
    private List<Ticket> tickets;
    private String userId;

    // ✅ No-arg constructor for Jackson
    public User() {
        this.tickets = new ArrayList<>();
    }

    public User(String name, String password, String hashedPassword, List<Ticket> tickets, String userId) {
        this.name = name;
        this.password = password;
        this.hashedPassword = hashedPassword;
        this.tickets = tickets;
        this.userId = userId;
    }

    // ✅ Getters & Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getHashedPassword() { return hashedPassword; }
    public void setHashedPassword(String hashedPassword) { this.hashedPassword = hashedPassword; }

    public List<Ticket> getTickets() { return tickets; }
    public void setTickets(List<Ticket> tickets) { this.tickets = tickets; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public void printTickets() {
        if (tickets == null || tickets.isEmpty()) {
            System.out.println("No tickets booked.");
            return;
        }
        int i = 0;
        for (Ticket t : tickets) {
            System.out.println("Ticket #" + i++);
            System.out.println("From: " + t.source + " To: " + t.destination);
            System.out.println("Date: " + t.date_of_travel);
            System.out.println("Train: " + t.train.getTrainId() + " (" + t.train.getTrainNo() + ")");
            System.out.println("-------------------");
        }
    }

    public static class Ticket {
        public String source;
        public String destination;
        public String date_of_travel;
        public Train train;

        public Ticket() {}

        public Ticket(String source, String destination, String date_of_travel, Train train) {
            this.source = source;
            this.destination = destination;
            this.date_of_travel = date_of_travel;
            this.train = train;
        }
    }
}
