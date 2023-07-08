package de.jsilbereisen.perfumator.engine.visitor;

import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TryStmtVisitor extends VoidVisitorAdapter<Object> {

    @Getter(onMethod = @__({@NotNull}))
    private List<TryStmt> tryStmts = new ArrayList<>();

    @Override
    public void visit(TryStmt tryStmt, Object o) {
        tryStmts.add(tryStmt);

        super.visit(tryStmt, o);
    }
}
