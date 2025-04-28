package example.BookingBE.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String id;
    private String sessionId;
    private String userId;
    private String message;
    private String role; // "user" or "bot"
    private LocalDateTime timestamp;
    
    public ChatMessage(String sessionId, String userId, String message, String role) {
        this.sessionId = sessionId;
        this.userId = userId;
        this.message = message;
        this.role = role;
        this.timestamp = LocalDateTime.now();
    }
}
