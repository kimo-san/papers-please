package org.example.domain;

public enum Document {
    PASSPORT("passport", "passport"),
    CERTIFICATE_OF_VACCINATION("certificate of vaccination", "certificate_of_vaccination"),
    ID_CARD("ID card", "ID_card"),
    ACCESS_PERMIT("access permit", "access_permit"),
    WORK_PASS("work pass", "work_pass"),
    GRANT_OF_ASYLUM("grant of asylum", "grant_of_asylum"),
    DIPLOMATIC_AUTHORIZATION("diplomatic authorization", "diplomatic_authorization");

    private final String value;
    private final String key;

    Document(String value, String key) {
        this.value = value;
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public static Document fromValue(String value) {
        for (Document document : values()) {
            if (document.value.equals(value)) return document;
        }
        throw new IllegalArgumentException("Invalid value: " + value);
    }

    public static Document fromKey(String key) {
        for (Document document : values()) {
            if (document.key.equals(key)) return document;
        }
        throw new IllegalArgumentException("Invalid key: " + key);
    }
}
