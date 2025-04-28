package example.BookingBE.repository;

import example.BookingBE.model.ChatMessage;
import example.BookingBE.model.ChatSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class ChatSessionRepository {
    private static final Logger logger = LoggerFactory.getLogger(ChatSessionRepository.class);

    // In-memory storage for chat sessions
    private final Map<String, ChatSession> sessions = new ConcurrentHashMap<>();

    /**
     * Create a new chat session
     * @param userId the user ID
     * @return the created chat session
     */
    public ChatSession createSession(String userId) {
        String sessionId = UUID.randomUUID().toString();
        ChatSession session = new ChatSession(sessionId, userId);
        sessions.put(sessionId, session);
        return session;
    }

    /**
     * Get a chat session by ID
     * @param sessionId the session ID
     * @return the chat session, or null if not found
     */
    public ChatSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    /**
     * Get all chat sessions for a user
     * @param userId the user ID
     * @return a list of chat sessions
     */
    public List<ChatSession> getSessionsByUser(String userId) {
        return sessions.values().stream()
                .filter(session -> session.getUserId().equals(userId))
                .collect(Collectors.toList());
    }

    /**
     * Add a message to a chat session
     * @param sessionId the session ID
     * @param message the chat message
     * @return the updated chat session, or null if the session doesn't exist
     */
    public ChatSession addMessage(String sessionId, ChatMessage message) {
        ChatSession session = sessions.get(sessionId);
        if (session != null) {
            message.setId(UUID.randomUUID().toString());
            session.addMessage(message);
            sessions.put(sessionId, session);
        }
        return session;
    }

    /**
     * Get the most recent session for a user, or create a new one if none exists
     * @param userId the user ID
     * @return the most recent chat session
     */
    public ChatSession getOrCreateLatestSession(String userId) {
        List<ChatSession> userSessions = getSessionsByUser(userId);

        if (userSessions.isEmpty()) {
            return createSession(userId);
        }

        // Return the most recently updated session
        return userSessions.stream()
                .max((s1, s2) -> s1.getLastUpdatedAt().compareTo(s2.getLastUpdatedAt()))
                .orElseGet(() -> createSession(userId));
    }

    /**
     * Get the messages from a chat session
     * @param sessionId the session ID
     * @return a list of chat messages, or an empty list if the session doesn't exist
     */
    public List<ChatMessage> getMessages(String sessionId) {
        ChatSession session = sessions.get(sessionId);
        return session != null ? session.getMessages() : new ArrayList<>();
    }

    /**
     * Get the most recent messages from a chat session, limited to a certain number
     * @param sessionId the session ID
     * @param limit the maximum number of messages to return
     * @return a list of the most recent chat messages
     */
    public List<ChatMessage> getRecentMessages(String sessionId, int limit) {
        List<ChatMessage> messages = getMessages(sessionId);
        int size = messages.size();

        // Log the messages for debugging
        for (int i = 0; i < messages.size(); i++) {
            ChatMessage msg = messages.get(i);
            logger.debug("Message {}: role={}, text={}", i, msg.getRole(), msg.getMessage());
        }

        if (size <= limit) {
            return messages;
        }

        List<ChatMessage> recentMessages = messages.subList(size - limit, size);
        logger.debug("Returning {} most recent messages out of {}", recentMessages.size(), size);
        return recentMessages;
    }
}
