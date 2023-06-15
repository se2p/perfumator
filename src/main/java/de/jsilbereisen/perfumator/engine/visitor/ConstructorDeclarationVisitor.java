package de.jsilbereisen.perfumator.engine.visitor;

import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class ConstructorDeclarationVisitor extends VoidVisitorAdapter<Object> {

    @Getter(onMethod = @__({@NotNull}))
    private final List<ConstructorDeclaration> constructors = new ArrayList<>();

    @Override
    public void visit(ConstructorDeclaration constructorDeclaration, Object o) {
        constructors.add(constructorDeclaration);
    }
}
