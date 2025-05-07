package example.BookingBE.Service.Payment;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import example.BookingBE.Entity.Booking;
import example.BookingBE.Entity.Payment;
import example.BookingBE.Repository.BookingRepository;
import example.BookingBE.Repository.PaymentRepository;
import example.BookingBE.Request.PaymentRequest;
import example.BookingBE.Response.PaymentResponse;
import example.BookingBE.Response.ResponseAPI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.success.url}")
    private String successUrl;

    @Value("${stripe.cancel.url}")
    private String cancelUrl;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    public PaymentResponse createPaymentLink(PaymentRequest paymentRequest) {
        try {
            Stripe.apiKey = stripeApiKey;

            Optional<Booking> bookingOpt = bookingRepository.findById(paymentRequest.getBookingId());
            if (bookingOpt.isEmpty()) {
                return createErrorResponse("Booking not found");
            }

            Booking booking = bookingOpt.get();
            Payment payment = new Payment();
            payment.setBooking(booking);
            payment.setAmount(booking.getTotalPrice());
            payment.setCurrency("USD");
            payment.setStatus("PENDING");

            SessionCreateParams params = SessionCreateParams.builder()
                    .setMode(SessionCreateParams.Mode.PAYMENT)
                    .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                    .setCancelUrl(cancelUrl)
                    .addLineItem(SessionCreateParams.LineItem.builder()
                            .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                                    .setCurrency("usd")
                                    .setUnitAmount(booking.getTotalPrice().multiply(BigDecimal.valueOf(100)).longValue())
                                    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                            .setName("Room Booking")
                                            .setDescription("Booking for room " + booking.getRoom().getId())
                                            .build())
                                    .build())
                            .setQuantity(1L)
                            .build())
                    .build();

            Session session = Session.create(params);
            payment.setSessionId(session.getId());
            paymentRepository.save(payment);

            PaymentResponse response = new PaymentResponse();
            response.setSuccess(true);
            response.setMessage("Payment link created successfully");
            response.setPaymentUrl(session.getUrl());
            response.setSessionId(session.getId());
            response.setBookingConfirmationCode(booking.getBookingConfirmationCode());
            return response;

        } catch (StripeException e) {
            log.error("Error creating payment link: ", e);
            return createErrorResponse("Error creating payment link: " + e.getMessage());
        }
    }

    @Override
    public ResponseAPI confirmPayment(String sessionId) {
        try {
            Stripe.apiKey = stripeApiKey;
            Session session = Session.retrieve(sessionId);
            
            Optional<Payment> paymentOpt = paymentRepository.findBySessionId(sessionId);
            if (paymentOpt.isEmpty()) {
                return new ResponseAPI(HttpStatus.NOT_FOUND.value(), "Payment not found");
            }

            Payment payment = paymentOpt.get();
            payment.setStatus(session.getPaymentStatus());
            payment.setPaymentMethod(session.getPaymentMethodTypes().get(0));
            payment.setReceiptUrl(session.getPaymentIntent() != null ? session.getPaymentIntent().toString() : null);
            paymentRepository.save(payment);

            Booking booking = payment.getBooking();
            booking.setPaymentStatus(session.getPaymentStatus().toUpperCase());
            bookingRepository.save(booking);

            Map<String, Object> data = new HashMap<>();
            data.put("bookingId", booking.getId());
            data.put("bookingConfirmationCode", booking.getBookingConfirmationCode());
            data.put("message", "Payment confirmed successfully");
            
            return new ResponseAPI(HttpStatus.OK.value(), "Payment confirmed successfully", data);
        } catch (StripeException e) {
            log.error("Error confirming payment: ", e);
            return new ResponseAPI(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error confirming payment: " + e.getMessage());
        }
    }

    private PaymentResponse createErrorResponse(String message) {
        PaymentResponse response = new PaymentResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
} 