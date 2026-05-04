package org.example;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        var inspector = new Inspector();
        inspector.receiveBulletin("""
                Entrants require passport
                Allow citizens of Arstotzka, Obristan
                Wanted by the State: Hubert Popovic
                """);

        Map<String, String> josef = new HashMap<>();
        josef.put("passport", """
                ID#: GC07D-FU8AR
                NATION: Arstotzka
                NAME: Costanza, Josef
                DOB: 1933.11.28
                SEX: M
                ISS: East Grestin
                EXP: 1983.03.15
                """);
        System.out.println("josef: " + inspector.inspect(josef));

        Map<String, String> guyovich = new HashMap<>();
        guyovich.put("access_permit", """
                NAME: Guyovich, Russian
                NATION: Obristan
                ID#: TE8M1-V3N7R
                PURPOSE: TRANSIT
                DURATION: 14 DAYS
                HEIGHT: 159cm
                WEIGHT: 60kg
                EXP: 1983.07.13
                """);
        System.out.println("guyovich: " + inspector.inspect(guyovich));

        Map<String, String> roman = new HashMap<>();
        roman.put("passport", """
                ID#: WK9XA-LKM0Q
                NATION: United Federation
                NAME: Dolanski, Roman
                DOB: 1933.01.01
                SEX: M
                ISS: Shingleton
                EXP: 1983.05.12
                """);
        roman.put("grant_of_asylum", """
                NAME: Popovic, Hubert
                NATION: United Federation
                ID#: WK9XA-LKM0Q
                DOB: 1933.01.01
                HEIGHT: 176cm
                WEIGHT: 71kg
                EXP: 1983.09.20
                """);
        System.out.println("roman: " + inspector.inspect(roman));
    }
}