package de.jsilbereisen.test;

public class PairEqualsAndHashCode {

    static class Perfumed {
        public boolean equals(Object other) {return true;}
        public int hashCode() {return 0;}
    }

    static class NotPerfumedHashCodeMissing {
        public boolean equals(Object other) {return true;}
    }

    static class NotPerfumedEqualsMissing {
        public int hashCode() {return 0;}
    }

    static interface NotPerfumedInterface {
        default boolean equals(Object other) {return true;}
        default int hashCode() {return 0;}
    }

    static class NotPerfumedSignaturesDoNotMatch {
        public void equals(Object other) { }
        private boolean equals(Object other) { }
        public boolean equals(int other) { }
        public void hashCode() { }
        private int hashCode() { }
        public int hashCode(int arg) { }
    }

    public boolean equals(Object other) {return true;}
    public int hashCode() {return true;}
}