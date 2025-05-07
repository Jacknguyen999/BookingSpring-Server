package example.BookingBE.Service.Room;

import com.amazonaws.Response;
import example.BookingBE.Response.ResponseAPI;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface RoomService {

    ResponseAPI addNewRoom(List<MultipartFile> photos, String roomType, BigDecimal roomPrice, String description);

    List<String> getAllRoomTypes();

    ResponseAPI getAllRooms();

    ResponseAPI deleteRoom(Long roomId);

    ResponseAPI updateRoom(Long roomId, String roomDescription, String roomType, BigDecimal roomPrice, List<MultipartFile> photos);

    ResponseAPI getRoomById(Long roomId);

    ResponseAPI getAvailableRoomsByDataAndType(LocalDate checkInDate, LocalDate checkOutDate, String roomType);

    ResponseAPI getAllAvailableRooms();

    ResponseAPI getUnavailableDates(Long roomId);

}
