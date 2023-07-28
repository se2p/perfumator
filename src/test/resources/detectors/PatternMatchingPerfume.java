package de.jsilbereisen.test;

/**
 * JavaParser does not support Pattern-Matching in switch-expressions, as it only supports Language features up to
 * Java 17 (switch pattern matching is only preview in Java 17).
 */
public class PatternMatchingPerfume {

    public static class Perfumed {
        void method(Object o) {
            if (o instanceof String s) {
                // Perfumed
            }
        }
    }

    public static class NotPerfumed {
        void method(Object o) {
            if (o instanceof String) {
                String s = (String) o;
            }
        }
    }
}