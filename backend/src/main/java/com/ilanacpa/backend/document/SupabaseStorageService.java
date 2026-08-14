package com.ilanacpa.backend.document;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@EnableConfigurationProperties(SupabaseStorageProperties.class)
public class SupabaseStorageService {

    private final SupabaseStorageProperties properties;
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper;

    public SupabaseStorageService(SupabaseStorageProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public String bucket() {
        return properties.storageBucket();
    }

    public void upload(String objectPath, byte[] content, String contentType) {
        String url = properties.url() + "/storage/v1/object/" + properties.storageBucket() + "/" + objectPath;
        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", "Bearer " + properties.serviceRoleKey())
                .header("apikey", properties.serviceRoleKey())
                .header("Content-Type", contentType != null ? contentType : "application/octet-stream")
                .header("x-upsert", "true")
                .POST(HttpRequest.BodyPublishers.ofByteArray(content))
                .build();
        send(request, "upload");
    }

    public String createSignedDownloadUrl(String objectPath, int expiresInSeconds) {
        String url = properties.url() + "/storage/v1/object/sign/" + properties.storageBucket() + "/" + objectPath;
        String body = objectMapper.writeValueAsString(Map.of("expiresIn", expiresInSeconds));

        HttpRequest request = HttpRequest.newBuilder(URI.create(url))
                .header("Authorization", "Bearer " + properties.serviceRoleKey())
                .header("apikey", properties.serviceRoleKey())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
        String responseBody = send(request, "sign");

        Map<?, ?> parsed = objectMapper.readValue(responseBody, Map.class);
        String signedPath = (String) parsed.get("signedURL");
        return properties.url() + "/storage/v1" + signedPath;
    }

    private String send(HttpRequest request, String action) {
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 300) {
                throw new IllegalStateException(
                        "Supabase storage " + action + " failed: " + response.statusCode() + " " + response.body());
            }
            return response.body();
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new IllegalStateException("Supabase storage " + action + " request failed", e);
        }
    }
}
