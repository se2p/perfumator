package de.jsilbereisen.perfumator.test;

public class VarargsPerfume {

    // Perfumed
    void perfumed(int first, int... remaining) {
        doSomething();
        return;
    }

    static class Inner {

        void perfumedInner(int first, int... remaining) {
            doSomething();
            return;
        }
    }

    // Not Perfumed: checks length + returns immediately
    void notPerfumed(int number, int... numbers) {
        if (numbers.length < 1) {
            return;
        }

        doSomething();
    }

    // Not Perfumed: missing another Parameter with the same type as the Varargs Parameter
    void noOtherArgOfSameType(String str, Object... objects) {
        doSomething();
    }

    // Not Perfumed: checks length + throws exception immediately
    void notPerfumedException(int number, int... numbers) {
        if (numbers.length < 5) {
            throw new IllegalArgumentException("Not perfumed");
        }

        doSomething();
    }

    // Not Perfumed: checks length with Greater(-equals) and exits immediately in the else-branch
    void notPerfumedElseBranch(int number, int... numbers) {
        doSomething();

        if (numbers.length >= 4) {
            continueMethod();
        } else {
            return;
        }
    }

    // Not perfumed: nested if-calls, nested binary expressions
    void notPerfumedMoreComplex(int number, String str, int... numbers) {
        int i = 1;
        doSomething();

        if (someMethodCall()) {
            Object o;
            if (o != null && numbers.length >= 2) {
                i = 2;
            } else {
                return;
            }
        }
    }

    // Not perfumed: BinaryExpr is negated, so have to search in the opposite branch for the return
    void notPerfumedNegatedBinaryExpr(int number, int... numbers) {
        if (!(numbers.length < 1)) {
            doSomething();
        } else {
            return;
        }
    }

    // TODO: test case: if (length > SOME_INT_VAR)  => Class field / method param?
}