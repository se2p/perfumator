package de.jsilbereisen.test;

import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class SwingTimer {
    
    Timer t = new Timer(1000, new ActionListener() {});
    SomeClass c = new SomeClass();
    
    public static void main(String[] args) {
        Timer timer1;
        timer1 = new Timer(50, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                // do something with gui data
            }
        });
        var timer2 = new Timer(100, new ActionListener() {});
        javax.swing.Timer timer3 = new javax.swing.Timer(100, () -> {});
    }
}
