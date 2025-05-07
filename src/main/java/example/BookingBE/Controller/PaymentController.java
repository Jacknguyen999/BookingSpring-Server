package example.BookingBE.Controller;

import example.BookingBE.Request.PaymentRequest;
import example.BookingBE.Response.ResponseAPI;
import example.BookingBE.Service.Payment.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payment", description = "API for payment processing")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create-payment-intent")
    @Operation(summary = "Create a payment intent", description = "Creates a Stripe payment intent for a booking")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<ResponseAPI> createPaymentIntent(@RequestBody PaymentRequest paymentRequest) {
        ResponseAPI response = paymentService.createPaymentIntent(paymentRequest);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/confirm/{paymentIntentId}")
    @Operation(summary = "Confirm a payment", description = "Confirms a payment using the payment intent ID")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<ResponseAPI> confirmPayment(@PathVariable String paymentIntentId) {
        ResponseAPI response = paymentService.confirmPayment(paymentIntentId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/booking/{bookingId}")
    @Operation(summary = "Get payment by booking ID", description = "Retrieves payment information for a specific booking")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<ResponseAPI> getPaymentByBookingId(@PathVariable Long bookingId) {
        ResponseAPI response = paymentService.getPaymentByBookingId(bookingId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/payment-intent/{paymentIntentId}")
    @Operation(summary = "Get payment by payment intent ID", description = "Retrieves payment information for a specific payment intent")
    @PreAuthorize("hasAuthority('ADMIN') or hasAuthority('USER')")
    public ResponseEntity<ResponseAPI> getPaymentByPaymentIntentId(@PathVariable String paymentIntentId) {
        ResponseAPI response = paymentService.getPaymentByPaymentIntentId(paymentIntentId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/all")
    @Operation(summary = "Get all payments", description = "Retrieves all payment information")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResponseAPI> getAllPayments() {
        ResponseAPI response = paymentService.getAllPayments();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
