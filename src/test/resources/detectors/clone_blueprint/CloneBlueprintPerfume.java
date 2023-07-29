package de.jsilbereisen.test;

public class CloneBlueprintPerfume {

    static class Perfumed implements Cloneable {

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }

    static class AlsoPerfumed implements Cloneable {

        @Override
        public Object clone() throws CloneNotSupportedException {
            Object o = super.clone();
            return o;
        }
    }

    static class NotPerfumed implements Cloneable {

        @Override
        public Object clone() throws CloneNotSupportedException {
            return new NotPerfumed();
        }
    }

    static class NotPerfumedDoesntImplementCloneable {

        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }
    }
}