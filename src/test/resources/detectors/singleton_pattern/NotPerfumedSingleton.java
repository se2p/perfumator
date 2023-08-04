package de.jsilbereisen.test;

import java.io.Serializable;

public class NotPerfumedSingleton {

    public static class ConstructorNotPrivate {
        public static final ConstructorNotPrivate INSTANCE = new ConstructorNotPrivate();
    }

    public static class UninitializedPublic {
        public static final UninitializedPublic INSTANCE;
        private UninitializedPublic() {}
    }

    public static class MultipleSingletonInstances {
        public static final MultipleSingletonInstances INSTANCE_ONE = new MultipleSingletonInstances();
        public static final MultipleSingletonInstances INSTANCE_TWO = new MultipleSingletonInstances();
        private MultipleSingletonInstances() {}
    }

    public static class PrivateFieldFactoryMethodMissing {
        private static final PrivateFieldFactoryMethodMissing INSTANCE;
        private PrivateFieldFactoryMethodMissing() {}
    }

    public static class PrivateFieldFactoryMethodWrongReturn {
        private static final PrivateFieldFactoryMethodWrongReturn INSTANCE;
        private static final Object NOT_INSTANCE;
        private PrivateFieldFactoryMethodWrongReturn() {}
        public static PrivateFieldFactoryMethodWrongReturn instance() {
            return NOT_INSTANCE;
        }
    }

    public static class SerializableNotAllTransient implements Serializable {
        public static final SerializableNotAllTransient INSTANCE = new SerializableNotAllTransient();
        private Object nonTransientField;
        private SerializableNotAllTransient() {}
        private Object readResolve() {
            return INSTANCE;
        }
    }

    public static class SerializableIncorrectReadResolve implements Serializable {
        public static final SerializableIncorrectReadResolve INSTANCE = new SerializableIncorrectReadResolve();
        private transient Object nonTransientField;
        private SerializableIncorrectReadResolve() {}
        private Object readResolve() {
            return new Object();
        }
    }

    public enum MultipleConstants {
        ONE, TWO, THREE;
        private Object field;
        public void someMethod() {}
    }

    public enum NoConstants {
        ;
        private Object field;
        public void someMethod() {}
    }

    public enum NoInstanceField {
        INSTANCE;
        private static Object staticField;
        public void someMethod() {}
    }

    public enum NoPublicInstanceMethod {
        INSTANCE;
        private Object field;
        public static void staticMethod() {}
    }
}