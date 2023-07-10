package de.jsilbereisen.test;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class IteratorNextContractPerfumeVariants {

    static class MethodCallElseBranch implements Iterator<String> {
        @Override
        public boolean hasNext() {return true;}

        @Override
        public String next() {
            if (hasNext()) {
                return "";
            } else {
                throw new NoSuchElementException();
            }
        }
    }

    static class MethodCallLastStmt implements Iterator<String> {
        @Override
        public boolean hasNext() {return true;}

        @Override
        public String next() {
            if (hasNext()) {
                return "";
            }

            throw new NoSuchElementException();
        }
    }

    static class VariableElseBranch implements Iterator<String> {

        private boolean next;

        @Override
        public boolean hasNext() {return true;}

        @Override
        public String next() {
            if (next) {
                return "";
            } else throw new NoSuchElementException();
        }
    }

    static class VariableLastStmt implements Iterator<String> {

        private boolean isNext;

        @Override
        public boolean hasNext() {return true;}

        @Override
        public String next() {
            if (isNext) {
                return "";
            }

            throw new NoSuchElementException();
        }
    }
}