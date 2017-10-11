
package reservation;

import java.util.*;

public class ReservationManager {
    private static ArrayList<String> seats;

    public static void Initialize(int numberOfSeats) {
        seats = new ArrayList<String>(numberOfSeats);
    }

    public static String HandleCommand(String command) {
        String[] options = command.split(" ");
        String commandType = options[0].toLowerCase();
        switch (commandType) {
            case "reserve":
                return reserve(options);
            case "bookSeat":
                return bookSeat(options);
            case "search":
                return search(options);
            case "delete":
                return delete(options);
            default:
                return "Invalid command type: " + commandType;
        }
    }

    private synchronized static String reserve(String[] options) {
        String name = options[1];

        if(seats.contains(name))
            return "Seat already booked against the name provided";

        int firstAvailableIndex = -1;
        for(int i = 0; i<seats.size();i++)
        {
            if(seats.get(i)!=null)
            {
                firstAvailableIndex = i;
                break;
            }
        }

        if(firstAvailableIndex >-1)
        {
            seats.set(firstAvailableIndex,name);
            return "Seat assigned to you is "+firstAvailableIndex;
        }
        else
            return "Sold out - No seat available";
    }

    private synchronized static String bookSeat(String[] options) {
        String name = options[1];
        int seatNumber = Integer.parseInt(options[2]);

        if(seats.contains(name))
            return "Seat already booked against the name provided";

        if(seats.get(seatNumber)!=null)
        {
            seats.set(seatNumber,name);
            return "Seat assigned to you is "+seatNumber;
        }
        else
        {
            return seatNumber+" is not available";
        }
    }

    private synchronized static String search(String[] options) {
        String name = options[1];

        if(seats.contains(name))
            return Integer.toString(seats.indexOf(name));
        return "No reservation found for "+name;
    }

    private synchronized static String delete(String[] options) {
        String name = options[1];

        if(seats.contains(name))
        {
            int number = seats.indexOf(name);
            seats.set(number,null);
            return Integer.toString(number);
        }
        return "No reservation found for "+name;
    }
}