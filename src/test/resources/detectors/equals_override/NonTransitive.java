package de.jsilbereisen.test;

import dependency.*;

public class NonTransitive {

    static class Perfumed extends AncestorThatOverridesEquals {

        int additionalField;

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }

    static class NotPerfumed extends AncestorThatOverridesEquals {

        static int staticField; // Should be ignored => doesnt need to override equals necessarily

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }

    static class NotPerfumed2 extends ClassThatDoesntOverrideEquals {

        int additionalField;

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }
}