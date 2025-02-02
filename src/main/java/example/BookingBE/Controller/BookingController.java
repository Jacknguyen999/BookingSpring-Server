package example.BookingBE.Controller;


import example.BookingBE.Entity.Booking;
import example.BookingBE.Response.ResponseAPI;
import example.BookingBE.Service.Booking.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @PostMapping("/book-room/{roomId}/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<ResponseAPI> saveBooking(
            @PathVariable Long roomId,
            @PathVariable Long userId,
            @RequestBody Booking bookingRequest
            ){

        ResponseAPI responseAPI = bookingService.saveBooking(roomId,userId,bookingRequest);
        return ResponseEntity.status(responseAPI.getStatusCode()).body(responseAPI);
    }

    @GetMapping("/all-booking")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResponseAPI> getAllBooking(){
        ResponseAPI responseAPI = bookingService.getAllBookings();

        return ResponseEntity.status(responseAPI.getStatusCode()).body(responseAPI);
    }

    @GetMapping("/get-by-confirmation-code/{confirmationCode}")
    public ResponseEntity<ResponseAPI> getBookingbyConfirmationCode (
            @PathVariable String confirmationCode
    ){
        ResponseAPI responseAPI = bookingService.findBookingByConfirmationCode(confirmationCode);

        return ResponseEntity.ok(responseAPI);
    }

    @DeleteMapping("/cancel/{bookingId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<ResponseAPI> cancelBooking(
            @PathVariable Long bookingId
    ){
        ResponseAPI responseAPI = bookingService.cancelBooking(bookingId);

        return ResponseEntity.status(responseAPI.getStatusCode()).body(responseAPI);
    }





}
