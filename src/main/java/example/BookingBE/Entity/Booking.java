package example.BookingBE.Entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "bookings")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "check In Date is mandatory")
    private LocalDate checkInDate;
    @NotBlank(message = "check Out Date is mandatory")
    private LocalDate checkOutDate;

    @Min(value = 1, message = "Number of adults must be at least 1")
    private int numOfAdults;

    @Min(value = 0, message = "Number of children must be at least 0")
    private int numOfChildren;

    private int totalNumberOfGuests;

    private String bookingConfirmationCode;


    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;


    public void setTotalNumberOfGuests() {
        this.totalNumberOfGuests = this.numOfAdults + this.numOfChildren;
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
                '}';
    }
}
