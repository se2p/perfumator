package de.jsilbereisen.perfumator.engine.visitor;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MethodDeclarationVisitor extends VoidVisitorAdapter<Object> {

    @Getter(onMethod = @__({@NotNull}))
    private final List<MethodDeclaration> methodDeclarations = new ArrayList<>();

    @Override
    public void visit(MethodDeclaration methodDeclaration, Object o) {
        methodDeclarations.add(methodDeclaration);

        super.visit(methodDeclaration, o);
    }
}
