package example.BookingBE.Service.Payment;

import example.BookingBE.Request.PaymentRequest;
import example.BookingBE.Response.PaymentResponse;
import example.BookingBE.Response.ResponseAPI;

public interface PaymentService {
    PaymentResponse createPaymentLink(PaymentRequest paymentRequest);
    ResponseAPI confirmPayment(String sessionId);
} 