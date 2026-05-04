package org.example.regulation;

import org.example.domain.Document;
import org.example.domain.Inspection;
import org.example.domain.Nationality;
import org.example.domain.Persona;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class NationalityDocumentRegulation implements Regulation {
    private static final String SEPARATOR = ",\\s+";
    private static final String ALLOWED_DOCS_REGEX = Arrays
            .stream(Document.values())
            .map(Document::getValue)
            .map(Pattern::quote)
            .collect(Collectors.joining("|"));

    private static final String DOC_LIST = "((?:" + ALLOWED_DOCS_REGEX + ")(?:" + SEPARATOR + "(?:" + ALLOWED_DOCS_REGEX + "))*)";
    private static final String ALLOWED_NATIONALITIES_REGEX = Arrays
            .stream(Nationality.values())
            .map(Nationality::getValue)
            .map(Pattern::quote)
            .collect(Collectors.joining("|"));

    private static final String NATIONALITY_LIST = "((?:" + ALLOWED_NATIONALITIES_REGEX + ")(?:" + SEPARATOR + "(?:" + ALLOWED_NATIONALITIES_REGEX + "))*)";

    private static final Pattern ENTRANTS_PATTERN = Pattern.compile("^Entrants require " + DOC_LIST + "$");
    private static final Pattern FOREIGNERS_PATTERN = Pattern.compile("^Foreigners require " + DOC_LIST + "$");
    private static final Pattern CITIZENS_PATTERN = Pattern.compile("^Citizens of " + NATIONALITY_LIST + " require " + DOC_LIST + "$");

    private final Set<Document> entrantRequirements = EnumSet.noneOf(Document.class);
    private final Map<Nationality, Set<Document>> citizenRequirements = new EnumMap<>(Nationality.class);

    @Override
    public Inspection inspection(Persona persona) {
        Set<Document> required = EnumSet.noneOf(Document.class);
        required.addAll(entrantRequirements);

        persona.getNationality().ifPresent(nationality ->
                required.addAll(citizenRequirements.getOrDefault(nationality, Collections.emptySet()))
        );

        for (Document document : required) {
            if (document == Document.ACCESS_PERMIT) {
                if (persona.hasAccessPermit() || persona.hasValidAsylumGrant()) {
                    continue;
                }

                if (!persona.isMissingDocument(Document.DIPLOMATIC_AUTHORIZATION)) {
                    if (persona.hasValidDiplomaticAuthorization()) {
                        continue;
                    } else {
                        return new Inspection.Denied("invalid diplomatic authorization.");
                    }
                }
            }

            if (persona.isMissingDocument(document)) {
                return new Inspection.Denied("missing required " + document.getValue() + ".");
            }
        }

        return new Inspection.Approved();
    }

    @Override
    public void update(String update) {
        Matcher matcher;

        if ((matcher = ENTRANTS_PATTERN.matcher(update)).matches()) {
            entrantRequirements.addAll(parseDocs(matcher.group(1)));
        } else if ((matcher = FOREIGNERS_PATTERN.matcher(update)).matches()) {
            addForCitizens(Nationality.foreign(), parseDocs(matcher.group(1)));
        } else if ((matcher = CITIZENS_PATTERN.matcher(update)).matches()) {
            Set<Nationality> nationalities = Arrays.stream(matcher.group(1).strip().split(SEPARATOR))
                    .map(Nationality::fromValue)
                    .collect(Collectors.toSet());
            addForCitizens(nationalities, parseDocs(matcher.group(2)));
        }
    }

    private Set<Document> parseDocs(String input) {
        return Arrays.stream(input.strip().split(SEPARATOR))
                .map(Document::fromValue)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(Document.class)));
    }

    private void addForCitizens(Collection<Nationality> nationalities, Collection<Document> documents) {
        nationalities.forEach(nationality -> citizenRequirements
                .computeIfAbsent(nationality, k -> EnumSet.noneOf(Document.class))
                .addAll(documents));
    }
}