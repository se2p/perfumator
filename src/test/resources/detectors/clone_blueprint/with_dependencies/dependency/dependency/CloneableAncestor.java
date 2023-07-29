package dependency;

public class CloneableAncestor implements Cloneable {

    @Override
    public CloneableAncestor clone() throws CloneNotSupportedException {
        return (CloneableAncestor) super.clone();
    }
}