package ticket.booking;

import okhttp3.*;
import ticket.booking.Util.UserServiceUtil;
import ticket.booking.entities.Train;
import ticket.booking.entities.User;
import ticket.booking.services.UserBookingService;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String TRAIN_PATH = "src/main/java/ticket/booking/loacalDb/train.json";
        String source = null;
        String Destination = null;
        OkHttpClient client = new OkHttpClient().newBuilder().build();

        Request request = new Request.Builder()
                .url("https://trackmytrain.co.in/wdata/train/10105")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String jsonResponse = response.body().string();
                System.out.println("JSON Response:\n" + jsonResponse);
            } else {
                System.out.println("Request failed: " + response.code());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

//            if(responseCode == HttpsURLConnection.HTTP_OK){
//                StringBuilder sb = new StringBuilder();
//                Scanner sc = new Scanner(connection.getInputStream());
//                while(sc.hasNext()){
//                    sb.append(sc.nextLine());
//                }
//                System.out.println(sb);
//
//            }


        System.out.println("Running Train Booking System");
        Scanner scanner = new Scanner(System.in);
        char option = 'a';
        UserBookingService userBookingService = null;
        Train trainSelectedForBooking = null;
        boolean flag = false;
        while (option != 'e') {
            System.out.println("\nChoose an option");
            if(flag == false){
                System.out.println("'s' -> Sign up");
                System.out.println("'l' -> Login");
            }
            else{
                System.out.println("'o' -> LogOut");
            }
            System.out.println("'f' -> Fetch Bookings");
            System.out.println("'t' -> Search Trains");
            System.out.println("'b' -> Book a Seat");
            System.out.println("'x' -> Cancel my Booking");
            System.out.println("'e' -> Exit the App");

            option = scanner.next().charAt(0);

            switch (option) {
                case 's': // Signup
                    System.out.println("Enter username to sign up:");
                    String signupName = scanner.next();
                    System.out.println("Enter password:");
                    String signupPassword = scanner.next();
                    String hashed = UserServiceUtil.hashPassword(signupPassword);
                    User newUser = new User(signupName, signupPassword, hashed, new ArrayList<>(), null);
                    try {
                        userBookingService = new UserBookingService();
                        if (userBookingService.signUp(newUser)) {
                            System.out.println("Signup successful!");
                        }
                    } catch (IOException e) {
                        System.out.println("Signup failed due to IO error.");
                    }
                    break;

                case 'l': // Login
                    if(flag){
                        System.out.println("Log Out Successfully");
                        flag = false;
                        break;
                    }
                    System.out.println("Enter username:");
                    String loginName = scanner.next();
                    System.out.println("Enter password:");
                    String loginPassword = scanner.next();
                    try {
                        userBookingService = new UserBookingService();
                        if (userBookingService.loginUser(loginName, loginPassword)) {
                            System.out.println("Login successful!");
                            flag = true;
                        } else {
                            System.out.println("Invalid credentials.");
                        }
                    } catch (IOException e) {
                        System.out.println("Login failed.");
                    }
                    break;

                case 'f':
                    if (userBookingService != null)
                        userBookingService.fetchBooking();
                    else System.out.println("Please login first.");
                    break;

                case 't':
                    if (userBookingService == null) {
                        System.out.println("Please login first.");
                        break;
                    }
                    System.out.println("Enter source station: ");
                    source = scanner.next();
                    System.out.println("Enter destination station: ");
                    String dest = scanner.next();
                    Destination = dest;
                    List<Train> trains = userBookingService.getTrains(source, dest);
                    if (trains.isEmpty()) {
                        System.out.println("No trains found between " + source + " and " + dest);
                        break;
                    }
                    int index = 0;

                    for (Train t : trains) {
                        System.out.println(index + ": Train ID " + t.getTrainId());
                        t.getStationTimes().forEach((station, time) ->
                                System.out.println("Station: " + station + " Time: " + time));
                        index++;
                    }
                    System.out.println("Select train index:");
                    int trainIndex = scanner.nextInt();
                    trainSelectedForBooking = trains.get(trainIndex);

                    System.out.println("Do you want to book tickets? (y/n)");
                    char ch = scanner.next().charAt(0);
                    if(ch == 'n'){
                        break;
                    }

                    bookTicket(source, Destination, trainSelectedForBooking, userBookingService);
                    break;

                case 'b':
                    if (trainSelectedForBooking == null || userBookingService == null) {
                        System.out.println("Search and select a train first.");
                        break;
                    }
                    List<List<Integer>> seats = trainSelectedForBooking.getSeats();
                    System.out.println("Available Seats (0 = free, 1 = booked):");
                    for (int i = 0; i < seats.size(); i++) {
                        for (int j = 0; j < seats.get(i).size(); j++) {
                            System.out.print(seats.get(i).get(j) + " ");
                        }
                        System.out.println();
                    }
                    System.out.println("Enter row:");
                    int row = scanner.nextInt();
                    System.out.println("Enter column:");
                    int col = scanner.nextInt();
                    while(row > seats.size() || col > seats.get(row).size()){
                        System.out.println("Enter the correct seat number ");
                        row = scanner.nextInt();
                        col = scanner.nextInt();

                    }
                    System.out.println("Enter source:");
                    String from = scanner.next();
                    System.out.println("Enter destination:");
                    String to = scanner.next();
                    System.out.println("Enter date (yyyy-mm-dd):");
                    String date = scanner.next();
                    while(!isValidDate(date)){
                        System.out.println("Enter the correct date");
                        date = scanner.next();
                        isValidDate(date);

                    }

                    if (userBookingService.bookSeat(trainSelectedForBooking, row, col, from, to, date)) {
                        System.out.println("Seat booked successfully.");
                        for (int i = 0; i < seats.size(); i++) {
                            for (int j = 0; j < seats.get(i).size(); j++) {
                                if(i == row && j == col){
                                    seats.get(i).set(j,1);
                                }
                                System.out.print(seats.get(i).get(j) + " ");
                            }
                            System.out.println();
                        }
                    } else {
                        System.out.println("Seat booking failed.");
                    }
                    break;

                case 'x':
                    if (userBookingService == null) {
                        System.out.println("Login first.");
                        break;
                    }
                    System.out.println("Enter ticket index to cancel:");
                    int ticketIndex = scanner.nextInt();
                    if (userBookingService.cancelBooking(ticketIndex)) {
                        System.out.println("Cancelled successfully.");
                    } else {
                        System.out.println("Invalid index.");
                    }
                    break;

                case 'o' :
                    System.out.println("log out succcessfully");
                    flag = false;
                    break;

                case 'e':
                    System.out.println("Exiting...");
                    break;
            }
        }
    }

    public static boolean isValidDate(String input){
       try{
           DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
           LocalDate date = LocalDate.parse(input, dateTimeFormatter);
           return !date.isBefore(LocalDate.now());
       }catch (DateTimeException e){
           return false;
       }

    }

    public static void bookTicket(String source, String destination, Train trainSelectedForBooking , UserBookingService userBookingService){
         Scanner scanner = new Scanner(System.in);
        List<List<Integer>> seats = trainSelectedForBooking.getSeats();
        System.out.println("Available Seats (0 = free, 1 = booked):");
        for (int i = 0; i < seats.size(); i++) {
            for (int j = 0; j < seats.get(i).size(); j++) {
                System.out.print(seats.get(i).get(j) + " ");
            }
            System.out.println();
        }
        System.out.println("Enter row:");
        int row = scanner.nextInt();
        System.out.println("Enter column:");
        int col = scanner.nextInt();

        while(row >= seats.size() || col >= seats.get(row).size()){
            System.out.println("Enter the correct seat number ");
            row = scanner.nextInt();
            col = scanner.nextInt();

        }
        System.out.println("Enter date (yyyy-mm-dd):");
        String date = scanner.next();

        while(!isValidDate(date)){
            System.out.println("Enter the correct date");
            date = scanner.next();
            isValidDate(date);

        }

        if (userBookingService.bookSeat(trainSelectedForBooking, row, col, source, destination, date)) {
            System.out.println("Seat booked successfully.");
            for (int i = 0; i < seats.size(); i++) {
                for (int j = 0; j < seats.get(i).size(); j++) {
                    if(i == row && j == col){
                        seats.get(i).set(j,1);
                    }
                    System.out.print(seats.get(i).get(j) + " ");
                }
                System.out.println();
            }
        } else {
            System.out.println("Seat booking failed.");
        }
    }
}
