package example.BookingBE.Service;

import example.BookingBE.Entity.Booking;
import example.BookingBE.Entity.Room;
import example.BookingBE.Entity.User;
import example.BookingBE.Repository.BookingRepository;
import example.BookingBE.Repository.RoomRepository;
import example.BookingBE.Repository.UserRepository;
import example.BookingBE.Service.Booking.BookingService;
import example.BookingBE.Service.Room.RoomService;
import example.BookingBE.Service.User.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service to integrate chatbot with booking system functionality
 */
@Service
public class BookingIntegrationService {
    private static final Logger logger = LoggerFactory.getLogger(BookingIntegrationService.class);

    @Autowired
    private RoomService roomService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get available room types with detailed information
     * @return List of room types with descriptions and prices as a formatted string
     */
    public String getAvailableRoomTypes() {
        try {
            // Get all room types
            List<String> roomTypes = roomService.getAllRoomTypes();
            if (roomTypes.isEmpty()) {
                return "There are no room types available at the moment.";
            }

            // Get all rooms to extract details for each type
            List<Room> allRooms = roomRepository.findAll();

            // Group rooms by type
            Map<String, List<Room>> roomsByType = allRooms.stream()
                .collect(Collectors.groupingBy(Room::getRoomType));

            StringBuilder response = new StringBuilder("# Available Room Types\n\n");

            // For each room type, get details from the first room of that type
            for (String type : roomTypes) {
                List<Room> roomsOfType = roomsByType.get(type);
                if (roomsOfType != null && !roomsOfType.isEmpty()) {
                    Room sampleRoom = roomsOfType.get(0);

                    response.append("## **").append(type).append("**\n");
                    response.append("* **Description:** ").append(sampleRoom.getRoomDescription()).append("\n");
                    response.append("* **Price:** $").append(sampleRoom.getRoomPrice()).append(" per night\n");
                    response.append("* **Available rooms:** ").append(roomsOfType.size()).append("\n\n");
                } else {
                    // Fallback if no rooms of this type exist (shouldn't happen normally)
                    response.append("## **").append(type).append("**\n");
                    response.append("* No detailed information available for this room type.\n\n");
                }
            }

            response.append("To check availability for specific dates, please tell me your check-in and check-out dates.");
            return response.toString();
        } catch (Exception e) {
            logger.error("Error getting room types: ", e);
            return "Sorry, I couldn't retrieve the room types at the moment.";
        }
    }

    /**
     * Get available rooms for a specific date range and room type
     * @param checkInDate Check-in date
     * @param checkOutDate Check-out date
     * @param roomType Room type (optional)
     * @return Formatted string with available rooms
     */
    public String getAvailableRooms(String checkInDate, String checkOutDate, String roomType) {
        try {
            logger.debug("Checking room availability for dates: {} to {}, type: {}", checkInDate, checkOutDate, roomType);

            LocalDate checkIn = parseDate(checkInDate);
            LocalDate checkOut = parseDate(checkOutDate);

            if (checkIn == null || checkOut == null) {
                logger.warn("Invalid date format provided: {} to {}", checkInDate, checkOutDate);
                return "Please provide valid dates in the format YYYY-MM-DD. I couldn't understand the dates you provided.";
            }

            logger.debug("Parsed dates: {} to {}", checkIn, checkOut);

            if (checkIn.isAfter(checkOut)) {
                return "Check-in date must be before check-out date. You provided check-in: " + checkIn + " and check-out: " + checkOut;
            }

            if (checkIn.isBefore(LocalDate.now())) {
                return "Check-in date cannot be in the past. The earliest available date is " + LocalDate.now() + ".";
            }

            List<Room> availableRooms;
            if (roomType != null && !roomType.isEmpty()) {
                availableRooms = roomRepository.findAvailableRoomsByDateAndType(checkIn, checkOut, roomType);
            } else {
                // Get all available rooms for the date range
                availableRooms = roomRepository.findAll().stream()
                    .filter(room -> isRoomAvailable(room, checkIn, checkOut))
                    .collect(Collectors.toList());
            }

            if (availableRooms.isEmpty()) {
                return "No rooms available for the selected dates" +
                       (roomType != null ? " and room type '" + roomType + "'" : "") + ".";
            }

            StringBuilder response = new StringBuilder();
            response.append("Checking availability for your stay from **")
                   .append(checkIn.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")))
                   .append("** to **")
                   .append(checkOut.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")))
                   .append("** (")
                   .append(checkIn.until(checkOut).getDays())
                   .append(" nights)...\n\n");

            response.append("Please wait a moment while I check our real-time availability data.\n\n");

            // Group available rooms by type
            Map<String, List<Room>> roomsByType = availableRooms.stream()
                .collect(Collectors.groupingBy(Room::getRoomType));

            // Get all room types from the database for comparison
            List<String> allRoomTypes = roomService.getAllRoomTypes();

            // Find which room types are not available
            List<String> unavailableRoomTypes = new ArrayList<>(allRoomTypes);
            unavailableRoomTypes.removeAll(roomsByType.keySet());

            // Build the response
            if (unavailableRoomTypes.isEmpty()) {
                response.append("Great news! All room types are available for your selected dates.\n\n");
            } else {
                response.append("I apologize, but the following room types are not available for your selected dates:\n");
                for (String type : unavailableRoomTypes) {
                    response.append("- **").append(type).append("**\n");
                }
                response.append("\n");
            }

            if (roomsByType.isEmpty()) {
                response.append("Unfortunately, we don't have any rooms available for your selected dates. ");
                response.append("Would you like to try different dates?");
            } else {
                response.append("The following room types are available:\n\n");

                for (Map.Entry<String, List<Room>> entry : roomsByType.entrySet()) {
                    Room sampleRoom = entry.getValue().get(0);
                    BigDecimal pricePerNight = sampleRoom.getRoomPrice();
                    BigDecimal totalPrice = pricePerNight.multiply(new BigDecimal(checkIn.until(checkOut).getDays()));

                    response.append("## **").append(entry.getKey()).append("**\n");
                    response.append("* **Description:** ").append(sampleRoom.getRoomDescription()).append("\n");
                    response.append("* **Price:** $").append(pricePerNight).append(" per night\n");
                    response.append("* **Total for ").append(checkIn.until(checkOut).getDays()).append(" nights:** $")
                           .append(totalPrice).append("\n");
                    response.append("* **Available rooms:** ").append(entry.getValue().size()).append("\n\n");
                }

                response.append("Would you like to proceed with a reservation? If so, please let me know which room type you prefer.");
            }

            return response.toString();
        } catch (Exception e) {
            logger.error("Error getting available rooms: ", e);
            return "Sorry, I couldn't check room availability at the moment.";
        }
    }

    /**
     * Get booking information by confirmation code
     * @param confirmationCode Booking confirmation code
     * @return Formatted string with booking details
     */
    public String getBookingInfo(String confirmationCode) {
        try {
            Optional<Booking> bookingOpt = bookingRepository.findByBookingConfirmationCode(confirmationCode);

            if (bookingOpt.isEmpty()) {
                StringBuilder response = new StringBuilder();
                response.append("# Booking Not Found\n\n");
                response.append("I could not find any booking with confirmation code: **").append(confirmationCode).append("**\n\n");
                response.append("This could be due to one of the following reasons:\n\n");
                response.append("1. The confirmation code may be incorrect\n");
                response.append("2. The booking may have been cancelled\n");
                response.append("3. The booking may not exist in our system\n\n");
                response.append("Please check your confirmation code and try again, or contact our customer support for assistance.");
                return response.toString();
            }

            Booking booking = bookingOpt.get();
            Room room = booking.getRoom();
            User user = booking.getUser();

            StringBuilder response = new StringBuilder("# Booking Information\n\n");
            response.append("## Reservation Details\n\n");
            response.append("**Confirmation Code:** ").append(booking.getBookingConfirmationCode()).append("\n");
            response.append("**Guest:** ").append(user.getName()).append("\n");
            response.append("**Email:** ").append(user.getEmail()).append("\n\n");

            response.append("## Stay Information\n\n");
            response.append("**Room Type:** ").append(room.getRoomType()).append("\n");
            response.append("**Check-in Date:** ").append(booking.getCheckInDate().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))).append("\n");
            response.append("**Check-out Date:** ").append(booking.getCheckOutDate().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy"))).append("\n");
            response.append("**Length of Stay:** ").append(booking.getCheckInDate().until(booking.getCheckOutDate()).getDays()).append(" nights\n\n");

            response.append("## Guest Information\n\n");
            response.append("**Number of Adults:** ").append(booking.getNumOfAdults()).append("\n");
            response.append("**Number of Children:** ").append(booking.getNumOfChildren()).append("\n");
            response.append("**Total Guests:** ").append(booking.getTotalNumberOfGuests()).append("\n\n");

            response.append("Thank you for choosing our hotel. We look forward to welcoming you!");

            return response.toString();
        } catch (Exception e) {
            logger.error("Error getting booking info: ", e);
            return "Sorry, I couldn't retrieve the booking information at the moment.";
        }
    }

    /**
     * Get user bookings by user ID or email
     * @param userIdentifier User ID or email
     * @return Formatted string with user's bookings
     */
    public String getUserBookings(String userIdentifier) {
        try {
            User user = null;

            // Try to find user by ID
            try {
                Long userId = Long.parseLong(userIdentifier);
                Optional<User> userOpt = userRepository.findById(userId);
                if (userOpt.isPresent()) {
                    user = userOpt.get();
                }
            } catch (NumberFormatException e) {
                // Not a numeric ID, try by email
                user = userRepository.findByEmail(userIdentifier).orElse(null);
            }

            if (user == null) {
                return "No user found with the provided identifier.";
            }

            List<Booking> bookings = bookingRepository.findByUserId(user.getId());

            if (bookings.isEmpty()) {
                return "No bookings found for user: " + user.getName();
            }

            StringBuilder response = new StringBuilder("Bookings for " + user.getName() + ":\n\n");

            for (Booking booking : bookings) {
                Room room = booking.getRoom();
                response.append("Confirmation Code: ").append(booking.getBookingConfirmationCode()).append("\n");
                response.append("Room Type: ").append(room.getRoomType()).append("\n");
                response.append("Check-in: ").append(booking.getCheckInDate()).append("\n");
                response.append("Check-out: ").append(booking.getCheckOutDate()).append("\n");
                response.append("Total Guests: ").append(booking.getTotalNumberOfGuests()).append("\n\n");
            }

            return response.toString();
        } catch (Exception e) {
            logger.error("Error getting user bookings: ", e);
            return "Sorry, I couldn't retrieve the user's bookings at the moment.";
        }
    }

    /**
     * Get room details by room type or ID
     * @param roomIdentifier Room type or ID
     * @return Formatted string with room details
     */
    public String getRoomDetails(String roomIdentifier) {
        try {
            List<Room> rooms;

            // Try to find room by ID
            try {
                Long roomId = Long.parseLong(roomIdentifier);
                Optional<Room> roomOpt = roomRepository.findById(roomId);
                if (roomOpt.isPresent()) {
                    rooms = List.of(roomOpt.get());
                } else {
                    return "No room found with ID: " + roomId;
                }
            } catch (NumberFormatException e) {
                // Not a numeric ID, try by room type
                rooms = roomRepository.findAll().stream()
                    .filter(room -> room.getRoomType().equalsIgnoreCase(roomIdentifier))
                    .collect(Collectors.toList());

                if (rooms.isEmpty()) {
                    return "No rooms found with type: " + roomIdentifier;
                }
            }

            StringBuilder response = new StringBuilder("Room Details:\n\n");

            for (Room room : rooms) {
                response.append("Room ID: ").append(room.getId()).append("\n");
                response.append("Type: ").append(room.getRoomType()).append("\n");
                response.append("Price: $").append(room.getRoomPrice()).append(" per night\n");
                response.append("Description: ").append(room.getRoomDescription()).append("\n\n");
            }

            return response.toString();
        } catch (Exception e) {
            logger.error("Error getting room details: ", e);
            return "Sorry, I couldn't retrieve the room details at the moment.";
        }
    }

    /**
     * Helper method to parse date string
     * @param dateStr Date string in various formats
     * @return LocalDate object or null if parsing fails
     */
    private LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            logger.warn("Null or empty date string provided");
            return null;
        }

        // Clean up the date string
        dateStr = dateStr.trim();
        logger.debug("Parsing date: {}", dateStr);

        try {
            // Try ISO format first (YYYY-MM-DD)
            try {
                return LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
            } catch (DateTimeParseException e) {
                // Continue with other formats
            }

            // Try other common formats
            DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("MM-dd-yyyy")
            };

            for (DateTimeFormatter formatter : formatters) {
                try {
                    return LocalDate.parse(dateStr, formatter);
                } catch (DateTimeParseException e) {
                    // Try next formatter
                }
            }

            // Handle two-digit year formats
            if (dateStr.matches("\\d{1,2}[/-]\\d{1,2}[/-]\\d{2}")) {
                String[] parts;
                if (dateStr.contains("/")) {
                    parts = dateStr.split("/");
                } else {
                    parts = dateStr.split("-");
                }

                if (parts.length == 3) {
                    try {
                        // Assume MM/DD/YY format
                        int month = Integer.parseInt(parts[0]);
                        int day = Integer.parseInt(parts[1]);
                        int year = 2000 + Integer.parseInt(parts[2]); // Assume 21st century

                        return LocalDate.of(year, month, day);
                    } catch (Exception ex) {
                        logger.debug("Failed to parse two-digit year: {}", ex.getMessage());
                    }
                }
            }

            logger.warn("Could not parse date: {}", dateStr);
            return null;
        } catch (Exception e) {
            logger.error("Error parsing date: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Helper method to check if a room is available for a date range
     * @param room Room to check
     * @param checkIn Check-in date
     * @param checkOut Check-out date
     * @return true if room is available, false otherwise
     */
    private boolean isRoomAvailable(Room room, LocalDate checkIn, LocalDate checkOut) {
        List<Booking> bookings = room.getBookings();
        if (bookings == null || bookings.isEmpty()) {
            return true;
        }

        for (Booking booking : bookings) {
            // Check if there's an overlap with existing booking
            if (!(booking.getCheckOutDate().isBefore(checkIn) || booking.getCheckInDate().isAfter(checkOut))) {
                return false;
            }
        }

        return true;
    }
}
