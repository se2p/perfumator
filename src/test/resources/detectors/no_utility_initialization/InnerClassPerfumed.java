package de.jsilbereisen.perfumator.test;

/**
 * Here, 2 instance of the "No Utility initialization" Perfume should be detected.
 * Reasons are given on the classes.
 */
public class WithInnerClass {

    // Does not allow instantiation, only static methods => perfumed
    public class PerfumedInnerClass {

        private PerfumedInnerClass() { }

        public static void staticMethod() { }

    }

    public static class PerfumedStaticInnerClass {

        private PerfumedStaticInnerClass() { }

        public static void staticMethod() { }
    }

    // Does allow instantiation => not perfumed
    public class NotPerfumedInnerClass {

        public static void staticMethod() { }
    }

    // Does allow instantiation => not perfumed; static modifier has no influence, just no instance of outer class
    // required
    public static class NotPerfumedStaticInnerClass {

        public static void staticMethod() { }
    }

    // Not all Methods static => not perfumed; static modifier has no influence, just no instance of outer class
    // required
    public static class NotPerfumedNonStaticMethodsInnerClass {

        private NotPerfumedNonStaticMethodsInnerClass() { }

        public static void staticMethod() { }

        public void nonStaticMethod() { }
    }

    // Not perfumed, as it is an interface type
    public interface NotPerfumedInterface {

        static void interfaceMethod() {
            // Must have body
        }
    }

    // Hinders outer class from being perfumed
    public static void staticMethod() { }
}