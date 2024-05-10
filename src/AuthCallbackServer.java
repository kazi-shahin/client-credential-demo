import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import config.Configuration;
import io.github.cdimascio.dotenv.Dotenv;
import manager.TokenManager;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;


import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AuthCallbackServer {

    public static void startServer() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(6661), 0);
            server.createContext("/callback", new AuthHandler());
            server.setExecutor(null); // creates a default executor
            server.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static class AuthHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String query = exchange.getRequestURI().getQuery();
            String code = queryToMap(query).get("code");

            if (code != null) {
                CompletableFuture<String> future = fetchTokens(code);
                future.thenAccept(response -> {
                    sendResponse(exchange, "Authentication complete. You may close this window.", 200);
                    ProfileManagement.fetchProfile();  // Assuming fetchProfile parses the response and updates UI
                }).exceptionally(e -> {
                    // Log the exception and only send an error response if no response has been sent yet
                    if (!exchange.getResponseHeaders().isEmpty()) {
                        System.err.println("An error occurred, but response was already sent: " + e.getMessage());
                    } else {
                        sendResponse(exchange, e.getMessage(), 400);  // Send error message back to client
                    }
                    return null;
                });
            } else {
                sendResponse(exchange, "No authorization code provided.", 400);
            }
        }

        private CompletableFuture<String> fetchTokens(String code) {
            String clientId = Configuration.getClientId();
            String clientSecret = Configuration.getClientSecret();
            String redirectUri = Configuration.getRedirectUri();

            HttpClient client = HttpClient.newHttpClient();
            JSONObject json = new JSONObject();
            json.put("grant_type", "authorization_code");
            json.put("code", code);
            json.put("client_id", clientId);
            json.put("client_secret", clientSecret);
            json.put("redirect_uri", redirectUri);
            json.put("scope", ""); // Add your scope here if needed

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://new4.xptrack.local/oauth/token"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.toString(), StandardCharsets.UTF_8))
                    .build();

            return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenApply(body -> {
                        JSONObject responseBody = new JSONObject(body);
                        if (responseBody.has("access_token")) {
                            String accessToken = responseBody.getString("access_token");
                            String refreshToken = responseBody.optString("refresh_token", "");  // Use empty string if not present
                            TokenManager.saveTokens(accessToken, refreshToken);
                            return accessToken;  // Continue with valid response
                        } else {
                            throw new RuntimeException("Failed to authenticate: " + body);
                        }
                    });
        }


        private void sendResponse(HttpExchange exchange, String response, int statusCode) {
            try {
                exchange.sendResponseHeaders(statusCode, response.getBytes().length);
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                exchange.close();
            }
        }

        private Map<String, String> queryToMap(String query) {
            Map<String, String> result = new HashMap<>();
            for (String param : query.split("&")) {
                String[] entry = param.split("=");
                if (entry.length > 1) {
                    result.put(entry[0], entry[1]);
                } else {
                    result.put(entry[0], "");
                }
            }
            return result;
        }
    }


}
