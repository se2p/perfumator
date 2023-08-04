package de.jsilbereisen.test;

import java.io.Serializable;

public class SingletonPatternPerfume {

    public static class PublicInstanceField {
        public static final PublicInstanceField INSTANCE = new PublicInstanceField();
        private PublicInstanceField() {}
    }

    public class PublicFactoryMethod {
        // If the instance field is private, we allow it to be initialized lazily in the factory method
        // => may be non-final
        private static PublicFactoryMethod instance;
        private PublicFactoryMethod() {}

        // We verify that it returns the content of the instance-field as the last statement.
        public static PublicFactoryMethod getInstance() {
            doInitialization();
            return instance;
        }
    }

    // Lets say an enum-singleton has to have at least one public non-static method and at least one non-static field
    // for it to make sense. Why would you need an instance otherwise?
    public enum EnumSingleton {
        INSTANCE;

        private boolean fieldWithStateInfo;

        public void switchState() {
            fieldWithStateInfo = !fieldWithStateInfo;
        }
    }

    // Serializable constraints
    public static class SerializableSingleton implements Serializable {
        public static final SerializableSingleton INSTANCE = new SerializableSingleton();

        // All instance fields need to be transient
        private transient int field1;
        private transient String field2;

        private SerializableSingleton() {}

        // need to provide a special readResolve() method (any access modifier, according to the Serializable JavaDoc)
        private Object readResolve() {
            return INSTANCE;
        }
    }
}