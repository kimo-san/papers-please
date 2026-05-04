package org.example.regulation;

import org.example.domain.Inspection;
import org.example.domain.Persona;

import java.util.List;

public final class MismatchRegulation implements Regulation {

    @Override
    public Inspection inspection(Persona persona) {
        List<String> mismatchedFields = persona.getMismatchFields();

        if (mismatchedFields.isEmpty()) {
            return new Inspection.Approved();
        }

        return new Inspection.Detained(mismatchedFields.get(0) + " mismatch.");
    }

    @Override
    public void update(String update) {
    }
}