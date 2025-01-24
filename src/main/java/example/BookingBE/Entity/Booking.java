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

    public Long getId() {
        return id;
    }

    public Room getRoom() {
        return room;
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotBlank(message = "check In Date is mandatory") LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(@NotBlank(message = "check In Date is mandatory") LocalDate checkInDate) {
        this.checkInDate = checkInDate;
    }

    public @NotBlank(message = "check Out Date is mandatory") LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(@NotBlank(message = "check Out Date is mandatory") LocalDate checkOutDate) {
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

    public int getTotalNumberOfGuests() {
        return totalNumberOfGuests;
    }

    public void setTotalNumberOfGuests(int totalNumberOfGuests) {
        this.totalNumberOfGuests = totalNumberOfGuests;
    }

    public String getBookingConfirmationCode() {
        return bookingConfirmationCode;
    }

    public void setBookingConfirmationCode(String bookingConfirmationCode) {
        this.bookingConfirmationCode = bookingConfirmationCode;
    }

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
