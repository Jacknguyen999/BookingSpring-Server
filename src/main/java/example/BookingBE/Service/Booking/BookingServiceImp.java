package example.BookingBE.Service.Booking;

import example.BookingBE.DTO.BookingDTO;
import example.BookingBE.DTO.UserDTO;
import example.BookingBE.Entity.Booking;
import example.BookingBE.Entity.Room;
import example.BookingBE.Entity.User;
import example.BookingBE.Exception.GlobalException;
import example.BookingBE.Repository.BookingRepository;
import example.BookingBE.Repository.RoomRepository;
import example.BookingBE.Repository.UserRepository;
import example.BookingBE.Request.BookingRequest;
import example.BookingBE.Response.ResponseAPI;
import example.BookingBE.Utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class BookingServiceImp implements BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Override
    public ResponseAPI saveBooking(Long roomId, Long userId, Booking bookingRequest) {
        ResponseAPI response = new ResponseAPI();

        try {
            if (bookingRequest.getCheckOutDate().isBefore(bookingRequest.getCheckInDate())) {
                throw new IllegalArgumentException("Check in date must come after check out date");
            }
            Room room = roomRepository.findById(roomId).orElseThrow(() -> new GlobalException("Room not found"));
            User user = userRepository.findById(userId).orElseThrow(() -> new GlobalException("User not found"));
            List<Booking> existingBooking = room.getBookings();

            if (!roomIsAvailable(bookingRequest, existingBooking)) {
                throw new GlobalException("Room is not available at the moment");
            }

            bookingRequest.setRoom(room);
            bookingRequest.setUser(user);
            bookingRequest.calculateTotalPrice(); // Calculate total price

            String bookingConfirmationCode = Utils.generateRandomString(10);
            bookingRequest.setBookingConfirmationCode(bookingConfirmationCode);
            
            Booking savedBooking = bookingRepository.save(bookingRequest);

            user.getUserBookings().add(savedBooking);
            userRepository.save(user);

            response.setStatusCode(200);
            response.setMessage("Success");
            response.setBookingConfirmationCode(bookingConfirmationCode);
            response.setBooking(Utils.mapBookingEntityToBookingDTOPlusBookedRoom(savedBooking, true));

        } catch (GlobalException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error saving booking " + e.getMessage());
        }

        return response;
    }

    @Override
    public ResponseAPI findBookingByConfirmationCode(String confirmationCode) {
        ResponseAPI response = new ResponseAPI();
        try{
            Booking booking = bookingRepository.findByBookingConfirmationCode(confirmationCode).orElseThrow(() -> new GlobalException("Booking Not Found"));
            BookingDTO bookingDTO = Utils.mapBookingEntityToBookingDTOPlusBookedRoom(booking,true);
            response.setStatusCode(200);
            response.setMessage("Successful");
            response.setBooking(bookingDTO);

        } catch (GlobalException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        }  catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error saving booking " +e.getMessage());
        }
        return response;
    }

    @Override
    public ResponseAPI getAllBookings() {
        ResponseAPI response = new ResponseAPI();

        try{
            List<Booking> booking = bookingRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
            List<BookingDTO> bookingDTO = Utils.mapBookingListEntityToBookingDTOList(booking);
            response.setStatusCode(200);
            response.setMessage("successful");
            response.setBookingList(bookingDTO);

        } catch (GlobalException e) {
            response.setStatusCode(404);
            response.setMessage(e.getMessage());
        }  catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error saving booking " +e.getMessage());
        }

        return response;
    }

    @Override
    public ResponseAPI cancelBooking(Long bookingId) {
        ResponseAPI responseAPI = new ResponseAPI();
        try{
            bookingRepository.findById(bookingId).orElseThrow(()-> new GlobalException("Booking not found "));
            bookingRepository.deleteById(bookingId);
            responseAPI.setStatusCode(200);
            responseAPI.setMessage("Successfully");
        } catch (GlobalException e) {
            responseAPI.setStatusCode(404);
            responseAPI.setMessage(e.getMessage());
        }  catch (Exception e) {
            responseAPI.setStatusCode(500);
            responseAPI.setMessage("Error delete booking " +e.getMessage());
        }
        return responseAPI;
    }


    private boolean roomIsAvailable(Booking bookingRequest, List<Booking> existingBookings) {
        return existingBookings.stream()
                .noneMatch(existingBooking ->
                        !(bookingRequest.getCheckOutDate().isBefore(existingBooking.getCheckInDate()) // Trả phòng trước khi đặt khác bắt đầu
                                || bookingRequest.getCheckInDate().isAfter(existingBooking.getCheckOutDate())) // Nhận phòng sau khi đặt khác kết thúc
                );
    }

}
