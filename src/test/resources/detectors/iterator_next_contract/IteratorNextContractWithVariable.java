package de.jsilbereisen.test;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IteratorNextContractWithVariable implements Iterator<String> {

    private boolean hasNext;

    @Override
    public boolean hasNext() {return true;}

    @Override
    public String next() {
        if (!hasNext) {
            throw new NoSuchElementException();
        }

        doSomething();
        return "";
    }
}