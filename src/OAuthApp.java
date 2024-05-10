import config.Configuration;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class OAuthApp extends Application {
    public static VBox layout = new VBox(10);
    private final Button loginButton = new Button("Login with XPtrack");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        AuthCallbackServer.startServer();
        loginButton.setOnAction(e -> openBrowserForAuthentication());
        layout.getChildren().add(loginButton);
        primaryStage.setTitle("OAuth Demo");
        primaryStage.setScene(new Scene(layout, 300, 250));
        primaryStage.show();
    }

    private void openBrowserForAuthentication() {
        String url = "http://new4.xptrack.local/oauth/authorize?response_type=code&client_id=" +
                Configuration.getClientId() + "&redirect_uri=" + Configuration.getRedirectUri() + "&scope=";
        try {
            Runtime.getRuntime().exec(new String[]{"/usr/bin/xdg-open", url});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}