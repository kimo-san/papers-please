package org.example.regulation;

import org.example.domain.Document;
import org.example.domain.Inspection;
import org.example.domain.Nationality;
import org.example.domain.Persona;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class VaccinationRegulation implements Regulation {
    private static final String SEPARATOR = ",\\s+";
    private static final String ALLOWED_NATIONALITIES_REGEX = Arrays
            .stream(Nationality.values())
            .map(Nationality::getValue)
            .map(Pattern::quote)
            .collect(Collectors.joining("|"));

    private static final String NATIONALITY_LIST = "((?:" + ALLOWED_NATIONALITIES_REGEX + ")(?:" + SEPARATOR + "(?:" + ALLOWED_NATIONALITIES_REGEX + "))*)";

    private static final Pattern ENTRANTS_PATTERN = Pattern.compile("^Entrants require (.*) vaccination$");
    private static final Pattern FOREIGNERS_PATTERN = Pattern.compile("^Foreigners require (.*) vaccination$");
    private static final Pattern CITIZENS_PATTERN = Pattern.compile("^Citizens of " + NATIONALITY_LIST + " require (.*) vaccination$");

    private static final Pattern ENTRANTS_NO_LONGER_PATTERN = Pattern.compile("^Entrants no longer require (.*) vaccination$");
    private static final Pattern FOREIGNERS_NO_LONGER_PATTERN = Pattern.compile("^Foreigners no longer require (.*) vaccination$");
    private static final Pattern CITIZENS_NO_LONGER_PATTERN = Pattern.compile("^Citizens of " + NATIONALITY_LIST + " no longer require (.*) vaccination$");

    private final Set<String> entrantVaccineRequirements = new HashSet<>();
    private final Map<Nationality, Set<String>> citizenVaccineRequirements = new EnumMap<>(Nationality.class);

    @Override
    public Inspection inspection(Persona persona) {
        Set<String> requiredVaccines = new HashSet<>(entrantVaccineRequirements);

        persona.getNationality().ifPresent(nationality ->
                requiredVaccines.addAll(citizenVaccineRequirements.getOrDefault(nationality, Collections.emptySet()))
        );

        if (requiredVaccines.isEmpty()) {
            return new Inspection.Approved();
        }

        if (persona.isMissingDocument(Document.CERTIFICATE_OF_VACCINATION)) {
            return new Inspection.Denied("missing required " + Document.CERTIFICATE_OF_VACCINATION.getValue() + ".");
        }

        for (String requiredVaccine : requiredVaccines) {
            if (!persona.getVaccines().contains(requiredVaccine)) {
                return new Inspection.Denied("missing required vaccination.");
            }
        }

        return new Inspection.Approved();
    }

    @Override
    public void update(String update) {
        Matcher matcher;

        if ((matcher = ENTRANTS_PATTERN.matcher(update)).matches()) {
            entrantVaccineRequirements.add(matcher.group(1));
        } else if ((matcher = FOREIGNERS_PATTERN.matcher(update)).matches()) {
            addForCitizens(Nationality.foreign(), matcher.group(1));
        } else if ((matcher = CITIZENS_PATTERN.matcher(update)).matches()) {
            Set<Nationality> nationalities = parseNationalities(matcher.group(1));
            addForCitizens(nationalities, matcher.group(2));
        }

        else if ((matcher = ENTRANTS_NO_LONGER_PATTERN.matcher(update)).matches()) {
            entrantVaccineRequirements.remove(matcher.group(1));
        } else if ((matcher = FOREIGNERS_NO_LONGER_PATTERN.matcher(update)).matches()) {
            removeForCitizens(Nationality.foreign(), matcher.group(1));
        } else if ((matcher = CITIZENS_NO_LONGER_PATTERN.matcher(update)).matches()) {
            Set<Nationality> nationalities = parseNationalities(matcher.group(1));
            removeForCitizens(nationalities, matcher.group(2));
        }
    }

    private Set<Nationality> parseNationalities(String input) {
        return Arrays.stream(input.strip().split(SEPARATOR))
                .map(Nationality::fromValue)
                .collect(Collectors.toSet());
    }

    private void addForCitizens(Collection<Nationality> nationalities, String vaccine) {
        nationalities.forEach(nationality -> citizenVaccineRequirements
                .computeIfAbsent(nationality, k -> new HashSet<>())
                .add(vaccine));
    }

    private void removeForCitizens(Collection<Nationality> nationalities, String vaccine) {
        nationalities.forEach(nationality -> {
            Set<String> requirements = citizenVaccineRequirements.get(nationality);
            if (requirements != null) {
                requirements.remove(vaccine);
            }
        });
    }
}