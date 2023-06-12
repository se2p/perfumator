package de.jsilbereisen.perfumator.test;

public class VarargsPerfume {

    void perfumed(int first, int... remaining) {
        doSomething();
        return;
    }

    /*
    void notPerfumed(int... numbers) {
        if (numbers.length < 1) {
            return;
        }

        doSomething();
    }
     */

    // TODO: test case with exception in if-statement

    // TODO: test case with length
}