package example.BookingBE.Service.Payment;

import example.BookingBE.Request.PaymentRequest;
import example.BookingBE.Response.ResponseAPI;

public interface PaymentService {
    ResponseAPI createPaymentIntent(PaymentRequest paymentRequest);
    ResponseAPI confirmPayment(String paymentIntentId);
    ResponseAPI getPaymentByBookingId(Long bookingId);
    ResponseAPI getPaymentByPaymentIntentId(String paymentIntentId);
    ResponseAPI getAllPayments();
}
