package example.BookingBE.Controller;


import example.BookingBE.Entity.User;
import example.BookingBE.Request.LoginRequest;
import example.BookingBE.Response.ResponseAPI;
import example.BookingBE.Service.User.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "API for authentication ")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ResponseAPI> register(@RequestBody User user) {
        ResponseAPI response = userService.register(user);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseAPI> login(@RequestBody LoginRequest loginRequest) {
        ResponseAPI response = userService.login(loginRequest);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}
