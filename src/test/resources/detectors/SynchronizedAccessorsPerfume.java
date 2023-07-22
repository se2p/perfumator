package de.jsilbereisen.test;

import java.util.List;

/**
 * We dont check whether the class actually has the fields, as they might be inherited - we just check for
 * getter/setter pairs.
 */
public class SynchronizedAccessorsPerfume {

    // Synchronized in the method signature
    public synchronized void setSomeProp(String someProp) {
        this.someProp = someProp;
    }

    public synchronized String getSomeProp() {
        return someProp;
    }

    // Synchronized within the method
    public void setSomeOtherProp(String someOtherProp) {
        synchronized (this) {
            this.someOtherProp = someOtherProp;
        }
    }

    public String getSomeOtherProp() {
        synchronized (this) {
            return someOtherProp;
        }
    }

    // A setter might have overloads, for convenience reasons
    public static class WithOverloads {

        public synchronized List<String> getList() {
            return list;
        }

        public synchronized void setList(List<String> list) {
            this.list = list;
        }

        public synchronized void setList(String... list) {
            this.list = List.of(list);
        }
    }


    // ------------------------------------------------------------------------------------
    //                             Non-perfumed cases
    // ------------------------------------------------------------------------------------

    // setter (overload) is not synchronized
    public synchronized List<String> getXx() {
        return xx;
    }

    public synchronized void setXx(List<String> xx) {
        this.xx = xx;
    }

    public void setXx(String... xx) {
        this.xx = List.of(xx);
    }

    // getter is not synchronized
    public List<String> getYy() {
        return yy;
    }

    public synchronized void setYy(List<String> yy) {
        this.yy = yy;
    }

    // setter (overload) is not synchronized
    public List<String> getX() {
        synchronized (this) {
            return x;
        }
    }

    public void setX(List<String> x) {
        synchronized (this) {
            this.x = x;
        }
    }

    public void setX(String... x) {
        this.x = List.of(x);
    }

    // getter is not synchronized
    public List<String> getY() {
        return y;
    }

    public synchronized void setY(List<String> y) {
        this.y = y;
    }
}