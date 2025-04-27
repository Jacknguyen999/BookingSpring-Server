package example.BookingBE.Entity;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    @Column(length = 100)
    @ElementCollection
    private List<String> roomImageUrl;

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
