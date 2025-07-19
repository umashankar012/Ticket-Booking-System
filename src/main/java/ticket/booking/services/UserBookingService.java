package ticket.booking.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ticket.booking.Util.UserServiceUtil;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class UserBookingService {

    private User currentUser;
    private List<User> userList;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 👇 path to the JSON file
    private static final String USERS_PATH = "main/java/ticket/booking/loacalDb/users.json";
    private static final String TRAIN_PATH = "main/java/ticket/booking/loacalDb/train.json";
    // Constructor: Load users from file or create new file if missing
    public UserBookingService() throws IOException {
        File file = new File(USERS_PATH);
        if (!file.exists()) {
            file.getParentFile().mkdirs(); // create folder if needed
            objectMapper.writeValue(file, new ArrayList<User>());
        }
        userList = objectMapper.readValue(file, new TypeReference<List<User>>() {});
    }

    // ✅ Sign up a new user
    public boolean signUp(User newUser) {
        boolean exists = userList.stream()
                .anyMatch(u -> u.getName().equalsIgnoreCase(newUser.getName()));

        if (exists) {
            System.out.println("User already exists!");
            return false;
        }

        newUser.setUserId(UUID.randomUUID().toString());
        newUser.setHashedPassword(UserServiceUtil.hashPassword(newUser.getPassword()));
        newUser.setTickets(new ArrayList<>());

        userList.add(newUser);

        try {
            saveUserListToFile();
            return true;
        } catch (IOException e) {
            System.out.println("Error saving user to file.");
            return false;
        }
    }

    // ✅ Login existing user
    public boolean loginUser(String username, String password) {
        String hashed = UserServiceUtil.hashPassword(password);

        Optional<User> match = userList.stream()
                .filter(u -> u.getName().equals(username) && u.getHashedPassword().equals(hashed))
                .findFirst();

        if (match.isPresent()) {
            currentUser = match.get();
            return true;
        }

        return false;
    }

    // 📌 Get current user (after login)
    public User getCurrentUser() {
        return currentUser;
    }

    // ✅ Fetch bookings of current user
    public void fetchBooking() {
        if (currentUser == null) {
            System.out.println("You must log in first.");
        } else {
            currentUser.printTickets();
        }
    }

    // ✅ Cancel booking by index
    public boolean cancelBooking(int ticketIndex) {
        if (currentUser == null) return false;

        if (ticketIndex >= 0 && ticketIndex < currentUser.getTickets().size()) {
            currentUser.getTickets().remove(ticketIndex);
            try {
                saveUserListToFile();
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    // ✅ Book seat
    public boolean bookSeat(Train train, int row, int col, String source, String dest, String date) {
        if (currentUser == null) return false;

        if (train.getSeats().get(row).get(col) == 0) {
            train.getSeats().get(row).set(col, 1);

            User.Ticket ticket = new User.Ticket(source, dest, date, train);
            currentUser.getTickets().add(ticket);

            try {
                saveUserListToFile();
                return true;
            } catch (IOException e) {
                return false;
            }
        }
        return false;
    }

    // ✅ Search for trains by route
    public List<Train> getTrains(String src, String dest) {
        try {
            TrainService trainService = new TrainService();
            return trainService.searchTrains(src, dest);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    // ✅ Save updated users to file
    private void saveUserListToFile() throws IOException {
        objectMapper.writeValue(new File(USERS_PATH), userList);
    }
}
