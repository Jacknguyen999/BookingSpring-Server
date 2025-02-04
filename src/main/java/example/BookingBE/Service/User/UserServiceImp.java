package example.BookingBE.Service.User;

import example.BookingBE.Config.JWTUntils;
import example.BookingBE.DTO.UserDTO;
import example.BookingBE.Entity.User;
import example.BookingBE.Exception.GlobalException;
import example.BookingBE.Repository.UserRepository;
import example.BookingBE.Request.LoginRequest;
import example.BookingBE.Response.ResponseAPI;
import example.BookingBE.Utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UserServiceImp implements UserService{


    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JWTUntils jwtUntils;

    @Autowired
    private AuthenticationManager authenticationManager;




    @Override
    public ResponseAPI register(User user) {
        ResponseAPI responseAPI = new ResponseAPI();

        try{
            if(user.getRole() == null || user.getRole().isEmpty()){
                user.setRole("USER");
            }

            if ( userRepository.existsByEmail(user.getEmail())){
                responseAPI.setStatusCode(400);
                responseAPI.setMessage("Email already exists");
                return responseAPI;
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User savedUser = userRepository.save(user);
            UserDTO userDTO = Utils.mapUserEntityToUserDTO(savedUser);

            responseAPI.setStatusCode(200);
            responseAPI.setUser(userDTO);
            responseAPI.setMessage("User registered successfully");



        }
        catch (GlobalException e){
            responseAPI.setStatusCode(400);
            responseAPI.setMessage(e.getMessage());
        }
        catch (Exception e){
            responseAPI.setStatusCode(500);
            responseAPI.setMessage("ERROR" + e.getMessage());
        }
        return responseAPI;
    }

    @Override
    public ResponseAPI login(LoginRequest loginRequest) {
        ResponseAPI responseAPI = new ResponseAPI();

        try{
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),loginRequest.getPassword()));

            var user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(() -> new GlobalException("User not found"));


            var token = jwtUntils.generateToken(user);
            responseAPI.setStatusCode(200);
            responseAPI.setToken(token);
            responseAPI.setRole(user.getRole());
            responseAPI.setExpirationTime(" 10 days");

            responseAPI.setMessage("Login successfully");


        } catch (GlobalException e){
            responseAPI.setStatusCode(404);
            responseAPI.setMessage(e.getMessage());
        } catch (Exception e){
            responseAPI.setStatusCode(500);
            responseAPI.setMessage("ERROR" + e.getMessage());
        }
        return responseAPI;
    }

    @Override
    public ResponseAPI getAllUser() {

        ResponseAPI responseAPI = new ResponseAPI();
        try {
            List<User> userList = userRepository.findAll();
            List<UserDTO> userDTOList = Utils.mapUserListEntityToUserDTOList(userList);
            responseAPI.setStatusCode(200);
            responseAPI.setMessage("successful");
            responseAPI.setUserList(userDTOList);

        } catch (Exception e) {
            responseAPI.setStatusCode(500);
            responseAPI.setMessage("Error getting all users " + e.getMessage());
        }

        return responseAPI;
    }

    @Override
    public ResponseAPI getUserBookingHistory(String userId) {
        ResponseAPI responseAPI = new ResponseAPI();
        try {
            User user = userRepository.findById(Long.valueOf(userId)).orElseThrow(() -> new GlobalException("User Not Found"));
            UserDTO userDTO = Utils.mapUserEntityToUserDTOPLusUserBookingAndRoom(user);
            responseAPI.setStatusCode(200);
            responseAPI.setMessage("successful");
            responseAPI.setUser(userDTO);

        } catch (GlobalException e) {
            responseAPI.setStatusCode(404);
            responseAPI.setMessage(e.getMessage());

        } catch (Exception e) {

            responseAPI.setStatusCode(500);
            responseAPI.setMessage("Error getting all users " + e.getMessage());
        }
        return responseAPI;
    }

    @Override
    public ResponseAPI deleteUser(String userId) {
        ResponseAPI responseAPI = new ResponseAPI();

        try {
            userRepository.findById(Long.valueOf(userId)).orElseThrow(() -> new GlobalException("User Not Found"));
            userRepository.deleteById(Long.valueOf(userId));
            responseAPI.setStatusCode(200);
            responseAPI.setMessage("successful");

        } catch (GlobalException e) {
            responseAPI.setStatusCode(404);
            responseAPI.setMessage(e.getMessage());

        } catch (Exception e) {

            responseAPI.setStatusCode(500);
            responseAPI.setMessage("Error getting all users " + e.getMessage());
        }
        return responseAPI;
    }

    @Override
    public ResponseAPI getUserById(String userId) {
        ResponseAPI responseAPI = new ResponseAPI();

        try {
            User user = userRepository.findById(Long.valueOf(userId)).orElseThrow(() -> new GlobalException("User Not Found"));
            UserDTO userDTO = Utils.mapUserEntityToUserDTO(user);
            responseAPI.setStatusCode(200);
            responseAPI.setMessage("successful");
            responseAPI.setUser(userDTO);

        } catch (GlobalException e) {
            responseAPI.setStatusCode(404);
            responseAPI.setMessage(e.getMessage());

        } catch (Exception e) {

            responseAPI.setStatusCode(500);
            responseAPI.setMessage("Error getting all users " + e.getMessage());
        }
        return responseAPI;
    }

    @Override
    public ResponseAPI getUserInformation(String email) {
        ResponseAPI responseAPI = new ResponseAPI();
        try {
            User user = userRepository.findByEmail(email).orElseThrow(() -> new GlobalException("User Not Found"));
            UserDTO userDTO = Utils.mapUserEntityToUserDTOPLusUserBookingAndRoom(user);
            responseAPI.setStatusCode(200);
            responseAPI.setMessage("successful");
            responseAPI.setUser(userDTO);

        } catch (GlobalException e) {
            responseAPI.setStatusCode(404);
            responseAPI.setMessage(e.getMessage());

        } catch (Exception e) {

            responseAPI.setStatusCode(500);
            responseAPI.setMessage("Error getting all users " + e.getMessage());
        }
        return responseAPI;
    }
}
