package de.jsilbereisen.test;

public class BuilderPatternInvalidTopLevelClass {

    // Not enough fields!
    int field1;
    int field2;
    int field3;

    public static class Builder {
        int field1;
        int field2;
        int field3;
        int field4;
        public BuilderPatternInvalidTopLevelClass build() {}
    }
}

public class BuilderPatternInvalidTopLevelClass2 {

    int field1;
    int field2;
    int field3;
    int field4;

    // Builder-Constructor may not be public!
    public BuilderPatternInvalidTopLevelClass2(Builder2 builder2) {}

    public static class Builder2 {
        int field1;
        int field2;
        int field3;
        int field4;
        public BuilderPatternInvalidTopLevelClass2 build() {}
    }
}
