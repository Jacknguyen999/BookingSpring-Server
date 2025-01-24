package example.BookingBE.DTO;



import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;


@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoomDTO {
    public void setBookings(List<BookingDTO> bookings) {
        this.bookings = bookings;
    }

    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public void setRoomPrice(BigDecimal roomPrice) {
        this.roomPrice = roomPrice;
    }

    public void setRoomDescription(String roomDescription) {
        this.roomDescription = roomDescription;
    }

    public void setRoomImageUrl(String roomImageUrl) {
        this.roomImageUrl = roomImageUrl;
    }

    private String roomType;
    private BigDecimal roomPrice;

    private String roomDescription;
    private String roomImageUrl;

    private List<BookingDTO> bookings;

    public Long getId() {
        return id;
    }

    public String getRoomType() {
        return roomType;
    }

    public BigDecimal getRoomPrice() {
        return roomPrice;
    }

    public String getRoomDescription() {
        return roomDescription;
    }

    public String getRoomImageUrl() {
        return roomImageUrl;
    }

    public List<BookingDTO> getBookings() {
        return bookings;
    }

    @Override
    public String toString() {
        return "RoomDTO{" +
                "id=" + id +
                ", roomType='" + roomType + '\'' +
                ", roomPrice=" + roomPrice +
                ", roomDescription='" + roomDescription + '\'' +
                ", roomImageUrl='" + roomImageUrl + '\'' +
                '}';
    }



}
