package example.BookingBE.Entity;


import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "rooms")
public class Room {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomType;
    private BigDecimal roomPrice;
    private String roomDescription;
    private String roomImageUrl;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    private List<Booking> bookings = new ArrayList<>();



    @Override
    public String toString() {
        return "Room{" +
                "id=" + id +
                ", roomType='" + roomType + '\'' +
                ", roomPrice=" + roomPrice +
                ", roomDescription='" + roomDescription + '\'' +
                ", roomImageUrl='" + roomImageUrl + '\'' +
                '}';
    }
}
