package example.BookingBE.Controller;

import example.BookingBE.Entity.Booking;
import example.BookingBE.Request.PaymentRequest;
import example.BookingBE.Response.PaymentResponse;
import example.BookingBE.Response.ResponseAPI;
import example.BookingBE.Service.Booking.BookingService;
import example.BookingBE.Service.Payment.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
@Tag(name = "Booking", description = "API for Booking ")
public class BookingController {

    @Autowired
    private BookingService bookingService;
    
    @Autowired
    private PaymentService paymentService;

    @PostMapping("/book-room/{roomId}/{userId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<PaymentResponse> saveBooking(
            @PathVariable Long roomId,
            @PathVariable Long userId,
            @RequestBody Booking bookingRequest
    ) {
        ResponseAPI bookingResponse = bookingService.saveBooking(roomId, userId, bookingRequest);
        
        if (bookingResponse.getStatusCode() != 200) {
            PaymentResponse errorResponse = new PaymentResponse();
            errorResponse.setSuccess(false);
            errorResponse.setMessage(bookingResponse.getMessage());
            return ResponseEntity.status(bookingResponse.getStatusCode()).body(errorResponse);
        }
        
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setBookingId(bookingResponse.getBooking().getId());
        
        PaymentResponse paymentResponse = paymentService.createPaymentLink(paymentRequest);
        return ResponseEntity.ok(paymentResponse);
    }

    @GetMapping("/all-booking")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResponseAPI> getAllBooking() {
        ResponseAPI responseAPI = bookingService.getAllBookings();

        return ResponseEntity.status(responseAPI.getStatusCode()).body(responseAPI);
    }

    @GetMapping("/get-by-confirmation-code/{confirmationCode}")
    public ResponseEntity<ResponseAPI> getBookingByConfirmationCode(
            @PathVariable String confirmationCode
    ) {
        ResponseAPI responseAPI = bookingService.findBookingByConfirmationCode(confirmationCode);

        return ResponseEntity.ok(responseAPI);
    }

    @DeleteMapping("/cancel/{bookingId}")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<ResponseAPI> cancelBooking(
            @PathVariable Long bookingId
    ) {
        ResponseAPI responseAPI = bookingService.cancelBooking(bookingId);

        return ResponseEntity.status(responseAPI.getStatusCode()).body(responseAPI);
    }
}
