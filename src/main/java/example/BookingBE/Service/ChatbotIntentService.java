package example.BookingBE.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service to detect user intents from chatbot messages and route to appropriate booking system functions
 */
@Service
public class ChatbotIntentService {
    private static final Logger logger = LoggerFactory.getLogger(ChatbotIntentService.class);

    @Autowired
    private BookingIntegrationService bookingIntegrationService;

    // Intent detection patterns
    private static final Pattern ROOM_TYPES_PATTERN = Pattern.compile(
            "(?i).*?(what|list|show|tell|get|display|available).*?(room|rooms).*?(type|types|category|categories|kind|kinds|offer|available).*");

    // Simplified room types pattern to catch more variations
    private static final Pattern SIMPLE_ROOM_TYPES_PATTERN = Pattern.compile(
            "(?i).*(room|rooms).*(type|types|available|offer).*");

    // Direct room types query pattern
    private static final Pattern DIRECT_ROOM_TYPES_PATTERN = Pattern.compile(
            "(?i).*show.*room.*available.*");

    private static final Pattern ROOM_AVAILABILITY_PATTERN = Pattern.compile(
            "(?i).*?(available|availability|check|find|search|book|reserve|get|from).*?" +
            "(room|rooms)?.*?" +
            "(from|between|on|for|date|dates|" +
            "\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}|" +
            "\\d{4}[/-]\\d{1,2}[/-]\\d{1,2}).*");

    // Simple date range pattern to catch queries like "from X to Y"
    private static final Pattern DATE_RANGE_PATTERN = Pattern.compile(
            "(?i).*?from\\s+([\\d/.-]+)\\s+to\\s+([\\d/.-]+).*");

    private static final Pattern BOOKING_INFO_PATTERN = Pattern.compile(
            "(?i).*(booking|reservation).*" +
            "(info|information|details|status).*" +
            "(code|confirmation|id|number|#)\\s*[:\\s]?\\s*([a-zA-Z0-9]{5,}).*");

    private static final Pattern USER_BOOKINGS_PATTERN = Pattern.compile(
            "(?i).*(my|user|customer)\\s*(booking|bookings|reservation|reservations).*" +
            "(id|email|user)?\\s*[:\\s]?\\s*([a-zA-Z0-9@.]{3,})?.*");

    private static final Pattern ROOM_DETAILS_PATTERN = Pattern.compile(
            "(?i).*(info|information|details|tell).*" +
            "(about|on)\\s*(room|rooms)\\s*(type|id)?\\s*[:\\s]?\\s*([a-zA-Z0-9\\s]{2,})?.*");

    private static final Pattern DATE_PATTERN = Pattern.compile(
            "(\\d{4}[/-]\\d{1,2}[/-]\\d{1,2}|\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4}|\\d{1,2}[/-]\\d{1,2})");

    // Simplified date pattern for common formats
    private static final Pattern SIMPLE_DATE_PATTERN = Pattern.compile("\\d+[/-]\\d+[/-]?\\d*");

    /**
     * Process a message to detect intent and route to appropriate booking system function
     * @param message User message
     * @return Response from the booking system or null if no intent detected
     */
    public String processIntent(String message) {
        logger.debug("Processing intent for message: {}", message);

        // Log the exact message for debugging
        logger.info("Processing exact message: '{}'", message);

        // Special case for the exact query "Show me the types of rooms available"
        if (message.equalsIgnoreCase("Show me the types of rooms available")) {
            logger.info("Detected exact match for room types query");
            return bookingIntegrationService.getAvailableRoomTypes();
        }

        // Check for direct room types pattern
        boolean directRoomTypesMatch = DIRECT_ROOM_TYPES_PATTERN.matcher(message).matches();
        logger.debug("Direct room types pattern match: {}", directRoomTypesMatch);

        if (directRoomTypesMatch) {
            logger.info("Detected direct room types intent");
            return bookingIntegrationService.getAvailableRoomTypes();
        }

        // Check for room types intent using primary pattern
        boolean roomTypesMatch = ROOM_TYPES_PATTERN.matcher(message).matches();
        logger.debug("Primary room types pattern match: {}", roomTypesMatch);

        // If primary pattern doesn't match, try simplified pattern
        boolean simpleRoomTypesMatch = false;
        if (!roomTypesMatch) {
            simpleRoomTypesMatch = SIMPLE_ROOM_TYPES_PATTERN.matcher(message).matches();
            logger.debug("Simplified room types pattern match: {}", simpleRoomTypesMatch);
        }

        if (roomTypesMatch || simpleRoomTypesMatch) {
            logger.info("Detected room types intent");
            return bookingIntegrationService.getAvailableRoomTypes();
        }

        // First, check for simple date range pattern (from X to Y)
        Matcher dateRangeMatcher = DATE_RANGE_PATTERN.matcher(message);
        boolean dateRangeMatch = dateRangeMatcher.matches();
        logger.debug("Date range pattern match: {}", dateRangeMatch);

        if (dateRangeMatch) {
            logger.info("Detected date range pattern");
            String checkInDate = standardizeDate(dateRangeMatcher.group(1));
            String checkOutDate = standardizeDate(dateRangeMatcher.group(2));
            logger.debug("Extracted dates from range pattern: {} to {}", checkInDate, checkOutDate);

            if (checkInDate != null && checkOutDate != null) {
                return bookingIntegrationService.getAvailableRooms(checkInDate, checkOutDate, null);
            }
        }

        // Check for room availability intent
        Matcher availabilityMatcher = ROOM_AVAILABILITY_PATTERN.matcher(message);
        boolean availabilityMatch = availabilityMatcher.matches();
        logger.debug("Room availability pattern match: {}", availabilityMatch);

        if (availabilityMatch) {
            logger.info("Detected room availability intent");

            // Extract dates from the message
            Matcher dateMatcher = DATE_PATTERN.matcher(message);
            String checkInDate = null;
            String checkOutDate = null;

            // Log all date matches for debugging
            logger.debug("Looking for dates in message: {}", message);

            // Find all dates in the message
            int dateCount = 0;
            while (dateMatcher.find()) {
                String date = dateMatcher.group(1);
                logger.debug("Found date: {}", date);
                dateCount++;

                if (checkInDate == null) {
                    checkInDate = standardizeDate(date);
                    logger.debug("Set check-in date: {}", checkInDate);
                } else if (checkOutDate == null) {
                    checkOutDate = standardizeDate(date);
                    logger.debug("Set check-out date: {}", checkOutDate);
                }
            }

            logger.debug("Found {} dates in message", dateCount);

            // If we couldn't find proper dates, try a simpler approach
            if ((checkInDate == null || checkOutDate == null) && message.contains("from") && message.contains("to")) {
                logger.debug("Trying alternative date extraction with 'from' and 'to'");
                String[] parts = message.split("from");
                if (parts.length > 1) {
                    String afterFrom = parts[1];
                    String[] toParts = afterFrom.split("to");
                    if (toParts.length > 1) {
                        // Extract potential dates
                        Matcher fromMatcher = SIMPLE_DATE_PATTERN.matcher(toParts[0]);
                        Matcher toMatcher = SIMPLE_DATE_PATTERN.matcher(toParts[1]);

                        if (fromMatcher.find()) {
                            checkInDate = standardizeDate(fromMatcher.group(0));
                            logger.debug("Alternative check-in date: {}", checkInDate);
                        }

                        if (toMatcher.find()) {
                            checkOutDate = standardizeDate(toMatcher.group(0));
                            logger.debug("Alternative check-out date: {}", checkOutDate);
                        }
                    }
                }
            }

            // Extract room type if present
            String roomType = null;
            String messageLower = message.toLowerCase();

            // Log room type detection attempt
            logger.debug("Checking for room type in message: {}", messageLower);

            if (messageLower.contains("deluxe") || messageLower.contains("luxury") || messageLower.contains("premium")) {
                roomType = "Deluxe";
                logger.debug("Detected room type: Deluxe");
            } else if (messageLower.contains("standard") || messageLower.contains("regular") || messageLower.contains("normal")) {
                roomType = "Standard";
                logger.debug("Detected room type: Standard");
            } else if (messageLower.contains("suite") || messageLower.contains("executive") || messageLower.contains("vip")) {
                roomType = "Suite";
                logger.debug("Detected room type: Suite");
            } else {
                logger.debug("No specific room type detected in message");
            }

            // If dates are missing, return a prompt for dates
            if (checkInDate == null || checkOutDate == null) {
                return "To check room availability, please provide both check-in and check-out dates in YYYY-MM-DD format.";
            }

            return bookingIntegrationService.getAvailableRooms(checkInDate, checkOutDate, roomType);
        }

        // Check for booking info intent
        Matcher bookingInfoMatcher = BOOKING_INFO_PATTERN.matcher(message);
        if (bookingInfoMatcher.matches()) {
            logger.info("Detected booking info intent");
            String confirmationCode = bookingInfoMatcher.group(4);
            if (confirmationCode != null && !confirmationCode.isEmpty()) {
                return bookingIntegrationService.getBookingInfo(confirmationCode);
            } else {
                return "Please provide a valid booking confirmation code.";
            }
        }

        // Check for user bookings intent
        Matcher userBookingsMatcher = USER_BOOKINGS_PATTERN.matcher(message);
        if (userBookingsMatcher.matches()) {
            logger.info("Detected user bookings intent");
            String userIdentifier = userBookingsMatcher.group(4);
            if (userIdentifier != null && !userIdentifier.isEmpty()) {
                return bookingIntegrationService.getUserBookings(userIdentifier);
            } else {
                return "Please provide a user ID or email to check bookings.";
            }
        }

        // Check for room details intent
        Matcher roomDetailsMatcher = ROOM_DETAILS_PATTERN.matcher(message);
        if (roomDetailsMatcher.matches()) {
            logger.info("Detected room details intent");
            String roomIdentifier = roomDetailsMatcher.group(5);
            if (roomIdentifier != null && !roomIdentifier.isEmpty()) {
                return bookingIntegrationService.getRoomDetails(roomIdentifier);
            } else {
                return "Please specify which room type or ID you'd like information about.";
            }
        }

        // No intent detected
        logger.debug("No booking-related intent detected");
        return null;
    }

    /**
     * Helper method to parse date string
     * @param dateStr Date string in various formats
     * @return Standardized date string in YYYY-MM-DD format
     */
    private String standardizeDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }

        // Clean up the date string
        dateStr = dateStr.trim();
        logger.debug("Standardizing date: {}", dateStr);

        try {
            // Handle different date formats
            DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ofPattern("yyyy/MM/dd"),
                DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                DateTimeFormatter.ofPattern("MM-dd-yyyy")
            };

            for (DateTimeFormatter formatter : formatters) {
                try {
                    LocalDate date = LocalDate.parse(dateStr, formatter);
                    String result = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
                    logger.debug("Successfully parsed date {} to {}", dateStr, result);
                    return result;
                } catch (DateTimeParseException e) {
                    // Try next formatter
                }
            }

            // Handle short date formats like "23/5" (day/month)
            if (dateStr.matches("\\d{1,2}[/-]\\d{1,2}")) {
                logger.debug("Detected short date format: {}", dateStr);
                String[] parts;
                if (dateStr.contains("/")) {
                    parts = dateStr.split("/");
                } else {
                    parts = dateStr.split("-");
                }

                if (parts.length == 2) {
                    try {
                        // Assume DD/MM format and current year
                        int day = Integer.parseInt(parts[0]);
                        int month = Integer.parseInt(parts[1]);
                        int year = LocalDate.now().getYear();

                        // If the resulting date is in the past, use next year
                        LocalDate date = LocalDate.of(year, month, day);
                        if (date.isBefore(LocalDate.now())) {
                            date = LocalDate.of(year + 1, month, day);
                        }

                        String result = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
                        logger.debug("Parsed short date {} to {}", dateStr, result);
                        return result;
                    } catch (Exception ex) {
                        logger.debug("Failed to parse short date: {}", ex.getMessage());
                    }
                }
            }

            // Handle two-digit year formats
            if (dateStr.matches("\\d{1,2}[/-]\\d{1,2}[/-]\\d{2}")) {
                // Add 20 to make it 20xx for recent years
                String[] parts;
                if (dateStr.contains("/")) {
                    parts = dateStr.split("/");
                } else {
                    parts = dateStr.split("-");
                }

                if (parts.length == 3) {
                    // Assume DD/MM/YY format
                    String day = parts[0];
                    String month = parts[1];
                    String year = "20" + parts[2]; // Assume 21st century

                    try {
                        LocalDate date = LocalDate.of(
                            Integer.parseInt(year),
                            Integer.parseInt(month),
                            Integer.parseInt(day)
                        );
                        String result = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
                        logger.debug("Parsed two-digit year date {} to {}", dateStr, result);
                        return result;
                    } catch (Exception ex) {
                        logger.debug("Failed to parse two-digit year: {}", ex.getMessage());

                        // Try MM/DD/YY format as fallback
                        try {
                            LocalDate date = LocalDate.of(
                                Integer.parseInt(year),
                                Integer.parseInt(day),
                                Integer.parseInt(month)
                            );
                            String result = date.format(DateTimeFormatter.ISO_LOCAL_DATE);
                            logger.debug("Parsed two-digit year date (alternative format) {} to {}", dateStr, result);
                            return result;
                        } catch (Exception e) {
                            logger.debug("Failed to parse two-digit year (alternative format): {}", e.getMessage());
                        }
                    }
                }
            }

            // If we get here, we couldn't parse the date
            logger.warn("Could not parse date: {}", dateStr);
            return null;
        } catch (Exception e) {
            logger.error("Error standardizing date: ", e);
            return null;
        }
    }
}
