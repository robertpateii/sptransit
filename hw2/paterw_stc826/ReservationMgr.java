
import java.util.*;

public class ReservationMgr {

    /**
     * seats: index is the seat number, the string is the name on the
     * reservation null value means the seat is not reserved The methods for the
     * commands handle incrementing the seat index by one for the users.
     */
    protected final ArrayList<String> seats;

    public ReservationMgr(int numSeats) {
        seats = new ArrayList<>(numSeats);
        for(int i= 0;i<numSeats;i++)
            seats.add(null);
    }

    public ReservationMgr(ArrayList<String> recoveredSeatList) {
        seats = recoveredSeatList;
    }

    public String HandleCommand(String command) {
        command = command.trim().toLowerCase();
        String[] options = command.split(" ");
        String commandType = options[0];
        switch (commandType) {
            case "reserve":
                return reserve(options);
            case "bookseat":
                return bookSeat(options);
            case "search":
                return search(options);
            case "delete":
                return delete(options);
            default:
                return "Invalid command type: " + commandType;
        }
    }

    private String reserve(String[] options) {
        String name = options[1];

        if (seats.contains(name)) {
            return "Seat already booked against the name provided";
        }

        int firstAvailableIndex = -1;
        for (int i = 0; i < seats.size(); i++) {
            if (seats.get(i) == null) {
                firstAvailableIndex = i;
                break;
            }
        }

        if (firstAvailableIndex > -1) {
            seats.set(firstAvailableIndex, name);
            return "Seat assigned to you is " + firstAvailableIndex + 1;
        } else {
            return "Sold out - No seat available";
        }
    }

    private String bookSeat(String[] options) {
        String name = options[1];
        int seatNumber = Integer.parseInt(options[2]);
        int seatIndex = seatNumber - 1;

        if (seats.contains(name)) {
            return "Seat already booked against the name provided";
        }

        if(seatIndex>= seats.size())
            return "Invalid seat number "+seatNumber;

        if (seats.get(seatIndex) == null) {
            seats.set(seatIndex, name);
            return "Seat assigned to you is " + seatNumber;
        } else {
            return seatNumber + " is not available";
        }
    }

    private String search(String[] options) {
        String name = options[1];

        if (seats.contains(name)) {
            return Integer.toString(seats.indexOf(name) + 1);
        }
        return "No reservation found for " + name;
    }

    private String delete(String[] options) {
        String name = options[1];

        if (seats.contains(name)) {
            int seatIndex = seats.indexOf(name);
            seats.set(seatIndex, null);
            return Integer.toString(seatIndex + 1);
        }
        return "No reservation found for " + name;
    }
}
