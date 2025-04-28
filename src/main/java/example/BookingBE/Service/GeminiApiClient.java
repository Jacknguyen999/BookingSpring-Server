package example.BookingBE.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Component
public class GeminiApiClient {
    private static final Logger logger = LoggerFactory.getLogger(GeminiApiClient.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${gemini.api.key}")
    private String apiKey;
    @Value("${gemini.api.url}")
    private String apiUrl;
    @Value("${gemini.api.model}")
    private String model;

    public String generateContent(String prompt) throws IOException {
        String fullUrl = apiUrl + "/" + model + ":generateContent?key=" + apiKey;
        URL url = new URL(fullUrl);

        logger.info("Making API call to Gemini API: {}", fullUrl);
        logger.debug("Prompt: {}", prompt);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.setConnectTimeout(10000); // 10 seconds timeout
        connection.setReadTimeout(10000);    // 10 seconds timeout

        // Create a simple request body
        String requestBody = createRequestBody(prompt);
        logger.debug("Request body: {}", requestBody);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        logger.info("Response code: {}", responseCode);

        // Read the response
        StringBuilder response = new StringBuilder();

        // Check if the request was successful
        if (responseCode >= 200 && responseCode < 300) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
        } else {
            // Read error stream if request was not successful
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
            logger.error("Error response from Gemini API: {}", response.toString());
            throw new IOException("API call failed with response code: " + responseCode +
                                 " and message: " + response.toString());
        }

        String responseString = response.toString();
        logger.debug("Response: {}", responseString);
        return responseString;
    }

    public String extractTextFromResponse(String jsonResponse) {
        try {
            logger.debug("Parsing JSON response: {}", jsonResponse);
            JsonNode responseJson = objectMapper.readTree(jsonResponse);

            // Check if the response contains an error
            if (responseJson.has("error")) {
                JsonNode error = responseJson.get("error");
                String errorMessage = error.has("message") ? error.get("message").asText() : "Unknown error";
                int errorCode = error.has("code") ? error.get("code").asInt() : -1;

                logger.error("API error response: code={}, message={}", errorCode, errorMessage);
                return "Sorry, there was an error with the AI service: " + errorMessage;
            }

            // Check if candidates array exists and is not empty
            if (!responseJson.has("candidates") || responseJson.get("candidates").isEmpty()) {
                logger.error("No candidates found in response: {}", jsonResponse);
                return "Sorry, the AI service didn't provide a valid response.";
            }

            String text = responseJson
                    .path("candidates")
                    .path(0)
                    .path("content")
                    .path("parts")
                    .path(0)
                    .path("text")
                    .asText(null);

            if (text == null) {
                logger.error("No text found in response: {}", jsonResponse);
                return "Sorry, I couldn't generate a response at the moment.";
            }

            logger.info("Successfully extracted text from response");
            return text;
        } catch (Exception e) {
            logger.error("Error extracting response: ", e);
            return "Error extracting response: " + e.getMessage();
        }
    }

    /**
     * Generate content with conversation history
     * @param prompt the user's prompt
     * @param conversationHistory the conversation history
     * @return the API response
     * @throws IOException if an error occurs
     */
    public String generateContentWithHistory(String prompt, String conversationHistory) throws IOException {
        String fullUrl = apiUrl + "/" + model + ":generateContent?key=" + apiKey;
        URL url = new URL(fullUrl);

        logger.info("Making API call to Gemini API with history: {}", fullUrl);
        logger.debug("Prompt: {}", prompt);
        logger.debug("Conversation history: {}", conversationHistory);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);
        connection.setConnectTimeout(15000); // 15 seconds timeout for longer conversations
        connection.setReadTimeout(15000);    // 15 seconds timeout

        // Create request body with conversation history
        String requestBody = createRequestBodyWithHistory(prompt, conversationHistory);
        logger.debug("Request body: {}", requestBody);

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int responseCode = connection.getResponseCode();
        logger.info("Response code: {}", responseCode);

        // Read the response
        StringBuilder response = new StringBuilder();

        // Check if the request was successful
        if (responseCode >= 200 && responseCode < 300) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
        } else {
            // Read error stream if request was not successful
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getErrorStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }
            logger.error("Error response from Gemini API: {}", response.toString());
            throw new IOException("API call failed with response code: " + responseCode +
                                 " and message: " + response.toString());
        }

        String responseString = response.toString();
        logger.debug("Response: {}", responseString);
        return responseString;
    }

    /**
     * Create a request body with conversation history
     * @param prompt the user's prompt
     * @param conversationHistory the conversation history
     * @return the request body as a JSON string
     */
    private String createRequestBodyWithHistory(String prompt, String conversationHistory) {
        try {
            // Use ObjectMapper to create the JSON structure
            ObjectNode rootNode = objectMapper.createObjectNode();

            // Create contents array
            ArrayNode contentsArray = rootNode.putArray("contents");

            // Create a single content item with system instructions, conversation history, and user prompt
            ObjectNode contentNode = contentsArray.addObject();
            ArrayNode partsArray = contentNode.putArray("parts");

            // Build the complete prompt with system instructions, history, and current query
            StringBuilder fullPrompt = new StringBuilder();
            fullPrompt.append("You are a helpful assistant for a hotel booking website. ")
                    .append("You can help users with booking rooms, checking availability, ")
                    .append("viewing room details, and managing their reservations. ")
                    .append("You can understand and respond to queries about room types, prices, ")
                    .append("booking dates, and booking status. ")
                    .append("You can also provide information about hotel amenities and travel recommendations. ")
                    .append("Be friendly, professional, and concise in your responses.\n\n")
                    .append("You can help users with the following tasks:\n")
                    .append("1. Check room availability for specific dates\n")
                    .append("2. Get information about room types and prices\n")
                    .append("3. View booking details using a confirmation code\n")
                    .append("4. View a user's bookings\n")
                    .append("5. Get general information about the hotel and its amenities\n\n");

            // Add conversation history if available
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                fullPrompt.append("Previous conversation:\n")
                        .append(conversationHistory)
                        .append("\n\n");

                // Log the conversation history for debugging
                logger.debug("Including conversation history in request: {}", conversationHistory);
            } else {
                logger.debug("No conversation history to include");
            }

            // Add current query
            fullPrompt.append("User: ").append(prompt);

            // Add the full prompt as a single part
            ObjectNode systemPart = partsArray.addObject();
            systemPart.put("text", fullPrompt.toString());

            // Convert to JSON string
            return objectMapper.writeValueAsString(rootNode);
        } catch (Exception e) {
            logger.error("Error creating request body with history: ", e);
            // Fallback to simple string concatenation if JSON creation fails
            StringBuilder fallbackJson = new StringBuilder();
            fallbackJson.append("{")
                    .append("\"contents\": [")
                    .append("  {")
                    .append("    \"parts\": [")
                    .append("      {")
                    .append("        \"text\": \"You are a helpful assistant for a hotel booking website. ")
                    .append("You can help users with booking rooms, checking availability, ")
                    .append("viewing room details, and managing their reservations. ")
                    .append("You can understand and respond to queries about room types, prices, ")
                    .append("booking dates, and booking status. ")
                    .append("You can also provide information about hotel amenities and travel recommendations. ")
                    .append("Be friendly, professional, and concise in your responses.\\n\\n")
                    .append("You can help users with the following tasks:\\n")
                    .append("1. Check room availability for specific dates\\n")
                    .append("2. Get information about room types and prices\\n")
                    .append("3. View booking details using a confirmation code\\n")
                    .append("4. View a user's bookings\\n")
                    .append("5. Get general information about the hotel and its amenities\\n\\n");

            // Add conversation history if available
            if (conversationHistory != null && !conversationHistory.isEmpty()) {
                fallbackJson.append("Previous conversation:\\n")
                        .append(escapeJson(conversationHistory))
                        .append("\\n\\n");
            }

            // Add current query
            fallbackJson.append("User: ")
                    .append(escapeJson(prompt))
                    .append("\"")
                    .append("      }")
                    .append("    ]")
                    .append("  }")
                    .append("]")
                    .append("}");

            return fallbackJson.toString();
        }
    }

    private String createRequestBody(String prompt) {
        try {
            // Use ObjectMapper to create the JSON structure
            ObjectNode rootNode = objectMapper.createObjectNode();

            // Create contents array
            ArrayNode contentsArray = rootNode.putArray("contents");

            // Create a single content item with both system instructions and user prompt
            ObjectNode contentNode = contentsArray.addObject();
            ArrayNode partsArray = contentNode.putArray("parts");

            // Add system instructions as the first part
            ObjectNode systemPart = partsArray.addObject();
            systemPart.put("text", "You are a helpful assistant for a hotel booking website. " +
                "You can help users with booking rooms, checking availability, " +
                "viewing room details, and managing their reservations. " +
                "You can understand and respond to queries about room types, prices, " +
                "booking dates, and booking status. " +
                "You can also provide information about hotel amenities and travel recommendations. " +
                "Be friendly, professional, and concise in your responses.\n\n" +
                "You can help users with the following tasks:\n" +
                "1. Check room availability for specific dates\n" +
                "2. Get information about room types and prices\n" +
                "3. View booking details using a confirmation code\n" +
                "4. View a user's bookings\n" +
                "5. Get general information about the hotel and its amenities\n\n" +
                "User: " + prompt);

            // Convert to JSON string
            return objectMapper.writeValueAsString(rootNode);
        } catch (Exception e) {
            logger.error("Error creating request body: ", e);
            // Fallback to simple string concatenation if JSON creation fails
            return "{"
                    + "\"contents\": ["
                    + "  {"
                    + "    \"parts\": ["
                    + "      {"
                    + "        \"text\": \"You are a helpful assistant for a hotel booking website. "
                    + "You can help users with booking rooms, checking availability, "
                    + "viewing room details, and managing their reservations. "
                    + "You can understand and respond to queries about room types, prices, "
                    + "booking dates, and booking status. "
                    + "You can also provide information about hotel amenities and travel recommendations. "
                    + "Be friendly, professional, and concise in your responses.\\n\\n"
                    + "You can help users with the following tasks:\\n"
                    + "1. Check room availability for specific dates\\n"
                    + "2. Get information about room types and prices\\n"
                    + "3. View booking details using a confirmation code\\n"
                    + "4. View a user's bookings\\n"
                    + "5. Get general information about the hotel and its amenities\\n\\n"
                    + "User: " + escapeJson(prompt) + "\""
                    + "      }"
                    + "    ]"
                    + "  }"
                    + "]"
                    + "}";
        }
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
