import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import manager.TokenManager;
import model.ProfileDetail;
import org.json.JSONObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ProfileManagement {
    public static void fetchProfile() {
        // Extract access token from tokenResponse
        String accessToken = TokenManager.getAccessToken();  // Ensure this method does not throw an unchecked exception
        System.out.println("Access token: " + accessToken);
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest profileRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://new4.xptrack.local/api/v1/profile"))
                .header("Authorization", "Bearer " + accessToken)
                .build();

        client.sendAsync(profileRequest, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(profileInfo -> Platform.runLater(() -> {
                    JSONObject json = new JSONObject(profileInfo);
                    setupProfileTable(json);
                }))
                .exceptionally(e -> {
                    System.err.println("Failed to fetch profile: " + e.getMessage());
                    Platform.runLater(() -> displayErrorMessage("Error loading profile."));
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

        OAuthApp.layout.getChildren().clear();
        OAuthApp.layout.getChildren().addAll(successMessage, table);
    }

    private static void displayErrorMessage(String message) {
        OAuthApp.layout.getChildren().clear();
        OAuthApp.layout.getChildren().add(new Label(message));
    }
}
