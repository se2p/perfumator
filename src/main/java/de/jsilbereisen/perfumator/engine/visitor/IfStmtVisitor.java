package de.jsilbereisen.perfumator.engine.visitor;

import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class IfStmtVisitor extends VoidVisitorAdapter<Object> {

    @Getter(onMethod = @__({@NotNull}))
    private final List<IfStmt> ifStmts = new ArrayList<>();

    @Override
    public void visit(IfStmt ifStmt, Object o) {
        ifStmts.add(ifStmt);

        super.visit(ifStmt, o);
    }
}
