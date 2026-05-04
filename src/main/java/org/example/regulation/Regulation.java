package org.example.regulation;

import org.example.domain.Inspection;
import org.example.domain.Persona;

import java.util.List;

public interface Regulation {
    Inspection inspection(Persona persona);

    void update(String update);

    default void update(List<String> updates) {
        updates.forEach(this::update);
    }
}