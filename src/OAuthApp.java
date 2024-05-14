import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.ProfileDetail;

public class OAuthApp extends Application {
    private static VBox layout = new VBox(10); // Maintain a layout VBox to be manipulated

    @Override
    public void start(Stage primaryStage) {
        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);

        Label clientIdLabel = new Label("Client ID:");
        TextField clientIdInput = new TextField();
        clientIdInput.setPromptText("Enter Client ID");

        Label clientSecretLabel = new Label("Client Secret:");
        TextField clientSecretInput = new TextField();
        clientSecretInput.setPromptText("Enter Client Secret");

        Button loginButton = new Button("Login");
        loginButton.setOnAction(e -> {
            String clientId = clientIdInput.getText();
            String clientSecret = clientSecretInput.getText();
            ProfileManagement.fetchProfile(clientId, clientSecret);
        });

        layout.getChildren().addAll(clientIdLabel, clientIdInput, clientSecretLabel, clientSecretInput, loginButton);
        grid.getChildren().add(layout);

        Scene scene = new Scene(grid, 300, 200);
        primaryStage.setTitle("OAuth Client Credentials Grant");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void updateUIWithProfileDetails(TableView<ProfileDetail> table, Label message) {
        Platform.runLater(() -> {
            layout.getChildren().clear();
            layout.getChildren().addAll(message, table);
        });
    }

    public static void displayErrorMessage(String message) {
        Platform.runLater(() -> {
            Label errorLabel = new Label(message);
            OAuthApp.layout.getChildren().clear();
            OAuthApp.layout.getChildren().add(errorLabel);
        });
    }


    public static void main(String[] args) {
        launch(args);
    }
}
