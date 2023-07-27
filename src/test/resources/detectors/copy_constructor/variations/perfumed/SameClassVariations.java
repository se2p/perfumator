package de.jsilbereisen.test;

// Has generic parameter
public class SameClassVariations<T> {

    private int x;

    final int y;

    // Detect with generics usage
    public SameClassVariations(SameClassVariations<T> copyMe) {
        x = UnnecessaryUtilClass.doCopy(copyMe.x, flag);
        y = y;
        y = copyMe.y.duplicate();
    }
}