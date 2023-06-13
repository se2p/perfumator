package de.jsilbereisen.perfumator.engine.visitor;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BinaryExprVisitor extends VoidVisitorAdapter<Object> {

    @Getter(onMethod = @__({@NotNull}))
    private final List<BinaryExpr> binaryExpressions = new ArrayList<>();

    @Override
    public void visit(BinaryExpr binaryExpression, Object o) {
        binaryExpressions.add(binaryExpression);

        super.visit(binaryExpression, null);
    }
}
