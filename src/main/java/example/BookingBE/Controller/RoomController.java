package example.BookingBE.Controller;


import example.BookingBE.Repository.RoomRepository;
import example.BookingBE.Response.ResponseAPI;
import example.BookingBE.Service.Room.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

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
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            @RequestParam(value = "roomType", required = false) String roomType,
            @RequestParam(value = "roomPrice", required = false) BigDecimal roomPrice,
            @RequestParam(value = "roomDescription", required = false) String roomDescription) {

        if (photo == null || photo.isEmpty() ||
                roomType == null || roomType.isBlank() ||
                roomPrice == null ||
                roomDescription == null || roomDescription.isBlank()) {
            ResponseAPI response = new ResponseAPI();
            response.setStatusCode(400);
            response.setMessage("Please provide values for all fields (photo, roomType, roomPrice, roomDescription)");
            return ResponseEntity.status(response.getStatusCode()).body(response);
        }

        ResponseAPI response = roomService.addNewRoom(photo, roomType, roomPrice, roomDescription);
        return ResponseEntity.status(response.getStatusCode()).body(response);

    }
    @GetMapping("/all")
    public ResponseEntity<ResponseAPI> getAllRooms() {
        ResponseAPI response = roomService.getAllRooms();
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }


}
