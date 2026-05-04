package org.example;

import org.example.domain.Persona;
import org.example.regulation.*;
import org.example.util.BulletinParser;
import org.example.util.BulletinParserImpl;

import java.util.*;
import java.util.function.Function;

public class Inspector {

    Regulation regulation = new CompositeRegulation(
            new WantedCriminalRegulation(),
            new MismatchRegulation(),
            new NationalityDocumentRegulation(),
            new WorkerDocumentRegulation(),
            new NationalityRegulation(),
            new ExpirationRegulation(),
            new VaccinationRegulation()
    );

    BulletinParser bulletinParser = new BulletinParserImpl();

    public void receiveBulletin(String bulletin) {
        regulation.update(bulletinParser.parse(bulletin));
    }

    public String inspect(Map<String, String> person) {
        var persona = new Persona(person);
        return regulation.inspection(persona)
                .fold(
                        () -> persona.isForeigner() ? "Cause no trouble." : "Glory to Arstotzka.",
                        Function.identity(),
                        Function.identity()
                );
    }

}
