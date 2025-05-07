package example.BookingBE.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String sessionId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String paymentMethod;
    private String receiptUrl;
    
    @OneToOne
    @JoinColumn(name = "booking_id")
    private Booking booking;
} 