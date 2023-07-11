package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.TryStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.visitor.MethodCallByNameVisitor;
import de.jsilbereisen.perfumator.engine.visitor.TryStmtVisitor;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.jsilbereisen.perfumator.model.DetectedInstance.from;
import static de.jsilbereisen.perfumator.util.NodeUtil.as;

/**
 * {@link Detector} for the "Single Method call when testing for runtime exceptions" {@link Perfume}.
 * All method calls to methods with the names, defined by the keys of
 * {@link #EXCEPTION_TEST_METHOD_TO_CLASS}, are searched for and checked for being perfumed. It is also
 * validated via checking the static imports, whether this is actually the method from the framework.
 * Only if none of the known framework methods are imported, all try-statements are visited and checked for
 * fitting the try-catch-idiom for RuntimeException testing, and for being perfumed.
 */
public class MethodCallRteTestingDetector implements Detector<Perfume> {

    public static final Map<String, Set<String>> EXCEPTION_TEST_METHOD_TO_CLASS = Map.of(
            "assertThrows", Set.of("org.junit.jupiter.api.Assertions", "org.junit.Assert"), // Present in JUnit 5, 4.13
            "assertThrowsExactly", Set.of("org.junit.jupiter.api.Assertions"), // Present only in JUnit 5
            "assertThatThrownBy", Set.of("org.assertj.core.api.Assertions"));

    public static final Map<String, ExceptionAssertionArgumentChecker> EXCEPTION_TEST_METHOD_TO_ARGUMENT_CHECKER = Map.of(
            "assertThrows", new JUnitArgumentChecker(),
            "assertThrowsExactly", new JUnitArgumentChecker(),
            "assertThatThrownBy", new AssertJArgumentChecker()
    );

    private Perfume perfume;

    private JavaParserFacade analysisContext;

    @Override
    public @NotNull List<DetectedInstance<Perfume>> detect(@NotNull CompilationUnit astRoot) {
        List<DetectedInstance<Perfume>> detections = new ArrayList<>();

        // If none of our known test-methods or the classes that contain them are imported, no reason to search
        Map<String, Boolean> frameworkMethodToNeedsClassNameOnCall = analyseImports(astRoot);

        if (frameworkMethodToNeedsClassNameOnCall.isEmpty()) {
            // look for the try-catch-idiom, if none of the known methods is imported
            TryStmtVisitor tryStmtVisitor = new TryStmtVisitor();
            astRoot.accept(tryStmtVisitor, null);
            List<TryStmt> tryStmts = tryStmtVisitor.getTryStmts();

            for (TryStmt tryStmt : tryStmts) {
                if (isPerfumed(tryStmt)) {
                    detections.add(from(perfume, tryStmt, astRoot));
                }
            }

        } else {
            // First, analyse all calls to the known methods from the frameworks
            MethodCallByNameVisitor visitor = new MethodCallByNameVisitor();
            astRoot.accept(visitor, frameworkMethodToNeedsClassNameOnCall.keySet());
            List<MethodCallExpr> callsToCheck = visitor.getMethodCalls();

            for (MethodCallExpr call : callsToCheck) {
                if (isPerfumed(call, frameworkMethodToNeedsClassNameOnCall)) {
                    detections.add(from(perfume, call, astRoot));
                }
            }
        }

        return detections;
    }

    @Override
    public void setConcreteDetectable(@NotNull Perfume concreteDetectable) {
        perfume = concreteDetectable;
    }

    @Override
    public void setAnalysisContext(@Nullable JavaParserFacade analysisContext) {
        this.analysisContext = analysisContext;
    }

    /**
     * Analyses the static imports in the AST. If any of the interesting framework-methods or the class which
     * contains them are imported, returns them in a map, where the method's name is the key, and the value signals
     * whether the class' name is required on the method call (depending on the import).
     *
     * @param astRoot The AST with the import declarations.
     * @return A map, as described above.
     */
    @NotNull
    private Map<String, Boolean> analyseImports(@NotNull CompilationUnit astRoot) {
        List<ImportDeclaration> staticImports = astRoot.getImports().stream().filter(ImportDeclaration::isStatic)
                .collect(Collectors.toList());

        if (staticImports.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, Boolean> methodToNeedsClassName = new HashMap<>();

        staticImports.forEach(importDeclaration -> {
            for (Map.Entry<String, Set<String>> entry : EXCEPTION_TEST_METHOD_TO_CLASS.entrySet()) {
                String methodName = entry.getKey();

                // Check if imports the method's class => if its not a * import, need class name on call
                if (entry.getValue().contains(importDeclaration.getNameAsString())) {
                    methodToNeedsClassName.put(methodName, !importDeclaration.isAsterisk());
                    continue;
                }

                // Imports the method fully qualified
                String withoutMethodNameAppended = importDeclaration.getNameAsString().replace("." + methodName, "");
                if (entry.getValue().contains(withoutMethodNameAppended)) {
                    methodToNeedsClassName.put(methodName, Boolean.FALSE);
                }
            }
        });

        return methodToNeedsClassName;
    }

    private boolean isPerfumed(@NotNull TryStmt tryStmt) {
        if (!catchesSingleException(tryStmt)) {
            return false;
        }

        BlockStmt tryBlock = tryStmt.getTryBlock();
        List<Statement> stmts = tryBlock.getStatements();
        if (stmts.size() != 2 || !stmts.stream().allMatch(Statement::isExpressionStmt)) {
            return false;
        }

        // Check whether the first Statement is a method call
        ExpressionStmt first = stmts.get(0).asExpressionStmt();
        MethodCallExpr firstCall = as(first.getExpression(), MethodCallExpr.class);
        if (firstCall == null) {
            return false;
        }

        // The method call may be executed on an Object/Class, but not be chained or anything
        Optional<Expression> scope = firstCall.getScope();
        if (!scope.map(expression -> as(expression, NameExpr.class) != null).orElse(true)) {
            return false;
        }

        // Check whether the second statement makes the test fail, if reached - this is not really precisely
        // validated though.
        ExpressionStmt second = stmts.get(1).asExpressionStmt();
        MethodCallExpr secondCall = as(second.getExpression(), MethodCallExpr.class);
        if (secondCall == null) {
            return false;
        }

        return secondCall.getNameAsString().equals("fail");
    }

    private boolean catchesSingleException(@NotNull TryStmt tryStmt) {
        List<CatchClause> catches = tryStmt.getCatchClauses();
        if (catches.size() != 1) {
            return false;
        }

        CatchClause first = catches.get(0);
        ClassOrInterfaceType singleExceptionType = as(first.getParameter().getType(), ClassOrInterfaceType.class);

        return singleExceptionType != null;
    }

    /**
     * Checks whether the method call is perfumed. At this point we know that the method call's name is one of the
     * keys of {@link #EXCEPTION_TEST_METHOD_TO_CLASS}, so we check whether it uses the correct arguments, and whether
     * the lambda-expression (which it should contain) is perfumed, in the sense that it only does a single method call.
     *
     * @param call The method call to analyse.
     * @param frameworkMethodToNeedsClassNameOnCall Map with the supported method names from the framework, and whether
     *                                              the method call needs to be called on the class (if it's not
     *                                              directly statically imported).
     * @return {@code true} if the call is perfumed. That is the case when the method is correctly called, with
     *         fitting arguments, and the lambda expression argument only issues a single method call.
     */
    private boolean isPerfumed(@NotNull MethodCallExpr call,
                               @NotNull Map<String, Boolean> frameworkMethodToNeedsClassNameOnCall) {
        Boolean needsClassCall = frameworkMethodToNeedsClassNameOnCall.get(call.getNameAsString());
        if (needsClassCall == null) {
            return false; // Should never happen
        }

        // Check if the method is called from the class if needed -> Should be sure enough then, that it is the correct
        // method from the framework
        Optional<Expression> scope = call.getScope();
        if (needsClassCall) {
            if (scope.isEmpty()) {
                return false;

            } else {
                NameExpr name = as(scope.get(), NameExpr.class);
                if (name == null) {
                    return false;
                }

                Set<String> possibleQualifiedClassesOfMethod = EXCEPTION_TEST_METHOD_TO_CLASS.get(call.getNameAsString());
                if (possibleQualifiedClassesOfMethod == null) {
                    return false; // Should never happen
                }

                if (possibleQualifiedClassesOfMethod.stream().noneMatch(str -> str.endsWith("." + name.getNameAsString()))) {
                    return false;
                }
            }
        }

        // Check the arguments given to the call
        ExceptionAssertionArgumentChecker argumentChecker = EXCEPTION_TEST_METHOD_TO_ARGUMENT_CHECKER.get(call.getNameAsString());
        if (argumentChecker == null) {
            return false;
        }

        return argumentChecker.checkArguments(call);
    }

    /**
     * Checks if the given lambda expression only does a single method-invocation.
     * To be a bit more tolerant, also supports the case that the invocation is wrapped within a block, even
     * though that is unnecessary.
     *
     * @param lambda The lambda to check.
     * @return {@code true} if the only real statement issues only a single method call, {@code false} otherwise.
     */
    private static boolean callsSingleMethod(@NotNull LambdaExpr lambda) {
        // Get the first "real" expression statement, either directly, or via the wrapped block.
        ExpressionStmt expr = as(lambda.getBody(), ExpressionStmt.class);
        if (expr == null) {
            BlockStmt block = as(lambda.getBody(), BlockStmt.class);
            if (block == null) {
                return false;
            }

            // If it's a block, it may only contain exactly 1 statement.
            if (block.getStatements().size() != 1) {
                return false;
            }

            Optional<Statement> first = block.getStatements().getFirst();
            if (first.isEmpty()) {
                return false; // Should not happen, as we checked for the size() before
            }

            expr = as(first.get(), ExpressionStmt.class);
            if (expr == null) {
                return false;
            }
        }

        // Is the statement a method call?
        MethodCallExpr call = as(expr.getExpression(), MethodCallExpr.class);
        if (call == null) {
            return false;
        }

        // The method call may be executed on an Object/Class, but not be chained or anything
        Optional<Expression> scope = call.getScope();
        return scope.map(expression -> as(expression, NameExpr.class) != null).orElse(true);
    }

    @FunctionalInterface
    private interface ExceptionAssertionArgumentChecker {

        boolean checkArguments(@NotNull MethodCallExpr call);
    }

    private static class JUnitArgumentChecker implements ExceptionAssertionArgumentChecker {

        @Override
        public boolean checkArguments(@NotNull MethodCallExpr call) {
            List<Expression> arguments = call.getArguments();
            if (arguments.size() < 2) {
                return false;
            }

            ClassExpr classExpr = as(arguments.get(0), ClassExpr.class);
            if (classExpr == null) {
                return false;
            }

            LambdaExpr lambda = as(arguments.get(1), LambdaExpr.class);
            if (lambda == null) {
                return false;
            }

            return callsSingleMethod(lambda);
        }
    }

    private static class AssertJArgumentChecker implements ExceptionAssertionArgumentChecker {

        @Override
        public boolean checkArguments(@NotNull MethodCallExpr call) {
            List<Expression> arguments = call.getArguments();
            if (arguments.isEmpty()) {
                return false;
            }

            LambdaExpr lambda = as(arguments.get(0), LambdaExpr.class);
            if (lambda == null) {
                return false;
            }

            return callsSingleMethod(lambda);
        }
    }
}
