package de.jsilbereisen.test;

import dependency.CloneableAncestor;

public class PerfumeWithDependency {

    static class Perfumed extends CloneableAncestor {

        @Override
        public Perfumed clone() throws CloneNotSupportedException {
            return (Perfumed) super.clone();
        }
    }

    static class NotPerfumed extends CloneableAncestor {

        @Override
        public NotPerfumed clone() throws CloneNotSupportedException {
            return new NotPerfumed();
        }
    }

    static class NotPerfumedNotCloneable {

        @Override
        public NotPerfumedNotCloneable clone() throws CloneNotSupportedException {
            return (NotPerfumedNotCloneable) super.clone();
        }
    }
}