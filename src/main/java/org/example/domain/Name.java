package org.example.domain;

public record Name(String firstName, String lastName) {

    public enum Format {
        LAST_COMMA_FIRST,
        FIRST_SPACE_LAST
    }

    public static Name parse(String rawName, Format format) {
        if (rawName == null || rawName.isBlank()) {
            throw new IllegalArgumentException("Raw name cannot be null or blank");
        }

        return switch (format) {
            case LAST_COMMA_FIRST -> {
                String[] parts = rawName.split(",", 2);
                if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
                    throw new IllegalArgumentException("Invalid LAST_COMMA_FIRST format. Expected 'LastName, FirstName', got: '" + rawName + "'");
                }
                yield new Name(parts[1].strip(), parts[0].strip());
            }
            case FIRST_SPACE_LAST -> {
                String[] parts = rawName.strip().split("\\s+", 2);
                if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank()) {
                    throw new IllegalArgumentException("Invalid FIRST_SPACE_LAST format. Expected 'FirstName LastName', got: '" + rawName + "'");
                }
                yield new Name(parts[0].strip(), parts[1].strip());
            }
        };
    }
}