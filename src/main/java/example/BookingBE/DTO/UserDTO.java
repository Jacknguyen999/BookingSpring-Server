package example.BookingBE.DTO;



import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Data
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {

    private Long id;
    private String email;

    private String name;
    private String phoneNum;

    private String role;

    private List<BookingDTO> bookings = new ArrayList<>();


}
