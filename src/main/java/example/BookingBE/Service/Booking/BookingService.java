package example.BookingBE.Service.Booking;

import example.BookingBE.Entity.Booking;
import example.BookingBE.Response.ResponseAPI;

public interface BookingService {

    ResponseAPI saveBooking(Long roomId, Long userId, Booking bookingRequest);

    ResponseAPI findBookingByConfirmationCode(String confirmationCode);

    ResponseAPI getAllBookings();

    ResponseAPI cancelBooking(Long bookingId);


}
