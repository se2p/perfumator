package de.jsilbereisen.test;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IteratorNextContractPerfume implements Iterator<String> {

    @Override
    public boolean hasNext() {return true;}

    @Override
    public String next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        doSomething();
        return "";
    }
}