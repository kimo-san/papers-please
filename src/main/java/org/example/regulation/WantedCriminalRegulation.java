package org.example.regulation;

import org.example.domain.Inspection;
import org.example.domain.Name;
import org.example.domain.Persona;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class WantedCriminalRegulation implements Regulation {
    private static final Pattern WANTED_BY_PATTERN = Pattern.compile("^Wanted by .+: (.+)$");
    private final Set<Name> wantedCriminalNames = new HashSet<>();

    @Override
    public Inspection inspection(Persona persona) {
        boolean isWanted = !Collections.disjoint(persona.getNames(), wantedCriminalNames);

        if (isWanted) {
            return new Inspection.Detained("Entrant is a wanted criminal.");
        }

        return new Inspection.Approved();
    }

    @Override
    public void update(String update) {
        wantedCriminalNames.clear();
        updateWantedList(update);
    }

    @Override
    public void update(List<String> updates) {
        wantedCriminalNames.clear();
        updates.forEach(this::updateWantedList);
    }

    private void updateWantedList(String update) {
        Matcher matcher = WANTED_BY_PATTERN.matcher(update);
        if (matcher.matches()) {
            wantedCriminalNames.add(Name.parse(matcher.group(1), Name.Format.FIRST_SPACE_LAST));
        }
    }
}