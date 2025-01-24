package example.BookingBE.Service.User;

import example.BookingBE.Entity.User;
import example.BookingBE.Request.LoginRequest;
import example.BookingBE.Response.ResponseAPI;

public interface UserService {

    ResponseAPI register(User user);
    ResponseAPI login(LoginRequest loginRequest);
    ResponseAPI getAllUser();
    ResponseAPI getUserBookingHistory(String userId);
    ResponseAPI deleteUser(String userId);
    ResponseAPI getUserById(String userId);
    ResponseAPI getUserInformation(String email);
}
