package ru.flux.desktop.chats.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

public class ChatApiClient {
    private static final Duration TIMEOUT = Duration.ofSeconds(5);
    private static final TypeReference<List<ChatResponse>> CHAT_LIST_TYPE = new TypeReference<>() {};

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final URI chatsUri;
    private final String authHeader;

    public ChatApiClient() {
        this(HttpClient.newBuilder().connectTimeout(TIMEOUT).build(), defaultMapper(), defaultChatsUri(), defaultAuthHeader());
    }

    ChatApiClient(HttpClient httpClient, ObjectMapper objectMapper, URI chatsUri, String authHeader) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.chatsUri = chatsUri;
        this.authHeader = authHeader;
    }

    public List<ChatResponse> fetchChats() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(chatsUri)
                .timeout(TIMEOUT)
                .header("Accept", "application/json")
                .header("Authorization", authHeader)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Unexpected response status: " + response.statusCode());
        }
        return objectMapper.readValue(response.body(), CHAT_LIST_TYPE);
    }

    private static ObjectMapper defaultMapper() {
        return new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private static URI defaultChatsUri() {
        String baseUrl = firstNonBlank(
                System.getProperty("flux.api.baseUrl"),
                System.getenv("FLUX_API_BASE_URL"),
                "http://localhost:8080"
        );
        return URI.create(trimTrailingSlash(baseUrl) + "/chats");
    }

    private static String defaultAuthHeader() {
        String username = firstNonBlank(
                System.getProperty("flux.api.username"),
                System.getenv("FLUX_API_USERNAME"),
                "dev"
        );
        String password = firstNonBlank(
                System.getProperty("flux.api.password"),
                System.getenv("FLUX_API_PASSWORD"),
                "dev"
        );
        String token = Base64.getEncoder().encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
        return "Basic " + token;
    }

    private static String trimTrailingSlash(String value) {
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private static String firstNonBlank(String first, String second, String fallback) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return fallback;
    }
}
