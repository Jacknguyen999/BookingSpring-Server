package example.BookingBE.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatSession {
    private String id;
    private String userId;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;
    private List<ChatMessage> messages = new ArrayList<>();
    
    public ChatSession(String id, String userId) {
        this.id = id;
        this.userId = userId;
        this.createdAt = LocalDateTime.now();
        this.lastUpdatedAt = LocalDateTime.now();
    }
    
    public void addMessage(ChatMessage message) {
        this.messages.add(message);
        this.lastUpdatedAt = LocalDateTime.now();
    }
}
