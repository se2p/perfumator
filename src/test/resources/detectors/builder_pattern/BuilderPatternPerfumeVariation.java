package de.jsilbereisen.test;

public class BuilderPatternPerfumeVariation {

    private int field1;
    private int field2;
    private int field3;
    private int field4;

    public BuilderPatternPerfumeVariation() {}

    // Test with generic builder type
    private BuilderPatternPerfumeVariation(Builder<?> bob){}

    public abstract static class Builder<T extends Builder<T>> {
        private static boolean staticField;

        private final int field1;
        private final int field2;
        private int field3;
        private int field4;

        protected Builder(int field1, int field2) {}

        // private/package-private/protected util methods => dont care about ret type
        protected int subtract(int a, int b) {return a-b;}
        // static method => dont care about return type
        public static boolean getStaticInfo() {return staticField;}

        // Public method with correct return type
        public Builder field1(int val) {field1=val;}

        // may also be abstract
        public abstract BuilderPatternPerfumeVariation build();

        // As in the example in "Effective Java" for inheritance builder
        protected abstract T self();
    }
}

public class SecondTopLevelClass {
    private int field1;
    private int field2;
    private int field3;
    private int field4;

    private SecondTopLevelClass(SecondBuilder bob) {}

    public static class SecondBuilder {
        private int field1;
        private int field2;
        private int field3;
        private int field4;
        // Use default constructor
        public SecondTopLevelClass build() {}
    }
}

public class ThirdClass {
    private int field1;
    private int field2;
    private int field3;
    private int field4;

    private ThirdClass(ThirdBuilder bob) {}

    public static ThirdBuilder builder() {}

    public static class ThirdBuilder {
        private int field1;
        private int field2;
        private int field3;
        private int field4;

        // private constructor -> top-level class needs "public static ThirdBuilder builder()" method
        private ThirdBuilder() {}
        public ThirdClass build() {}

        public void incField1() {field1++;}
    }
}
