package example.BookingBE.Controller;


import example.BookingBE.Response.ResponseAPI;
import example.BookingBE.Service.User.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping ("/users")
public class UserController {

    @Autowired
    private UserService userService;


    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ResponseAPI> getAllUsers(){
        ResponseAPI responseAPI = userService.getAllUser();

        return ResponseEntity.status(responseAPI.getStatusCode()).body(responseAPI);
    }

    @GetMapping("/get_by_id/{userId}")
    public ResponseEntity<ResponseAPI> getUserById(
            @PathVariable("userId") String userId
    ){
        ResponseAPI responseAPI = userService.getUserById(userId);

        return ResponseEntity.status(responseAPI.getStatusCode()).body(responseAPI);
    }

    @DeleteMapping("/delete_user/{userId}")

    public ResponseEntity<ResponseAPI> deleteUser(
            @PathVariable("userId") String userId
    ){
        ResponseAPI responseAPI = userService.deleteUser(userId);

        return ResponseEntity.status(responseAPI.getStatusCode()).body(responseAPI);
    }

    @GetMapping("/get_current_user_profile")

    public ResponseEntity<ResponseAPI> getCurrentUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        ResponseAPI responseAPI = userService.getUserInformation(email);

        return ResponseEntity.status(responseAPI.getStatusCode()).body(responseAPI);
    }


    @GetMapping("/get_user_booking/{userId}")

    public ResponseEntity<ResponseAPI> getUserBooking(
            @PathVariable("userId") String userId
    ){
        ResponseAPI responseAPI = userService.getUserBookingHistory(userId);

        return ResponseEntity.status(responseAPI.getStatusCode()).body(responseAPI);
    }


}
