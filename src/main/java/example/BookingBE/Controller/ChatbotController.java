package example.BookingBE.Controller;

import example.BookingBE.Service.ChatbotService;
import example.BookingBE.model.ChatMessage;
import example.BookingBE.model.ChatRequest;
import example.BookingBE.model.ChatResponse;
import example.BookingBE.model.ChatSession;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chatbot")
@Tag(name = "Chatbot", description = "API for chatbot interactions using Gemini API")
public class ChatbotController {
    private static final Logger logger = LoggerFactory.getLogger(ChatbotController.class);

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping("/chat")
    @Operation(summary = "Send a message to the chatbot", description = "Processes a user message and returns a response from the Gemini AI")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest request) {
        logger.info("Received chat request: {}", request);

        try {
            // Validate request
            if (request.getUserId() == null || request.getUserId().isEmpty()) {
                request.setUserId("anonymous"); // Default user ID if not provided
            }

            // Always use the version with userId to ensure a session is created
            ChatResponse response = chatbotService.processMessage(
                request.getMessage(),
                request.getUserId(),
                request.getSessionId() // This can be null for first message
            );

            logger.info("Chat response generated successfully: {}", response.isSuccess());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error processing chat request: ", e);
            return ResponseEntity.ok(new ChatResponse(false, "An unexpected error occurred. Please try again later."));
        }
    }

    @GetMapping("/history")
    @Operation(summary = "Get conversation history", description = "Retrieves the conversation history for a specific session")
    public ResponseEntity<List<ChatMessage>> getHistory(@RequestParam String sessionId) {
        logger.info("Received history request for session: {}", sessionId);

        try {
            List<ChatMessage> history = chatbotService.getConversationHistory(sessionId);
            return ResponseEntity.ok(history);
        } catch (Exception e) {
            logger.error("Error retrieving conversation history: ", e);
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/sessions")
    @Operation(summary = "Get user sessions", description = "Retrieves all chat sessions for a user")
    public ResponseEntity<List<ChatSession>> getUserSessions(@RequestParam String userId) {
        logger.info("Received sessions request for user: {}", userId);

        try {
            List<ChatSession> sessions = chatbotService.getUserSessions(userId);
            return ResponseEntity.ok(sessions);
        } catch (Exception e) {
            logger.error("Error retrieving user sessions: ", e);
            return ResponseEntity.badRequest().build();
        }
    }
}
