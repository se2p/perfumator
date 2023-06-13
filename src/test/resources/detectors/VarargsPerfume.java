package de.jsilbereisen.perfumator.test;

public class VarargsPerfume {

    void perfumed(int first, int... remaining) {
        doSomething();
        return;
    }

    void notPerfumed(int number, int... numbers) {
        if (numbers.length < 1) {
            return;
        }

        doSomething();
    }

    // TODO: test case with exception in if-statement

    // TODO: test case with length

    // TODO: test case: if (length > 1) ... else return

    // TODO: test case: if (length > SOME_INT_VAR)  => Class field / method param?
}