package example.BookingBE.Controller;

import example.BookingBE.Response.ResponseAPI;
import example.BookingBE.Service.Payment.PaymentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
@Tag(name = "Payment", description = "API for Payment")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    @PostMapping("/confirm/{sessionId}")
    public ResponseEntity<ResponseAPI> confirmPayment(@PathVariable String sessionId) {
        ResponseAPI response = paymentService.confirmPayment(sessionId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
} 