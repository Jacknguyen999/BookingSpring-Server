package example.BookingBE.DTO;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RoomDTO {
    private Long id;

    private String roomType;
    private BigDecimal roomPrice;

    private String roomDescription;
    private String roomImageUrl;

    private List<BookingDTO> bookings;


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
