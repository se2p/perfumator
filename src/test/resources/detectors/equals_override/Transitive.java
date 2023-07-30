package de.jsilbereisen.test;

import dependency.*;

public class Transitive {

    static class Perfumed extends IntermediateClass {

        int additionalField;

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }

    static class NotPerfumed extends IntermediateClass {

        static int staticField; // Should be ignored => doesnt need to override equals necessarily

        @Override
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }

    static class NotPerfumedNoEqualsOverride extends IntermediateClass {

        int additionalField;

        // Does not override equals!
        public int equals(Object obj) {
            return super.equals(obj);
        }
    }

}