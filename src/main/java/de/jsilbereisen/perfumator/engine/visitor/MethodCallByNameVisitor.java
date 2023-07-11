package de.jsilbereisen.perfumator.engine.visitor;

import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MethodCallByNameVisitor extends VoidVisitorAdapter<Set<String>> {

    @Getter(onMethod = @__({@NotNull}))
    private final List<MethodCallExpr> methodCalls = new ArrayList<>();

    /**
     * When visiting a {@link MethodCallExpr}, captures it in the list of this visitor, if the name of the called method
     * is present in the given {@link Set} of method names. If this set is {@code null} though,
     * every method call is captured.
     *
     * @param methodCall The currently visited {@link MethodCallExpr}.
     * @param methodNames The {@link Set} with method names, whose calls to should be captured. If the given set is
     *                    {@code null}, will capture every method call that is visited.
     */
    @Override
    public void visit(@NotNull MethodCallExpr methodCall, @Nullable Set<String> methodNames) {
        if (methodNames == null || methodNames.contains(methodCall.getNameAsString())) {
            methodCalls.add(methodCall);
        }

        super.visit(methodCall, methodNames);
    }
}
