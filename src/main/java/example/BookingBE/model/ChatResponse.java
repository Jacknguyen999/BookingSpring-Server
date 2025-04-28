package example.BookingBE.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String response;
    private boolean success;
    private String error;
    private String sessionId;
    private LocalDateTime timestamp;
    private List<ChatMessage> history;

    public ChatResponse(String response) {
        this.response = response;
        this.success = true;
        this.timestamp = LocalDateTime.now();
    }

    public ChatResponse(boolean success, String error) {
        this.success = success;
        this.error = error;
        this.timestamp = LocalDateTime.now();
    }

    public ChatResponse(String response, String sessionId) {
        this.response = response;
        this.sessionId = sessionId;
        this.success = true;
        this.timestamp = LocalDateTime.now();
    }
}
