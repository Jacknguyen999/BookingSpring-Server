package example.BookingBE.Controller;


import example.BookingBE.Repository.RoomRepository;
import example.BookingBE.Response.ResponseAPI;
import example.BookingBE.Service.Room.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/room")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomRepository roomRepository;


    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResponseAPI> addNewRoom(
            @RequestParam(value = "photos", required = false) List<MultipartFile> photos,
            @RequestParam(value = "roomType", required = false) String roomType,
            @RequestParam(value = "roomPrice", required = false) BigDecimal roomPrice,
            @RequestParam(value = "roomDescription", required = false) String roomDescription) {

        if (photos == null || photos.isEmpty() ||
                roomType == null || roomType.isBlank() ||
                roomPrice == null ||
                roomDescription == null || roomDescription.isBlank()) {
            ResponseAPI response = new ResponseAPI();
            response.setStatusCode(400);
            response.setMessage("Please provide values for all fields (photos, roomType, roomPrice, roomDescription)");
            return ResponseEntity.status(response.getStatusCode()).body(response);
        }

        ResponseAPI response = roomService.addNewRoom(photos, roomType, roomPrice, roomDescription);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
    @GetMapping("/all")
    public ResponseEntity<ResponseAPI> getAllRooms() {
        ResponseAPI response = roomService.getAllRooms();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/types")
    public List<String> getRoomTypes() {
        return roomService.getAllRoomTypes();
    }

    @GetMapping("/room-by-id/{roomId}")
    public ResponseEntity<ResponseAPI> getRoomById(@PathVariable Long roomId) {
        ResponseAPI response = roomService.getRoomById(roomId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/all-available-rooms")
    public ResponseEntity<ResponseAPI> getAvailableRooms() {
        ResponseAPI response = roomService.getAllAvailableRooms();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @GetMapping("/available-rooms-by-date-and-type")
    public ResponseEntity<ResponseAPI> getAvailableRoomsByDateAndType(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
            @RequestParam(required = false) String roomType

            ) {

        if (checkInDate == null ||
                checkOutDate == null ||
                roomType == null || roomType.isBlank()) {
            ResponseAPI response = new ResponseAPI();
            response.setStatusCode(400);
            response.setMessage("Please provide values for all fields (checkInDate, checkOutDate, roomType)");
            return ResponseEntity.status(response.getStatusCode()).body(response);
        }

        ResponseAPI response = roomService.getAvailableRoomsByDataAndType(checkInDate, checkOutDate, roomType);
        return ResponseEntity.status(response.getStatusCode()).body(response);

    }

    @DeleteMapping("delete/{roomId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResponseAPI> deleteRoom(@PathVariable Long roomId) {
        ResponseAPI response = roomService.deleteRoom(roomId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PutMapping("/update/{roomId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResponseAPI> updateRoom(
            @PathVariable Long roomId,
            @RequestParam(value = "photos", required = false) List<MultipartFile> photos,
            @RequestParam(value = "roomType", required = false) String roomType,
            @RequestParam(value = "roomPrice", required = false) BigDecimal roomPrice,
            @RequestParam(value = "roomDescription", required = false) String roomDescription) {

        ResponseAPI response = roomService.updateRoom(roomId, roomDescription, roomType, roomPrice, photos);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
