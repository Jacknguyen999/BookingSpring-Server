package example.BookingBE.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bookings")
public class Booking {

    public @NotNull(message = "check In Date is mandatory") LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(@NotNull(message = "check In Date is mandatory") LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public @NotNull(message = "check Out Date is mandatory") LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(@NotNull(message = "check Out Date is mandatory") LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    @Min(value = 1, message = "Number of adults must be at least 1")
    public int getNumOfAdults() {
        return numOfAdults;
    }

    @Min(value = 0, message = "Number of children must be at least 0")
    public int getNumOfChildren() {
        return numOfChildren;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "check In Date is mandatory")
    private LocalDate checkInDate;
    @NotNull(message = "check Out Date is mandatory")
    private LocalDate checkOutDate;

    @Min(value = 1, message = "Number of adults must be at least 1")
    private int numOfAdults;

    @Min(value = 0, message = "Number of children must be at least 0")
    private int numOfChildren;

    private int totalNumberOfGuests;

    private String bookingConfirmationCode;

    @Column(nullable = false)
    private BigDecimal totalPrice;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;

    public void setTotalNumberOfGuests() {
        this.totalNumberOfGuests = this.numOfAdults + this.numOfChildren;
    }

    public void calculateTotalPrice() {
        if (room != null && checkInDate != null && checkOutDate != null) {
            long nights = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
            this.totalPrice = room.getRoomPrice().multiply(BigDecimal.valueOf(nights));
        }
    }

    public void setNumOfAdults(@Min(value = 1, message = "Number of adults must be at least 1") int numOfAdults) {
        this.numOfAdults = numOfAdults;
        setTotalNumberOfGuests();
    }

    public void setNumOfChildren(@Min(value = 0, message = "Number of children must be at least 0") int numOfChildren) {
        this.numOfChildren = numOfChildren;
        setTotalNumberOfGuests();
    }

    @Override
    public String toString() {
        return "Booking{" +
                "id=" + id +
                ", checkInDate=" + checkInDate +
                ", checkOutDate=" + checkOutDate +
                ", numOfAdults=" + numOfAdults +
                ", numOfChildren=" + numOfChildren +
                ", totalNumberOfGuests=" + totalNumberOfGuests +
                ", bookingConfirmationCode='" + bookingConfirmationCode + '\'' +
                ", totalPrice=" + totalPrice +
                '}';
    }
}
