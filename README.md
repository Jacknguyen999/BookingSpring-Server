# BookingHotelBE

Welcome to the **BookingHotelBE** repository! This project is the backend system for a hotel booking platform. It provides APIs for user authentication, room management, booking processing, and more.

---

## Features

- User Authentication and Authorization (JWT-based)
- Role Management (Admin, Customers, Hotel Owners)
- Hotel and Room CRUD Operations
- Booking Management
- Payment Gateway Integration
- Database Connectivity and Optimization

---

## Tech Stack

- **Programming Language**: Java
- **Framework**: Spring Boot
- **Database**: MySQL
- **Authentication**: JSON Web Tokens (JWT)
- **Others**: Spring Security, Hibernate, and more

---

## Getting Started

### Prerequisites

Ensure you have the following installed:

- [Java JDK](https://www.oracle.com/java/technologies/javase-downloads.html) (v11 or later)
- [MySQL](https://dev.mysql.com/downloads/installer/)
- [Maven](https://maven.apache.org/install.html)

### Installation

1. Clone this repository:

   ```bash
   git clone https://github.com/Jacknguyen999/BookingHotelBE.git
   cd BookingHotelBE
   ```

2. Configure the application properties:

   Update the `src/main/resources/application.properties` file with your database and other configuration details:

   ```properties
   server.port=8080
   spring.datasource.url=jdbc:mysql://localhost:3306/booking_hotel_db
   spring.datasource.username=your_username
   spring.datasource.password=your_password
   spring.jpa.hibernate.ddl-auto=update
   jwt.secret=your_jwt_secret
   payment.gateway.key=your_payment_gateway_key
   ```

3. Build and run the application:

   ```bash
   mvn spring-boot:run
   ```

   The server will start at `http://localhost:8080`.

---

## Directory Structure

Below is the main structure of the `src` folder:

```
src/
├── main/
   ├── java/
   │   └── com/
   │       └── example/
   │           └── bookinghotel/
   │               ├── BookingApplication.java
   │               ├── controller/
   │               ├── model/
   │               ├── repository/
   │               ├── request/
   │               ├── response/
   │               └── service/
   └── resources/
       └── application.properties
```

---

## API Documentation

### Base URL

`http://localhost:8080/api`

### Swagger UI
`http://localhost:8080/swagger-ui/index.html`

### Endpoints

#### User Authentication
- `POST /api/auth/register` - Register a new user
- `POST /api/auth/login` - Log in a user

#### Hotels & Rooms
- `GET /api/hotels` - Get all hotels
- `POST /api/hotels` - Add a new hotel (Admin only)
- `GET /api/hotels/:id/rooms` - Get all rooms for a specific hotel
- `POST /api/rooms` - Add a new room (Admin only)
- `PUT /api/rooms/:id` - Update a room (Admin only)
- `DELETE /api/rooms/:id` - Delete a room (Admin only)

#### Bookings
- `POST /api/bookings` - Place a booking
- `GET /api/bookings/:userId` - Get all bookings for a specific user
- `PUT /api/bookings/:id/status` - Update booking status (Admin/Hotel Owner)

#### Payments
- `POST /api/payments` - Process a payment

*(Full documentation can be found in the `/docs` folder or via Swagger UI if integrated.)*

---

## Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch:

   ```bash
   git checkout -b feature/your-feature-name
   ```

3. Commit your changes:

   ```bash
   git commit -m "Add your feature"
   ```

4. Push the branch:

   ```bash
   git push origin feature/your-feature-name
   ```

5. Open a pull request

---

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.

---

## Contact

For any inquiries, please reach out to:

- **Author**: Jack Nguyen
- **Email**: jacknguyen999@example.com
- **GitHub**: [Jacknguyen999](https://github.com/Jacknguyen999)

---

Thank you for checking out the BookingHotelBE project! Your feedback and contributions are greatly appreciated!
