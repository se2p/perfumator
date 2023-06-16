package de.jsilbereisen.test;

/**
 * Test detection of the "Defensive Default Case" Perfume. It rewards usage of the "default" case in a switch
 * statement.
 */
public class DefensiveDefaultCase {

    private void method() {
        // perfumed Switch: default case present and not empty
        switch (someEnum) {
            case ONE:
                doSomething();
                break;
            case TWO:
                doSomethingElse();
                break;
            default:
                handleDefaultCase();
        }

        // perfumed Switch: default case present
        switch (someEnum) {
            case ONE:
                doSomething();
                break;
            case TWO:
                doSomethingElse();
                break;
            default:
                // Can not happen because...
        }

        // not perfumed: no default case given
        switch (someEnum) {
            case ONE -> {
                return;
            }
            case TWO -> {
                return;
            }
        }
    }

    // TODO Test case: nested switch statements
}