package org.example.domain;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Persona {
    private static final String LINE_SEPARATOR = "\\R+";
    private static final String ENTITY_SEPARATOR = ",\\s*";

    private static final Pattern FIELD_PATTERN = Pattern.compile("^(.*):\\s+(.*)$");

    private static final DateTimeFormatter EXP_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    private static final String EXPIRATION_RAW_KEY = "EXP";

    private static final Map<String, String> KEY_TRANSFORMS = Map.of(
            "ID#", "ID number",
            "NATION", "nationality"
    );

    private static final String NATIONALITY_KEY = "nationality";
    private static final String PURPOSE_KEY = "purpose";
    private static final String NAME_KEY = "name";
    private static final String ACCESS_KEY = "access";
    private static final String VACCINES_KEY = "vaccines";

    private static final String WORK_VALUE = "WORK";

    private final Map<Document, DocumentContent> documents = new EnumMap<>(Document.class);

    private final Optional<Nationality> nationality;
    private final boolean isForeigner;
    private final boolean isWorker;
    private final boolean hasAccessPermit;
    private final boolean hasValidAsylumGrant;
    private final boolean hasValidDiplomaticAuthorization;
    private final List<Name> names;
    private final List<String> mismatchFields;
    private final Set<String> vaccines;

    public Persona(Map<String, String> rawDocuments) {
        initDocuments(rawDocuments);

        this.nationality = computeNationality();
        this.isForeigner = this.nationality.map(nationality -> nationality != Nationality.ARSTOTZKA).orElse(true);
        this.isWorker = computeIsWorker();
        this.names = computeNames();
        this.mismatchFields = computeMismatchFields();
        this.hasAccessPermit = documents.containsKey(Document.ACCESS_PERMIT);
        this.hasValidAsylumGrant = documents.containsKey(Document.GRANT_OF_ASYLUM);
        this.hasValidDiplomaticAuthorization = isDocumentTargetingNation(Document.DIPLOMATIC_AUTHORIZATION, Nationality.ARSTOTZKA);
        this.vaccines = computeVaccines();
    }

    private void initDocuments(Map<String, String> rawDocuments) {
        for (var entry : rawDocuments.entrySet()) {
            Document type = Document.fromKey(entry.getKey());

            List<Row> rows = new ArrayList<>();
            LocalDate expiration = null;

            for (String row : entry.getValue().split(LINE_SEPARATOR)) {
                var matcher = FIELD_PATTERN.matcher(row);
                if (matcher.matches()) {
                    String rawKey = matcher.group(1);
                    String value = matcher.group(2);

                    if (EXPIRATION_RAW_KEY.equals(rawKey)) {
                        expiration = LocalDate.parse(value, EXP_DATE_FORMATTER);
                    } else {
                        rows.add(new Row(transformKey(rawKey), value));
                    }
                } else {
                    throw new IllegalArgumentException("Invalid row: " + row + " in document: " + type);
                }
            }
            this.documents.put(type, new DocumentContent(expiration, rows));
        }
    }

    private String transformKey(String rawKey) {
        return KEY_TRANSFORMS.getOrDefault(rawKey, rawKey.toLowerCase());
    }

    public boolean isMissingDocument(Document document) {
        return !documents.containsKey(document);
    }

    public boolean hasAccessPermit() {
        return hasAccessPermit;
    }

    public boolean hasValidAsylumGrant() {
        return hasValidAsylumGrant;
    }

    public boolean hasValidDiplomaticAuthorization() {
        return hasValidDiplomaticAuthorization;
    }

    private boolean isDocumentTargetingNation(Document document, Nationality expectedNation) {
        DocumentContent content = documents.get(document);
        if (content == null) return false;

        return content.rows().stream()
                .anyMatch(row -> ACCESS_KEY.equals(row.name()) &&
                        Arrays.asList(row.value().split(ENTITY_SEPARATOR)).contains(expectedNation.getValue()));
    }

    public List<Document> getExpiredDocuments(LocalDate currentDate) {
        return documents.entrySet().stream()
                .filter(entry -> {
                    LocalDate exp = entry.getValue().expiration();
                    return exp != null && exp.isBefore(currentDate);
                })
                .map(Map.Entry::getKey)
                .toList();
    }

    public List<String> getMismatchFields() {
        return mismatchFields;
    }

    public Optional<Nationality> getNationality() {
        return nationality;
    }

    public boolean isWorker() {
        return isWorker;
    }

    public List<Name> getNames() {
        return names;
    }

    public boolean isForeigner() {
        return isForeigner;
    }

    public Set<String> getVaccines() {
        return vaccines;
    }

    private Optional<Nationality> computeNationality() {
        return documents.values().stream()
                .flatMap(content -> content.rows().stream())
                .filter(row -> NATIONALITY_KEY.equals(row.name()))
                .map(Row::value)
                .map(Nationality::fromValue)
                .findFirst()
                .or(() -> documents.containsKey(Document.ID_CARD) ? Optional.of(Nationality.ARSTOTZKA) : Optional.empty());
    }

    private boolean computeIsWorker() {
        return Optional.ofNullable(documents.get(Document.ACCESS_PERMIT))
                .stream()
                .flatMap(content -> content.rows().stream())
                .anyMatch(row -> PURPOSE_KEY.equals(row.name()) && WORK_VALUE.equals(row.value()));
    }

    private List<Name> computeNames() {
        return documents.values().stream()
                .flatMap(content -> content.rows().stream())
                .filter(row -> NAME_KEY.equals(row.name()))
                .map(Row::value)
                .map(rawName -> Name.parse(rawName, Name.Format.LAST_COMMA_FIRST))
                .toList();
    }

    private List<String> computeMismatchFields() {
        return documents.values().stream()
                .flatMap(content -> content.rows().stream())
                .collect(Collectors.groupingBy(
                        Row::name,
                        Collectors.mapping(Row::value, Collectors.toSet())
                ))
                .entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .toList();
    }

    private Set<String> computeVaccines() {
        return documents.values().stream()
                .flatMap(content -> content.rows().stream())
                .filter(row -> VACCINES_KEY.equals(row.name()))
                .map(Row::value)
                .flatMap(value -> Arrays.stream(value.split(ENTITY_SEPARATOR)))
                .collect(Collectors.toSet());
    }

    private record DocumentContent(LocalDate expiration, List<Row> rows) {
    }

    private record Row(String name, String value) {
    }
}