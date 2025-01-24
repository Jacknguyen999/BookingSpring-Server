package example.BookingBE.Entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")

public class User implements UserDetails {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = "Name is mandatory")
    private String name;
    @NotBlank(message = "Email is mandatory")
    private String email;
    @NotBlank(message = "phoneNum is mandatory")
    private String phoneNum;
    @NotBlank(message = "password is mandatory")
    private String password;
    private String role ;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Booking> userBookings = new ArrayList<>();

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(@NotBlank(message = "Name is mandatory") String name) {
        this.name = name;
    }

    public void setEmail(@NotBlank(message = "Email is mandatory") String email) {
        this.email = email;
    }

    public void setPhoneNum(@NotBlank(message = "phoneNum is mandatory") String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public void setPassword(@NotBlank(message = "password is mandatory") String password) {
        this.password = password;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<Booking> getUserBookings() {
        return userBookings;
    }

    public void setUserBookings(List<Booking> userBookings) {
        this.userBookings = userBookings;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(
                new SimpleGrantedAuthority(role)
        );
    }

    @Override
    public String getUsername() {
        return email;
    }
    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public Long getId() {
        return id;
    }

    public @NotBlank(message = "Name is mandatory") String getName() {
        return name;
    }

    public @NotBlank(message = "Email is mandatory") String getEmail() {
        return email;
    }

    public @NotBlank(message = "phoneNum is mandatory") String getPhoneNum() {
        return phoneNum;
    }

    public String getRole() {
        return role;
    }
}
