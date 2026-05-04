package org.example.domain;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

public enum Nationality {
    ARSTOTZKA("Arstotzka"),
    ANTEGRIA("Antegria"),
    IMPOR("Impor"),
    KOLECHIA("Kolechia"),
    OBRISTAN("Obristan"),
    REPUBLIA("Republia"),
    UNITED_FEDERATION("United Federation");

    private final String value;

    Nationality(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Nationality fromValue(String value) {
        for (Nationality nationality : values()) {
            if (nationality.value.equals(value)) return nationality;
        }
        throw new IllegalArgumentException("Invalid value: " + value);
    }

    public static Set<Nationality> foreign() {
        return Arrays
                .stream(values())
                .filter(nationality -> nationality != ARSTOTZKA)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Nationality.class)));
    }
}
