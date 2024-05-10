package model;

import javafx.beans.property.SimpleStringProperty;

public class ProfileDetail {
    private final SimpleStringProperty key;
    private final SimpleStringProperty value;

    public ProfileDetail(String key, String value) {
        this.key = new SimpleStringProperty(key);
        this.value = new SimpleStringProperty(value);
    }

    public String getKey() { return key.get(); }
    public String getValue() { return value.get(); }
}

