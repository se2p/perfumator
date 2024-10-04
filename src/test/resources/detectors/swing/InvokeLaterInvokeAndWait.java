package de.jsilbereisen.test;

import javax.swing.SwingUtilities;
import static javax.swing.SwingUtilities.invokeAndWait;

public class InvokeLaterInvokeAndWait {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            // perfume
        });
        
        invokeAndWait(() -> {
            // perfume
        });
        
        // no perfume
        invokeLater();
    }
    
    public static void invokeLater() {
        // no perfume, not the library method we are looking for
    }
}