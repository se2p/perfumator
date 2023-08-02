package de.jsilbereisen.test;

public class BuilderPatternInvalidBuilderClasses {

    private int field1;
    private int field2;
    private int field3;
    private int field4;

    private BuilderPatternInvalidBuilderClasses(NonStaticBuilder b) {}
    public class NonStaticBuilder {
        private int field1;
        private int field2;
        private int field3;
        private int field4;
        public NonStaticBuilder() {}
        public BuilderPatternInvalidInnerClasses build() {}
    }

    private BuilderPatternInvalidBuilderClasses(ClassNameDoesntEndWithBuilderSadly b) {}
    public static class ClassNameDoesntEndWithBuilderSadly {
        private int field1;
        private int field2;
        private int field3;
        private int field4;
        public ClassNameDoesntEndWithBuilderSadly() {}
        public BuilderPatternInvalidInnerClasses build() {}
    }

    protected BuilderPatternInvalidBuilderClasses(NotEnoughFieldsBuilder b) {}
    public static class NotEnoughFieldsBuilder {
        private int field1;
        private int field2;
        private int field3;
        public NotEnoughFieldsBuilder() {}
        public BuilderPatternInvalidInnerClasses build() {}
    }

    private BuilderPatternInvalidBuilderClasses(NoBuildMethodBuilder b) {}
    public static class NoBuildMethodBuilder {
        private int field1;
        private int field2;
        private int field3;
        private int field4;
        public NoBuildMethodBuilder() {}
    }

    private BuilderPatternInvalidBuilderClasses(NoPublicOrProtectedConstructorBuilder b) {}
    public static class NoPublicOrProtectedConstructorBuilder {
        private int field1;
        private int field2;
        private int field3;
        private int field4;
        private NoPublicOrProtectedConstructorBuilder() {}
        NoPublicOrProtectedConstructorBuilder() {}
        public BuilderPatternInvalidInnerClasses build() {}
    }

    private BuilderPatternInvalidBuilderClasses(NonStaticMethodWrongReturnTypeBuilder b) {}
    public static class NonStaticMethodWrongReturnTypeBuilder {
        private int field1;
        private int field2;
        private int field3;
        private int field4;
        public NonStaticMethodWrongReturnTypeBuilder() {}
        public BuilderPatternInvalidInnerClasses build() {}

        // Invalid return for non-static public method (should
        // return builder or void)
        public boolean incrementField1() {field1++;return true;}
    }

    // Wouldnt compile anyway, bcuz needs qualifier: "NestMe.DoubleNestedBuilder". Just to test whether Double nested
    // class is ignored anyway
    private BuilderPatternInvalidBuilderClasses(DoubleNestedBuilder b) {}
    public class NestMe {
        public static class DoubleNestedBuilder {
            private int field1;
            private int field2;
            private int field3;
            private int field4;
            public DoubleNestedBuilder() {}
            public BuilderPatternInvalidInnerClasses build() {}
        }
    }
}