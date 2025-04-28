package example.BookingBE.Service;

import example.BookingBE.model.ChatMessage;
import example.BookingBE.model.ChatResponse;
import example.BookingBE.model.ChatSession;
import example.BookingBE.repository.ChatSessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatbotService {
    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);

    private final GeminiApiClient geminiApiClient;
    private final ChatSessionRepository chatSessionRepository;
    
    @Value("${chatbot.history.max-messages:10}")
    private int maxHistoryMessages;

    @Autowired
    public ChatbotService(GeminiApiClient geminiApiClient, ChatSessionRepository chatSessionRepository) {
        this.geminiApiClient = geminiApiClient;
        this.chatSessionRepository = chatSessionRepository;
    }

    /**
     * Process a message from a user, with conversation history
     * @param message the user's message
     * @param userId the user's ID
     * @param sessionId the session ID (optional)
     * @return the chatbot's response
     */
    public ChatResponse processMessage(String message, String userId, String sessionId) {
        try {
            logger.info("Processing chat message: {}, userId: {}, sessionId: {}", message, userId, sessionId);
            
            // Get or create a chat session
            ChatSession session;
            if (sessionId != null && !sessionId.isEmpty()) {
                session = chatSessionRepository.getSession(sessionId);
                if (session == null) {
                    logger.info("Session ID {} not found, creating new session", sessionId);
                    session = chatSessionRepository.createSession(userId);
                }
            } else {
                logger.info("No session ID provided, creating or getting latest session for user {}", userId);
                session = chatSessionRepository.getOrCreateLatestSession(userId);
            }
            
            logger.info("Using session with ID: {}", session.getId());
            
            // Add the user's message to the session
            ChatMessage userMessage = new ChatMessage(session.getId(), userId, message, "user");
            chatSessionRepository.addMessage(session.getId(), userMessage);
            
            // Get recent conversation history
            List<ChatMessage> recentMessages = chatSessionRepository.getRecentMessages(session.getId(), maxHistoryMessages);
            logger.debug("Retrieved {} recent messages for session {}", recentMessages.size(), session.getId());
            
            // Format conversation history for the API
            String conversationContext = formatConversationHistory(recentMessages);
            logger.debug("Formatted conversation context: {}", conversationContext);
            
            // Call the Gemini API with conversation history
            String response = geminiApiClient.generateContentWithHistory(message, conversationContext);
            
            // Extract the text from the response
            String generatedText = geminiApiClient.extractTextFromResponse(response);
            
            // Add the bot's response to the session
            ChatMessage botMessage = new ChatMessage(session.getId(), userId, generatedText, "bot");
            chatSessionRepository.addMessage(session.getId(), botMessage);
            
            logger.info("Generated response successfully");
            
            // Return the response with the session ID
            ChatResponse chatResponse = new ChatResponse(generatedText);
            chatResponse.setSessionId(session.getId());
            logger.info("Setting session ID in response: {}", session.getId());
            return chatResponse;
        } catch (Exception e) {
            logger.error("Error processing message: ", e);
            return new ChatResponse(false, "Error processing message: " + e.getMessage());
        }
    }
    
    /**
     * Process a message without conversation history (for backward compatibility)
     * @param message the user's message
     * @return the chatbot's response
     */
    public ChatResponse processMessage(String message) {
        try {
            logger.info("Processing chat message without history: {}", message);
            
            // Call the Gemini API
            String response = geminiApiClient.generateContent(message);
            
            // Extract the text from the response
            String generatedText = geminiApiClient.extractTextFromResponse(response);
            
            logger.info("Generated response successfully");
            return new ChatResponse(generatedText);
        } catch (Exception e) {
            logger.error("Error processing message: ", e);
            return new ChatResponse(false, "Error processing message: " + e.getMessage());
        }
    }
    
    /**
     * Get the conversation history for a session
     * @param sessionId the session ID
     * @return the list of messages in the session
     */
    public List<ChatMessage> getConversationHistory(String sessionId) {
        return chatSessionRepository.getMessages(sessionId);
    }
    
    /**
     * Get all sessions for a user
     * @param userId the user ID
     * @return the list of sessions for the user
     */
    public List<ChatSession> getUserSessions(String userId) {
        return chatSessionRepository.getSessionsByUser(userId);
    }
    
    /**
     * Format conversation history for the API
     * @param messages the list of messages
     * @return a formatted string with the conversation history
     */
    private String formatConversationHistory(List<ChatMessage> messages) {
        if (messages.isEmpty()) {
            return "";
        }
        
        StringBuilder history = new StringBuilder();
        
        // Include all previous messages except the current one
        // The current message will be the last one in the list (the user's latest message)
        for (int i = 0; i < messages.size() - 1; i++) {
            ChatMessage msg = messages.get(i);
            String role = msg.getRole().equals("user") ? "User" : "Assistant";
            history.append(role).append(": ").append(msg.getMessage()).append("\n\n");
        }
        
        logger.debug("Formatted conversation history: {}", history.toString());
        return history.toString().trim();
    }
}
