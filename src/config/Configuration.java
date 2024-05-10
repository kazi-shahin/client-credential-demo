package config;

import io.github.cdimascio.dotenv.Dotenv;

public class Configuration {
    private static final Dotenv dotenv = Dotenv.load();

    public static String getClientId() {
        return dotenv.get("OAUTH_CLIENT_ID");
    }

    public static String getRedirectUri() {
        return dotenv.get("OAUTH_REDIRECT_URI");
    }

    public static String getClientSecret() {
        return dotenv.get("OAUTH_CLIENT_SECRET");
    }
}