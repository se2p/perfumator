package de.jsilbereisen.test;

import static javax.swing.SwingUtilities.invokeAndWait;
import static javax.swing.SwingUtilities.invokeLater;

public class InvokeLaterInvokeAndWait {

    public static void main(String[] args) {
        invokeLater(new Runnable() {
            // perfume
        });
        
        invokeAndWait(() -> {
            // perfume
        });
    }
}
