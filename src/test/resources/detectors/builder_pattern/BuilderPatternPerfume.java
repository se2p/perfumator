package de.jsilbereisen.test;

/**
 * To be perfumed, allow outer class to only have private/protected (for inheritance, if class is abstract)
 * constructor(s)
 */
public class BuilderPatternPerfume {

    private int field1;

    private int field2;

    private String optionalField1;

    private String optionalField2;

    private String optionalField3;

    private BuilderPatternPerfume(Builder builder) {
        this.field1 = builder.field1;
        this.field2 = builder.field2;
        this.optionalField1 = builder.optionalField1;
        this.optionalField2 = builder.optionalField2;
        this.optionalField3 = builder.optionalField3;
    }

    public static class Builder {

        private final int field1;

        private final int field2;

        private String optionalField1;

        private String optionalField2;

        private String optionalField3;

        public Builder(int field1, int field2) {
            this.field1 = field1;
            this.field2 = field2;
        }

        // The exact amount of methods (e.g. one for each field) the build provides are not checked, as it might offer
        // specific methods depending on the field type; for example an "add" method for Lists
        // BUT every public method has to return the Builder object itself, EXCEPT the "build" method
        public Builder optionalField1(String str) {optionalField1=str;}
        public Builder optionalField2(String str) {optionalField2=str;}
        public Builder optionalField3(String str) {optionalField3=str;}

        // Might be abstract, for inheritance
        public BuilderPatternPerfume build() {
            return new BuilderPatternPerfume(this);
        }
    }
}
