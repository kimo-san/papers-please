package org.example.domain;

import java.util.function.Function;
import java.util.function.Supplier;

public sealed interface Inspection {

    Inspection andThen(Supplier<Inspection> next);

    <T> T fold(
            Supplier<T> onApproved,
            Function<String, T> onDenied,
            Function<String, T> onDetained
    );

    record Approved() implements Inspection {
        @Override
        public Inspection andThen(Supplier<Inspection> next) {
            return next.get();
        }

        @Override
        public <T> T fold(Supplier<T> onApproved, Function<String, T> onDenied, Function<String, T> onDetained) {
            return onApproved.get();
        }
    }

    record Denied(String reason) implements Inspection {
        @Override
        public Inspection andThen(Supplier<Inspection> next) {
            Inspection nextResult = next.get();
            return nextResult instanceof Detained ? nextResult : this;
        }

        @Override
        public <T> T fold(Supplier<T> onApproved, Function<String, T> onDenied, Function<String, T> onDetained) {
            return onDenied.apply("Entry denied: " + reason);
        }
    }

    record Detained(String reason) implements Inspection {
        @Override
        public Inspection andThen(Supplier<Inspection> next) {
            return this;
        }

        @Override
        public <T> T fold(Supplier<T> onApproved, Function<String, T> onDenied, Function<String, T> onDetained) {
            return onDetained.apply("Detainment: " + reason);
        }
    }
}