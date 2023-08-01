package de.jsilbereisen.test;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashSet;

public class OptimizedEnumCollectionsPerfume {

    public enum MyEnum {
        ONE, TWO, THREE, FOUR
    }

    static class Perfumed {

        private Set<MyEnum> enumSet1 = EnumSet.of(MyEnum.ONE);
        private Set<MyEnum> enumSet2 = EnumSet.allOf(MyEnum.class);
        private Set<MyEnum> enumSet3 = EnumSet.noneOf(MyEnum.class);
        private Set<MyEnum> enumSet4 = EnumSet.range(MyEnum.ONE, MyEnum.THREE);
        private Set<MyEnum> justCopied = EnumSet.copyOf(enumSet1);
        private Set<MyEnum> complemented = EnumSet.complementOf(enumSet4);

        void method() {
            Map<MyEnum, Object> enumMap = new EnumMap<>(MyEnum.class);
        }
    }

    static class NotPerfumed {

        private Set<MyEnum> notEnumSet = new HashSet<>();
    }
}