package de.jsilbereisen.test;

import java.util.Objects;

public class AssertPrivateMethodPerfume {

    private void perfumed(int i, Object x) {
        assert i > 0 || Objects.requireNonNull(x): "Perfumed";

        doSomething();
    }

    private void notPerfumedNoMessage(int i) {
        assert i > 0;

        doSomething();
    }

    void notPerfumedNotPrivate(int i) {
        assert i > 0 : "Msg";

        doSomething();
    }

    void notPerfumedAssertUsesOtherVar(int i) {
        int x = i;
        assert x > 0 : "Msg";

        doSomething();
    }
}