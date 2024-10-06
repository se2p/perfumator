package de.jsilbereisen.test;

import javax.swing.SwingUtilities;

public class InvokeLaterInvokeAndWait {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            // perfume
        });

        SwingUtilities.invokeAndWait(() -> {
            // perfume
        });
        
        invokeLater(() -> {
            // no perfume
        });
    }
    
    public static void invokeLater(Runnable runnable) {
        
    }
}
