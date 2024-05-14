import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.ProfileDetail;
import org.json.JSONException;
import org.json.JSONObject;

public class ProfileManagement {
    public static void fetchProfile(String clientId, String clientSecret) {
        HttpClient client = HttpClient.newHttpClient();

        JSONObject requestData = new JSONObject();
        requestData.put("grant_type", "client_credentials");
        requestData.put("client_id", clientId);
        requestData.put("client_secret", clientSecret);
        requestData.put("scope", "category:read");

        System.out.println("Request data: " + requestData);

        HttpRequest tokenRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://new4.xptrack.local/oauth/token"))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestData.toString(), StandardCharsets.UTF_8))
                .build();

        client.sendAsync(tokenRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(body -> {
                    JSONObject json = new JSONObject(body);
                    System.out.println("Response data: " + json);
                    if (!json.has("access_token")) {
                        throw new RuntimeException("Failed to authenticate: " + body);
                    }
                    return json.getString("access_token");
                })
                .thenAccept(accessToken -> fetchProfileDetails(accessToken))
                .exceptionally(e -> {
                    Platform.runLater(() -> OAuthApp.displayErrorMessage("Failed to fetch token: " + e.getMessage()));
                    return null;
                });
    }

    private static void fetchProfileDetails(String accessToken) {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest profileRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://new4.xptrack.local/api/v2/profile"))
                .header("Authorization", "Bearer " + accessToken)
                .build();

        client.sendAsync(profileRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(profileInfo -> Platform.runLater(() -> {
                    System.out.println("Profile info: " + profileInfo);
                    try {
                        JSONObject json = new JSONObject(profileInfo);
                        setupProfileTable(json);
                    } catch (JSONException e) {
                        OAuthApp.displayErrorMessage("Invalid JSON format: " + e.getMessage());
                    }
                }))
                .exceptionally(e -> {
                    System.err.println("Failed to fetch profile: " + e.getMessage());
                    Platform.runLater(() -> OAuthApp.displayErrorMessage("Error loading profile: " + e.getMessage()));
                    return null;
                });
    }

    private static void setupProfileTable(JSONObject json) {
        TableView<ProfileDetail> table = new TableView<>();
        ObservableList<ProfileDetail> data = FXCollections.observableArrayList(
                new ProfileDetail("First Name", json.optString("first_name")),
                new ProfileDetail("Last Name", json.optString("last_name")),
                new ProfileDetail("Email", json.optString("email")),
                new ProfileDetail("Login ID", json.optString("login")),
                new ProfileDetail("User Type", json.optString("user_type"))
        );

        TableColumn<ProfileDetail, String> column1 = new TableColumn<>("Property");
        column1.setCellValueFactory(new PropertyValueFactory<>("key"));
        TableColumn<ProfileDetail, String> column2 = new TableColumn<>("Value");
        column2.setCellValueFactory(new PropertyValueFactory<>("value"));

        table.getColumns().addAll(column1, column2);
        table.setItems(data);

        Label successMessage = new Label("Profile information fetched successfully");
        OAuthApp.updateUIWithProfileDetails(table, successMessage);
    }
}
