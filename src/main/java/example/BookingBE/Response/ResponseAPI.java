package example.BookingBE.Response;


import com.fasterxml.jackson.annotation.JsonInclude;

import example.BookingBE.DTO.BookingDTO;
import example.BookingBE.DTO.RoomDTO;
import example.BookingBE.DTO.UserDTO;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Data
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseAPI {

    private int statusCode;
    private String message;
    private String token;
    private String role;
    private String expirationTime;
    private String bookingConfirmationCode;

    private UserDTO user;
    private RoomDTO room;
    private BookingDTO booking;
    private List<UserDTO> userList;
    private List<RoomDTO> RoomList;
    private List<BookingDTO> bookingList;


}
