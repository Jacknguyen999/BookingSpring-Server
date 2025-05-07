package example.BookingBE.Request;

import lombok.Data;

@Data
public class PaymentRequest {
    private Long bookingId;
    private String paymentMethodId;
    private String returnUrl;
}
