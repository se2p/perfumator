package de.jsilbereisen.test;

/**
 * No perfume should be detected, because ancestors of classes can't be resolved (missing imports).
 */
public class DependenciesMissing {

    int field;

    static class Perfumed extends IntermediateClass {

        int additionalField;

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }

    static class Perfumed2 extends AncestorThatOverridesEquals {

        int additionalField;

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }

    static class NotPerfumedNonTransitive extends AncestorThatOverridesEquals {

        static int staticField; // Should be ignored => doesnt need to override equals necessarily

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }

    static class NotPerfumedNonTransitive2 extends ClassThatDoesntOverrideEquals {

        int additionalField;

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }

    static class NotPerfumedTransitive extends IntermediateClass {

        static int staticField;

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }

    static class NotPerfumedNoEqualsOverrideTransitive extends IntermediateClass {

        int additionalField;

        // Does not override equals!
        public int equals(Object obj) {
            return super.equals(obj);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}