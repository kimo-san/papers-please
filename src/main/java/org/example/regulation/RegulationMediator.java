package org.example.regulation;

import org.example.domain.Inspection;
import org.example.domain.Persona;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class RegulationMediator implements Regulation {

    List<Regulation> regulations;
    public RegulationMediator(Regulation... regulations) {
        this.regulations = Arrays.asList(regulations);
    }

    @Override
    public void update(String update) {
        for (Regulation reg : regulations) {
            reg.update(update);
        }
    }

    @Override
    public Inspection inspection(Persona persona) {
        Inspection inspection = new Inspection.Approved();

        for (Regulation reg : regulations) {
            inspection = reg.inspection(persona);
        }

        return inspection;
    }

    public List<Inspection> inspectAll(Persona persona) {
        List<Inspection> detected = new ArrayList<>();

        for (Regulation reg : regulations) {
            Inspection ins = reg.inspection(persona);
            if (!(ins instanceof Inspection.Approved)) {
                detected.add(ins);
            }
        }
        return detected.isEmpty() ? Collections.singletonList(new Inspection.Approved()) : detected;
    }

}
