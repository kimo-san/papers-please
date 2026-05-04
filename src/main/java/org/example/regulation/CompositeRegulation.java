package org.example.regulation;

import org.example.domain.Inspection;
import org.example.domain.Persona;

import java.util.Arrays;
import java.util.List;

public final class CompositeRegulation implements Regulation {
    private final List<Regulation> regulations;

    public CompositeRegulation(Regulation... regulations) {
        this.regulations = Arrays.asList(regulations);
    }

    @Override
    public Inspection inspection(Persona persona) {
        Inspection finalResult = new Inspection.Approved();

        for (Regulation regulation : regulations) {
            finalResult = finalResult.andThen(() -> regulation.inspection(persona));
        }

        return finalResult;
    }

    @Override
    public void update(String update) {
        regulations.forEach(regulation -> regulation.update(update));
    }

    @Override
    public void update(List<String> updates) {
        regulations.forEach(regulation -> regulation.update(updates));
    }
}
