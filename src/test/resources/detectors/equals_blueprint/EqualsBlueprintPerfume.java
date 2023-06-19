package de.jsilbereisen.test;

/**
 * Tests detection of the "Equals Blueprint" Perfume. It checks taking the first two necessary steps for implementing
 * the contract of the "equals" method in {@link Object}, namely checking for equality of the object-references
 * and checking for type-equality at runtime.<br/>
 * As nested if-statements should be preferable avoided, it is <b>not</b> seen as perfumed
 * when e.g. checking for {@code this != other} and returning in the else-branch,
 * as it adds unnecessary nesting of if-statements.
 */
public class EqualsBlueprintPerfume {

    private int field;

    // perfumed
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (!(other instanceof EqualsBlueprintPerfume)) {
            return false;
        }

        EqualsBlueprintPerfume otherPerf = (EqualsBlueprintPerfume) other;

        return field == ebp.field;
    }

    // Should be detected with correct Type name and line nums in the DetectedInstance
    class ShouldBeDetected {
        public boolean equals(Object param) {
            if (param == this) {
                return true;
            }
            if (!(param instanceof ShouldBeDetected)) {
                return false;
            }

            ShouldBeDetected otherPerf = (ShouldBeDetected) param;

            return field == ebp.field;
        }
    }

    // Should NOT be detected, false return value in the reference check
    class Inner {
        public boolean equals(Object other) {
            if (this == other) {
                return false;
            }

            if (!(other instanceof Inner)) {
                return false;
            }

            Inner inner = (Inner) other;

            return true;
        }
    }

    // Should NOT be detected, false return value in the instanceof check
    class Inner2 {
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof Inner2)) {
                return true;
            }

            Inner2 inner = (Inner2) other;

            return true;
        }
    }

    // Should NOT be detected, cast is missing
    class Inner3 {
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof Inner3)) {
                return false;
            }

            return true;
        }
    }

    // Should NOT be detected, instanceof checks for the wrong type
    class Inner4 {
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }

            if (!(other instanceof Inner3)) {
                return false;
            }

            Inner4 inner = (Inner4) o;
            return true;
        }
    }

    // Should NOT be detected, casts to the wrong type
    class Inner5 {
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof Inner5)) {
                return false;
            }

            Inner5 inner = (Inner4) other;
            return true;
        }
    }

    // Should NOT be detected, casts to the wrong type
    class Inner6 {
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof Inner6)) {
                return false;
            }

            Inner5 inner = (Inner6) other;
            return true;
        }
    }

    // Should NOT be detected, uses unknown variable in reference check
    class Inner7 {
        public boolean equals(Object other) {
            if (this == unknown) {
                return true;
            }

            if (!(other instanceof Inner7)) {
                return false;
            }

            Inner7 inner = (Inner7) other;
            return true;
        }
    }

    // Should NOT be detected, uses unknown variable in instanceof check
    class Inner8 {
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (!(unknown instanceof Inner8)) {
                return false;
            }

            Inner8 inner = (Inner8) other;
            return true;
        }
    }

    // Should NOT be detected, uses unknown variable in cast
    class Inner9 {
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }

            if (!(other instanceof Inner9)) {
                return false;
            }

            Inner9 inner = (Inner9) unknown;
            return true;
        }
    }

}