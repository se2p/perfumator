package dependency;

public class AncestorThatOverridesEquals {

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}