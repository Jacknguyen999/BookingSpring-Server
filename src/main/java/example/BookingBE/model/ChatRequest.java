package example.BookingBE.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatRequest {
    private String message;
    private String userId;
    private String sessionId;

    public ChatRequest(String message, String userId) {
        this.message = message;
        this.userId = userId;
    }
}
