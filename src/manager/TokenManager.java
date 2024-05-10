package manager;

import config.Configuration;
import javafx.application.Platform;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.prefs.Preferences;

public class TokenManager {
    private static Preferences prefs = Preferences.userNodeForPackage(TokenManager.class);

    public static void saveTokens(String accessToken, String refreshToken) {
        prefs.put("ACCESS_TOKEN", accessToken);
        prefs.put("REFRESH_TOKEN", refreshToken);
    }

    public static String getAccessToken() {
        return prefs.get("ACCESS_TOKEN", null);
    }

    public static String getRefreshToken() {
        return prefs.get("REFRESH_TOKEN", null);
    }

    public static void clearTokens() {
        prefs.remove("ACCESS_TOKEN");
        prefs.remove("REFRESH_TOKEN");
    }

    public static CompletableFuture<String> makeAuthenticatedRequest(String uri) {
        String accessToken = TokenManager.getAccessToken();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .header("Authorization", "Bearer " + accessToken)
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .exceptionally(e -> {
                    System.err.println("Failed request: " + e.getMessage());
                    return "Error";
                });
    }

    public static void refreshToken() {
        String refreshToken = TokenManager.getRefreshToken();

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://new4.xptrack.local/oauth/token"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString(
                        "grant_type=refresh_token&refresh_token=" + refreshToken +
                                "&client_id=" + Configuration.getClientId() + "&client_secret=" + Configuration.getClientSecret()))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(body -> {
                    JSONObject json = new JSONObject(body);
                    if (json.has("access_token")) {
                        String newAccessToken = json.getString("access_token");
                        String newRefreshToken = json.optString("refresh_token", refreshToken); // Use old refresh token if not updated
                        TokenManager.saveTokens(newAccessToken, newRefreshToken);
                    } else {
                        System.err.println("Failed to refresh token: " + body);
                        Platform.runLater(() -> {
                            // Notify user or re-initiate login
                        });
                    }
                })
                .join(); // Consider handling this asynchronously
    }


}
