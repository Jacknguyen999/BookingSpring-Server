package example.BookingBE.Utils;

import example.BookingBE.DTO.BookingDTO;
import example.BookingBE.DTO.RoomDTO;
import example.BookingBE.DTO.UserDTO;
import example.BookingBE.Entity.Booking;
import example.BookingBE.Entity.Room;
import example.BookingBE.Entity.User;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.stream.Collectors;


@Service
public class Utils {

    private static final String ALPHANNUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
            + "0123456789"
            + "abcdefghijklmnopqrstuvxyz";




    public static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int index = (int) (ALPHANNUMERIC_STRING.length() * Math.random());
            sb.append(ALPHANNUMERIC_STRING.charAt(index));
        }

        return sb.toString();
    }

    public static UserDTO mapUserEntityToUserDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setRole(user.getRole());
        userDTO.setPhoneNum(user.getPhoneNum());

        return userDTO;
    }

    public static RoomDTO mapRoomEntityToRoomDTO(Room room) {
        RoomDTO roomDTO = new RoomDTO();

        // Ánh xạ các trường và in log để kiểm tra
        roomDTO.setId(room.getId());
//        System.out.println("Mapping ID: " + room.getId());
        roomDTO.setRoomType(room.getRoomType());
//        System.out.println("Mapping Room Type: " + room.getRoomType());
        roomDTO.setRoomPrice(room.getRoomPrice());
//        System.out.println("Mapping Room Price: " + room.getRoomPrice());
        roomDTO.setRoomImageUrl(room.getRoomImageUrl());
//        System.out.println("Mapping Room Image URL: " + room.getRoomImageUrl());
        roomDTO.setRoomDescription(room.getRoomDescription());
//        System.out.println("Mapping Room Description: " + room.getRoomDescription());

        return roomDTO;
    }


    public static BookingDTO mapBookingEntityToBookingDTO(Booking booking) {
        BookingDTO bookingDTO = new BookingDTO();
        bookingDTO.setId(booking.getId());
        bookingDTO.setCheckInDate(booking.getCheckInDate());
        bookingDTO.setCheckOutDate(booking.getCheckOutDate());
        bookingDTO.setNumOfAdults(booking.getNumOfAdults());
        bookingDTO.setNumOfChildren(booking.getNumOfChildren());
        bookingDTO.setTotalNumberOfGuests(booking.getTotalNumberOfGuests());
        bookingDTO.setBookingConfirmationCode(booking.getBookingConfirmationCode());


        return bookingDTO;
    }

    public static RoomDTO mapRoomEntityToRoomDTOPlusBookings(Room room) {
        RoomDTO roomDTO = mapRoomEntityToRoomDTO(room);

        if (room.getBookings() != null){
            roomDTO.setBookings(room.getBookings().stream().map(Utils::mapBookingEntityToBookingDTO).collect(Collectors.toList()));
        }

        return roomDTO;


    }

    public static BookingDTO mapBookingEntityToBookingDTOPlusBookedRoom (Booking booking,boolean mapUser){
        BookingDTO bookingDTO = mapBookingEntityToBookingDTO(booking);

        if (mapUser){
            bookingDTO.setUser(mapUserEntityToUserDTO(booking.getUser()));
        }

        bookingDTO.setRoom(mapRoomEntityToRoomDTO(booking.getRoom()));

        return bookingDTO;

    }



    public static UserDTO mapUserEntityToUserDTOPLusUserBookingAndRoom(User user) {
        UserDTO userDTO = mapUserEntityToUserDTO(user);

        if (!user.getUserBookings().isEmpty()){
            userDTO.setBookings(user.getUserBookings().stream().map(booking -> mapBookingEntityToBookingDTOPlusBookedRoom(booking,false)).collect(Collectors.toList()));


        }

        return userDTO;
    }

    public static List<UserDTO> mapUserListEntityToUserDTOList(List<User> users) {
        return users.stream().map(Utils::mapUserEntityToUserDTO).collect(Collectors.toList());
    }

    public static List<RoomDTO> mapRoomListEntityToRoomDTOList(List<Room> room) {

        return room.stream().map(Utils::mapRoomEntityToRoomDTO).collect(Collectors.toList());
    }

    public static List<BookingDTO> mapBookingListEntityToBookingDTOList(List<Booking> bookings) {

        return bookings.stream().map(Utils::mapBookingEntityToBookingDTO).collect(Collectors.toList());
    }



}
