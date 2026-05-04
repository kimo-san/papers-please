package org.example.regulation;

import org.example.domain.Document;
import org.example.domain.Inspection;
import org.example.domain.Persona;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class WorkerDocumentRegulation implements Regulation {
    private static final String SEPARATOR = ",\\s+";
    private static final String ALLOWED_DOCS_REGEX = Arrays
            .stream(Document.values())
            .map(Document::getValue)
            .map(Pattern::quote)
            .collect(Collectors.joining("|"));
    private static final String DOC_LIST = "((?:" + ALLOWED_DOCS_REGEX + ")(?:" + SEPARATOR + "(?:" + ALLOWED_DOCS_REGEX + "))*)";
    private static final Pattern WORKERS_PATTERN = Pattern.compile("^Workers require " + DOC_LIST + "$");

    private final Set<Document> workerRequirements = EnumSet.noneOf(Document.class);

    @Override
    public Inspection inspection(Persona persona) {
        if (!persona.isWorker()) {
            return new Inspection.Approved();
        }
        for (Document document : workerRequirements) {
            if (persona.isMissingDocument(document)) {
                return new Inspection.Denied("missing required " + document.getValue() + ".");
            }
        }

        return new Inspection.Approved();
    }

    @Override
    public void update(String update) {
        Matcher matcher = WORKERS_PATTERN.matcher(update);

        if (!matcher.matches()) {
            return;
        }

        workerRequirements.addAll(Arrays
                .stream(matcher.group(1).strip().split(SEPARATOR))
                .map(Document::fromValue)
                .collect(Collectors.toSet()));
    }
}