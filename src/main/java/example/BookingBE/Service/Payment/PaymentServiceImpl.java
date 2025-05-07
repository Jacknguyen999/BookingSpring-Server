package example.BookingBE.Service.Payment;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import example.BookingBE.DTO.BookingDTO;
import example.BookingBE.DTO.PaymentDTO;
import example.BookingBE.Entity.Booking;
import example.BookingBE.Entity.Payment;
import example.BookingBE.Entity.Room;
import example.BookingBE.Exception.GlobalException;
import example.BookingBE.Repository.BookingRepository;
import example.BookingBE.Repository.PaymentRepository;
import example.BookingBE.Request.PaymentRequest;
import example.BookingBE.Response.ResponseAPI;
import example.BookingBE.Utils.Utils;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentServiceImpl.class);

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @Value("${stripe.currency}")
    private String currency;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Override
    public ResponseAPI createPaymentIntent(PaymentRequest paymentRequest) {
        ResponseAPI response = new ResponseAPI();

        try {
            // Find the booking
            Booking booking = bookingRepository.findById(paymentRequest.getBookingId())
                    .orElseThrow(() -> new GlobalException("Booking not found"));

            Optional<Payment> existingPayment = paymentRepository.findByBookingId(booking.getId());
            if (existingPayment.isPresent()) {
                String status = existingPayment.get().getStatus();
                if ("succeeded".equals(status) || "processing".equals(status)) {
                    response.setStatusCode(400);
                    response.setMessage("Payment already processed for this booking");
                    return response;
                } else {
                    // If payment exists but is not succeeded or processing, delete it to create a new one
                    paymentRepository.delete(existingPayment.get());
                }
            }

            // Calculate amount based on room price and stay duration
            Room room = booking.getRoom();
            BigDecimal pricePerNight = room.getRoomPrice();
            long nights = ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
            BigDecimal totalAmount = pricePerNight.multiply(BigDecimal.valueOf(nights));

            // Convert to cents for Stripe
            long amountInCents = totalAmount.multiply(new BigDecimal("100")).longValue();

            // Create payment intent
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setCurrency(currency)
                    .setAmount(amountInCents)
                    .setDescription("Booking #" + booking.getBookingConfirmationCode())
                    .setReceiptEmail(booking.getUser().getEmail())
                    .putMetadata("booking_id", booking.getId().toString())
                    .putMetadata("confirmation_code", booking.getBookingConfirmationCode())
                    .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                            .setEnabled(true)
                            .build()
                    )
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            // Save payment information
            Payment payment = new Payment();
            payment.setPaymentIntentId(paymentIntent.getId());
            payment.setAmount(totalAmount);
            payment.setCurrency(currency);
            payment.setStatus(paymentIntent.getStatus());
            payment.setBooking(booking);

            // Save payment first
            Payment savedPayment = paymentRepository.save(payment);

            // Update booking payment status
            booking.setPayment(savedPayment);
            booking.setPaymentStatus("PENDING");
            bookingRepository.save(booking);

            // Prepare response
            response.setStatusCode(200);
            response.setMessage("Payment intent created successfully");
            response.setClientSecret(paymentIntent.getClientSecret());
            response.setPaymentIntentId(paymentIntent.getId());

            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setPaymentIntentId(paymentIntent.getId());
            paymentDTO.setAmount(totalAmount);
            paymentDTO.setCurrency(currency);
            paymentDTO.setStatus(paymentIntent.getStatus());
            paymentDTO.setClientSecret(paymentIntent.getClientSecret());

            BookingDTO bookingDTO = Utils.mapBookingEntityToBookingDTOPlusBookedRoom(booking, false);
            paymentDTO.setBooking(bookingDTO);

            response.setPayment(paymentDTO);

        } catch (GlobalException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (StripeException e) {
            logger.error("Stripe error: {}", e.getMessage());
            response.setStatusCode(500);
            response.setMessage("Error processing payment: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error creating payment intent: {}", e.getMessage());
            response.setStatusCode(500);
            response.setMessage("Error creating payment intent: " + e.getMessage());
        }

        return response;
    }

    @Override
    public ResponseAPI confirmPayment(String paymentIntentId) {
        ResponseAPI response = new ResponseAPI();

        try {
            // Retrieve the payment intent from Stripe
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            // Find the payment in our database
            Payment payment = paymentRepository.findByPaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new GlobalException("Payment not found"));

            // Update payment status
            payment.setStatus(paymentIntent.getStatus());

            if ("succeeded".equals(paymentIntent.getStatus())) {
                // Get payment method if available
                String paymentMethod = paymentIntent.getPaymentMethod();
                if (paymentMethod != null) {
                    payment.setPaymentMethod(paymentMethod);
                }

                // Retrieve charges associated with this payment intent
                try {
                    com.stripe.model.ChargeCollection charges =
                        com.stripe.model.Charge.list(Map.of("payment_intent", paymentIntentId));

                    if (charges != null && charges.getData() != null && !charges.getData().isEmpty()) {
                        payment.setReceiptUrl(charges.getData().get(0).getReceiptUrl());
                    }
                } catch (Exception e) {
                    logger.warn("Could not retrieve receipt URL: {}", e.getMessage());
                    // Continue without setting receipt URL
                }

                // Update booking payment status
                Booking booking = payment.getBooking();
                if (booking != null) {
                    booking.setPaymentStatus("PAID");
                    bookingRepository.save(booking);
                }
            } else if ("canceled".equals(paymentIntent.getStatus())) {
                // Update booking payment status
                Booking booking = payment.getBooking();
                if (booking != null) {
                    booking.setPaymentStatus("FAILED");
                    bookingRepository.save(booking);
                }
            }

            paymentRepository.save(payment);

            // Prepare response
            response.setStatusCode(200);
            response.setMessage("Payment status updated successfully");

            PaymentDTO paymentDTO = new PaymentDTO();
            paymentDTO.setPaymentIntentId(payment.getPaymentIntentId());
            paymentDTO.setAmount(payment.getAmount());
            paymentDTO.setCurrency(payment.getCurrency());
            paymentDTO.setStatus(payment.getStatus());
            paymentDTO.setPaymentMethod(payment.getPaymentMethod());
            paymentDTO.setReceiptUrl(payment.getReceiptUrl());

            // Only map booking if it exists
            if (payment.getBooking() != null) {
                BookingDTO bookingDTO = Utils.mapBookingEntityToBookingDTOPlusBookedRoom(payment.getBooking(), false);
                paymentDTO.setBooking(bookingDTO);
            }

            response.setPayment(paymentDTO);

        } catch (GlobalException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (StripeException e) {
            logger.error("Stripe error: {}", e.getMessage());
            response.setStatusCode(500);
            response.setMessage("Error confirming payment: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error confirming payment: {}", e.getMessage());
            response.setStatusCode(500);
            response.setMessage("Error confirming payment: " + e.getMessage());
        }

        return response;
    }

    @Override
    public ResponseAPI getPaymentByBookingId(Long bookingId) {
        ResponseAPI response = new ResponseAPI();

        try {
            Payment payment = paymentRepository.findByBookingId(bookingId)
                    .orElseThrow(() -> new GlobalException("Payment not found for booking ID: " + bookingId));

            PaymentDTO paymentDTO = mapPaymentToDTO(payment);

            response.setStatusCode(200);
            response.setMessage("Payment retrieved successfully");
            response.setPayment(paymentDTO);

        } catch (GlobalException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving payment: {}", e.getMessage());
            response.setStatusCode(500);
            response.setMessage("Error retrieving payment: " + e.getMessage());
        }

        return response;
    }

    @Override
    public ResponseAPI getPaymentByPaymentIntentId(String paymentIntentId) {
        ResponseAPI response = new ResponseAPI();

        try {
            Payment payment = paymentRepository.findByPaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new GlobalException("Payment not found for payment intent ID: " + paymentIntentId));

            PaymentDTO paymentDTO = mapPaymentToDTO(payment);

            response.setStatusCode(200);
            response.setMessage("Payment retrieved successfully");
            response.setPayment(paymentDTO);

        } catch (GlobalException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            logger.error("Error retrieving payment: {}", e.getMessage());
            response.setStatusCode(500);
            response.setMessage("Error retrieving payment: " + e.getMessage());
        }

        return response;
    }

    @Override
    public ResponseAPI getAllPayments() {
        ResponseAPI response = new ResponseAPI();

        try {
            List<Payment> payments = paymentRepository.findAll();
            List<PaymentDTO> paymentDTOs = payments.stream()
                    .map(this::mapPaymentToDTO)
                    .collect(Collectors.toList());

            response.setStatusCode(200);
            response.setMessage("Payments retrieved successfully");
            response.setPaymentList(paymentDTOs);

        } catch (Exception e) {
            logger.error("Error retrieving payments: {}", e.getMessage());
            response.setStatusCode(500);
            response.setMessage("Error retrieving payments: " + e.getMessage());
        }

        return response;
    }

    private PaymentDTO mapPaymentToDTO(Payment payment) {
        PaymentDTO paymentDTO = new PaymentDTO();
        paymentDTO.setId(payment.getId());
        paymentDTO.setPaymentIntentId(payment.getPaymentIntentId());
        paymentDTO.setAmount(payment.getAmount());
        paymentDTO.setCurrency(payment.getCurrency());
        paymentDTO.setStatus(payment.getStatus());
        paymentDTO.setPaymentMethod(payment.getPaymentMethod());
        paymentDTO.setCreatedAt(payment.getCreatedAt());
        paymentDTO.setUpdatedAt(payment.getUpdatedAt());
        paymentDTO.setReceiptUrl(payment.getReceiptUrl());

        if (payment.getBooking() != null) {
            BookingDTO bookingDTO = Utils.mapBookingEntityToBookingDTOPlusBookedRoom(payment.getBooking(), false);
            paymentDTO.setBooking(bookingDTO);
        }

        return paymentDTO;
    }
}
