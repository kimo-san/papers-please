package org.example.regulation;

import org.example.domain.Inspection;
import org.example.domain.Nationality;
import org.example.domain.Persona;

import java.util.*;
import java.util.stream.Collectors;

public final class NationalityRegulation implements Regulation {
    private final static String ALLOW_PREFIX = "Allow citizens of ";
    private final static String DENY_PREFIX = "Deny citizens of ";
    private final static String SEPARATOR = ",\\s+";

    private final Set<Nationality> allowedNationalities = EnumSet.noneOf(Nationality.class);

    @Override
    public Inspection inspection(Persona persona) {
        if (!persona.getNationality().map(allowedNationalities::contains).orElse(false)) {
            return new Inspection.Denied("citizen of banned nation.");
        }

        return new Inspection.Approved();
    }

    @Override
    public void update(String update) {
        boolean isAllow = update.startsWith(ALLOW_PREFIX);
        boolean isDeny = update.startsWith(DENY_PREFIX);

        if (!isAllow && !isDeny) return;

        String prefix = isAllow ? ALLOW_PREFIX : DENY_PREFIX;

        Set<Nationality> nationalities = Arrays
                .stream(update.substring(prefix.length()).strip().split(SEPARATOR))
                .map(Nationality::fromValue)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Nationality.class)));

        if (isAllow) {
            allowedNationalities.addAll(nationalities);
        } else {
            allowedNationalities.removeAll(nationalities);
        }
    }
}