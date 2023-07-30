package de.jsilbereisen.test;

import dependency.SomeComparable;

public class CompareToAndEqualsOverridePerfume {

    static class Perfumed implements Comparable<Perfumed> {
        @Override
        public int compareTo(Perfumed o) {
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }

    static class PerfumedTransitive extends SomeComparable {
        @Override
        public int compareTo(SomeComparable o) {
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }

    static class NotPerfumedNoInterface {
        public int compareTo(NotPerfumedNoInterface o) {
            return 0;
        }

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }

    static class NotPerfumedOnlyCompareTo implements Comparable<NotPerfumedOnlyCompareTo> {
        @Override
        public int compareTo(NotPerfumedOnlyCompareTo o) {
            return 0;
        }
    }

    static class NotPerfumedOnlyEquals implements Comparable<NotPerfumedOnlyEquals> {
        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }
}