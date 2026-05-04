package org.example.util;

import java.util.List;

public class BulletinParserImpl implements BulletinParser {
    @Override
    public List<String> parse(String bulletin) {
        if (bulletin == null || bulletin.isBlank()) {
            return List.of();
        }

        return bulletin.lines()
                .filter(line -> !line.isBlank())
                .map(String::trim)
                .toList();
    }
}
