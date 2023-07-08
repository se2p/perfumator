package de.jsilbereisen.test;

public class UseTryWithResources {
    public void method() {
        // Perfumed
        try (SomeAutoclosable x = new SomeAutoclosable()) {
            x.doSomething();
        } catch (Exception e) {

        }

        // Not perfumed
        try {
            doSomething();
        } catch (Exception e) {

        }
    }
}