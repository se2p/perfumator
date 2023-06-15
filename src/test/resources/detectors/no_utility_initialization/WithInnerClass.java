package de.jsilbereisen.perfumator.test;

/**
 * Here, an instance of the "No Utility initialization" Perfume should be detected,
 * as the class only declares static methods and explicitely prevents instanciation
 * by declaring a single private constructor.<br/>
 * Especially, the detection should not be influenced by the declared inner type.
 */
public class WithInnerClass {

    public class IgnoreMe {

        public IgnoreMe() { }

        public void nonStaticMethod() { }

    }

    private WithInnerClass() { }

    public static void staticMethod() { }
}