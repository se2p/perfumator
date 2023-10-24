package de.jsilbereisen.test;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IteratorNextContractNotPerfumed {

    // does not Implement the interface
    static class NotAnIterator {
        public boolean hasNext() {return true;}
        public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return "";
        }
    }

    // Iterator has another type argument
    static class WrongIteratorType implements Iterator<Integer> {
        public boolean hasNext() {return true;}
        public String next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            return "";
        }
    }

    // Variable "hasNext" is not a boolean
    static class HasNextNotBoolean implements Iterator<String> {
        private String hasNext;
        public boolean hasNext() {return true;}
        public String next() {
            if (!hasNext) {
                throw new NoSuchElementException();
            }
            return "";
        }
    }

    // Simply does not throw the exception
    static class DoesntThrow implements Iterator<String> {
        private boolean hasNext;
        public boolean hasNext() {return true;}
        public String next() {
            if (!hasNext()) {
                notThrown();
            } else {
                notThrown();
            }

            if (!hasNext) {
                notThrown();
            } else {
                notThrown();
            }

            notThrown();
        }
    }

    static class CantResolveMethod implements Iterator<String> {
        public String next() {
            if (!doesHaveNext()) {
                throw new NoSuchElementException();
            }
            return "";
        }
    }
}