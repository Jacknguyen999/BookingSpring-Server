package example.BookingBE.Service.Room;

import example.BookingBE.DTO.RoomDTO;
import example.BookingBE.Entity.Room;
import example.BookingBE.Entity.Booking;
import example.BookingBE.Exception.GlobalException;
import example.BookingBE.Repository.RoomRepository;
import example.BookingBE.Repository.BookingRepository;
import example.BookingBE.Response.ResponseAPI;
import example.BookingBE.Service.AwsS3ServiceImp;
import example.BookingBE.Utils.Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


@Service
public class RoomServiceImp implements RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private AwsS3ServiceImp awsS3Service;



    @Override
    public ResponseAPI addNewRoom(List<MultipartFile> photos,
                                  String roomType, BigDecimal roomPrice,
                                  String description) {
        ResponseAPI response = new ResponseAPI();
        try {
            List<String> imageURL = awsS3Service.saveImagestoS3(photos);
            Room room = new Room();
            room.setRoomImageUrl(imageURL);
            room.setRoomType(roomType);
            room.setRoomPrice(roomPrice);
            room.setRoomDescription(description);

            // Save room to database
            Room savedRoom = roomRepository.save(room);
            System.out.println("Saved Room: " + savedRoom);

            // Map to RoomDTO
            RoomDTO roomDTO = Utils.mapRoomEntityToRoomDTO(savedRoom);
            System.out.println("Mapped RoomDTO: " + roomDTO);

            // Set response
            response.setStatusCode(200);
            response.setMessage("Successful");
            response.setRoom(roomDTO);

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error saving a room: " + e.getMessage());
        }
        return response;
    }


    @Override
    public List<String> getAllRoomTypes() {
        return roomRepository.findDistinceRoomType();

    }

    @Override
    public ResponseAPI getAllRooms() {
        ResponseAPI response = new ResponseAPI();
        try{
            List<Room> rooms = roomRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
            List<RoomDTO> roomDTOList = Utils.mapRoomListEntityToRoomDTOList(rooms);
            response.setStatusCode(200);
            response.setMessage("successful");
            response.setRoomList(roomDTOList);


        } catch (Exception e){
            response.setStatusCode(500);
            response.setMessage("Error saving a room " + e.getMessage());
        }
        return response;
    }

    @Override
    public ResponseAPI deleteRoom(Long roomId) {
        ResponseAPI response = new ResponseAPI();

        try{
            roomRepository.findById(roomId).orElseThrow(() -> new GlobalException("Room Not Found"));
            roomRepository.deleteById(roomId);
            response.setStatusCode(200);
            response.setMessage("successful");

        } catch (GlobalException e){
            response.setStatusCode(404);
            response.setMessage( e.getMessage());
        }
        catch (Exception e){
            response.setStatusCode(500);
            response.setMessage("Error saving a room " + e.getMessage());
        }

        return response;
    }

    @Override
    public ResponseAPI updateRoom(Long roomId,
                                  String description,
                                  String roomType,
                                  BigDecimal roomPrice,
                                  List<MultipartFile> photos) {
        ResponseAPI response = new ResponseAPI();
        try{
            List<String> imgURl = null;
            if(photos != null && !photos.isEmpty()){
                imgURl = awsS3Service.saveImagestoS3(photos);
            }
            Room room = roomRepository.findById(roomId).orElseThrow(() -> new GlobalException("Room Not Found"));
            if (roomType != null) room.setRoomType(roomType);
            if (roomPrice != null) room.setRoomPrice(roomPrice);
            if (description != null) room.setRoomDescription(description);
            if (imgURl != null) {
                List<String> currentImages = room.getRoomImageUrl();
                if (currentImages == null) {
                    currentImages = new ArrayList<>();
                }
                currentImages.addAll(imgURl);
                room.setRoomImageUrl(currentImages);
            }


            Room updatedRoom = roomRepository.save(room);
            RoomDTO roomDTO = Utils.mapRoomEntityToRoomDTO(updatedRoom);

            response.setStatusCode(200);
            response.setMessage("successful");
            response.setRoom(roomDTO);

        } catch (GlobalException e){
            response.setStatusCode(404);
            response.setMessage( e.getMessage());
        }
        catch (Exception e){
            response.setStatusCode(500);
            response.setMessage("Error saving a room " + e.getMessage());
        }

        return response;
    }

    @Override
    public ResponseAPI getRoomById(Long roomId) {
        ResponseAPI response = new ResponseAPI();
        try{
            Room room = roomRepository.findById(roomId).orElseThrow(() -> new GlobalException("Room Not Found"));
            RoomDTO roomDTO = Utils.mapRoomEntityToRoomDTOPlusBookings(room);
            response.setStatusCode(200);
            response.setMessage("successful");
            response.setRoom(roomDTO);

        } catch (GlobalException e){
            response.setStatusCode(404);
            response.setMessage( e.getMessage());
        }
        catch (Exception e){
            response.setStatusCode(500);
            response.setMessage("Error saving a room " + e.getMessage());
        }
        return response;
    }

    @Override
    public ResponseAPI getAvailableRoomsByDataAndType(LocalDate checkInDate,
                                                      LocalDate checkOutDate,
                                                      String roomType) {
        ResponseAPI response = new ResponseAPI();

        try {
            List<Room> roomList = roomRepository.findAvailableRoomsByDateAndType(checkInDate,checkOutDate,roomType);
            List<RoomDTO> roomDTOList = Utils.mapRoomListEntityToRoomDTOList(roomList);
            response.setStatusCode(200);
            response.setMessage("successful");
            response.setRoomList(roomDTOList);

        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error saving a room " + e.getMessage());
        }

        return response;
    }

    @Override
    public ResponseAPI getAllAvailableRooms() {
        ResponseAPI response = new ResponseAPI();
        try {
            List<Room> roomList = roomRepository.getAllAvailableRoom();
            List<RoomDTO> roomDTOList = Utils.mapRoomListEntityToRoomDTOList(roomList);
            response.setStatusCode(200);
            response.setMessage("successful");
            response.setRoomList(roomDTOList);

        }catch (GlobalException e){
            response.setStatusCode(404);
            response.setMessage( e.getMessage());
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error saving a room " + e.getMessage());
        }

        return response;
    }

    @Override
    public ResponseAPI getUnavailableDates(Long roomId) {
        ResponseAPI response = new ResponseAPI();
        try {
            Room room = roomRepository.findById(roomId).orElse(null);
            if (room == null) {
                response.setStatusCode(404);
                response.setMessage("Room not found");
                return response;
            }

            List<Booking> bookings = bookingRepository.findByRoomId(roomId);
            List<LocalDate> unavailableDates = new ArrayList<>();

            for (Booking booking : bookings) {
                LocalDate startDate = booking.getCheckInDate();
                LocalDate endDate = booking.getCheckOutDate();
                LocalDate currentDate = startDate;

                while (!currentDate.isAfter(endDate)) {
                    unavailableDates.add(currentDate);
                    currentDate = currentDate.plusDays(1);
                }
            }

            response.setStatusCode(200);
            response.setMessage("Successfully retrieved unavailable dates");
            response.setDates(unavailableDates);
        } catch (Exception e) {
            response.setStatusCode(500);
            response.setMessage("Error getting unavailable dates: " + e.getMessage());
        }
        return response;
    }
}
