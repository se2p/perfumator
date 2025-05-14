package de.jsilbereisen.test;

import javax.swing.*;

public class JFrameDispose {
    class NoRealJFrame {
        public void dispose() {
            
        }
    }

    class JFrameSubClass extends JFrame {
        @Override
        public void dispose() {
            super.dispose();
        }
    }

    public static void main(String[] args) {
        NoRealJFrame frame = new NoRealJFrame();
        JFrame frame1 = new JFrame();
        javax.swing.JFrame frame2 = new javax.swing.JFrame();
        var frame3 = new JFrame();
        frame.dispose();
        frame1.dispose();
        frame2.dispose();
        frame3.dispose();
        new JFrameSubClass().dispose();
    }
}
