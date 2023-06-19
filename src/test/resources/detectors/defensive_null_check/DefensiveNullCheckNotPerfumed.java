package de.jsilbereisen.test;

import org.jetbrains.annotations.NotNull;

/**
 * Test detection of the "Defensive Null Check" Perfume.
 * This test file contains only cases that should <b>not</b> be detected.
 */
public class DefensiveNullCheckNotPerfumed {

    // not public
    void nonPublicMethod(@NotNull Object param) { }

    // no parameters
    public void noParams() { }

    public abstract class A {
        // abstract
        public abstract void abstractMethod(@NotNull Object o);
    }

    public interface B {
        // not default in interface
        public int nonDefault(@NotNull Object o);

        // private in interface
        private default void privateInInterface(@NotNull Object o) { }
    }

    // One check missing
    public void notAllChecked(@NotNull Object a, Object b) {
        doSomething();
    }

    // Varargs parameters are ignored
    public void varArgs(@NotNull Object... o) { }

    // Only primitives
    public int onlyPrimitives(int x, byte b, boolean flag) {
        noChecksNeeded();
    }

    public int noParameters() { }
}