package org.example.regulation;

import org.example.domain.Document;
import org.example.domain.Inspection;
import org.example.domain.Persona;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;

public final class ExpirationRegulation implements Regulation {
    private static final LocalDate CURRENT_DATE = LocalDate.of(1982, Month.NOVEMBER, 22);

    @Override
    public Inspection inspection(Persona persona) {
        List<Document> expiredDocs = persona.getExpiredDocuments(CURRENT_DATE);

        if (expiredDocs.isEmpty()) {
            return new Inspection.Approved();
        }

        return new Inspection.Denied(expiredDocs.get(0) + " expired.");
    }

    @Override
    public void update(String update) {
    }
}