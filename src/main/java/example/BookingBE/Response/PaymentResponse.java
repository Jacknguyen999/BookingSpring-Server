package example.BookingBE.Response;

import lombok.Data;

@Data
public class PaymentResponse {
    private boolean success;
    private String message;
    private String paymentUrl;
    private String sessionId;
    private String bookingConfirmationCode;
} 