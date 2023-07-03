package de.jsilbereisen.perfumator.engine.visitor;

import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class SwitchStmtVisitor extends VoidVisitorAdapter<Object> {

    @Getter(onMethod = @__({@NotNull}))
    private final List<SwitchStmt> switchStmts = new ArrayList<>();

    @Override
    public void visit(SwitchStmt switchStmt, Object o) {
        switchStmts.add(switchStmt);

        super.visit(switchStmt, o);
    }
}
