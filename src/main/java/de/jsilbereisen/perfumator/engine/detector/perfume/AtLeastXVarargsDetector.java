package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.visitor.BinaryExprVisitor;
import de.jsilbereisen.perfumator.engine.visitor.IfStmtVisitor;
import de.jsilbereisen.perfumator.engine.visitor.MethodDeclarationVisitor;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * {@link Detector<Perfume>} for the "At least X Varargs" perfume.
 */
public class AtLeastXVarargsDetector implements Detector<Perfume> {
    private static final Set<BinaryExpr.Operator> OPERATORS_FOR_LENGTH_CHECK = Set.of(BinaryExpr.Operator.LESS,
            BinaryExpr.Operator.LESS_EQUALS, BinaryExpr.Operator.GREATER, BinaryExpr.Operator.GREATER_EQUALS);

    private Perfume perfume;

    /**
     * Searches for the "At least X Varargs" {@link Perfume} instances in the given AST by
     * checking each declared method.
     * Returns a list with all findings.
     *
     * @param astRoot The root node of the AST where the {@link Perfume} should be detected.
     * @return A list with all {@link DetectedInstance<Perfume>}s of the "At least X Varargs" {@link Perfume}.
     */
    @Override
    public @NotNull List<DetectedInstance<Perfume>> detect(@NotNull CompilationUnit astRoot) {
        List<DetectedInstance<Perfume>> detectedPerfumes = new ArrayList<>();

        MethodDeclarationVisitor methodDeclarationVisitor = new MethodDeclarationVisitor();
        astRoot.accept(methodDeclarationVisitor, null);
        List<MethodDeclaration> methodDeclarations = methodDeclarationVisitor.getMethodDeclarations();

        for (MethodDeclaration methodDeclaration : methodDeclarations) {
            Optional<DetectedInstance<Perfume>> detected = checkForPerfume(methodDeclaration);
            detected.ifPresent(det -> {
                astRoot.getPrimaryTypeName().ifPresent(det::setTypeName);
                detectedPerfumes.add(det);
            });
        }

        return detectedPerfumes;
    }

    @Override
    public void setConcreteDetectable(@NotNull Perfume concreteDetectable) {
        perfume = concreteDetectable;
    }

    /**
     * Checks whether the given Method is perfumed with the "At least X Varargs" perfume.
     *
     * @param methodDeclaration The method to check.
     * @return An {@link Optional} with the {@link DetectedInstance<Perfume>} if the method is perfumed. Otherwise returns
     *         an {@link Optional#empty()}
     */
    @NotNull
    private Optional<DetectedInstance<Perfume>> checkForPerfume(@NotNull MethodDeclaration methodDeclaration) {
        if (methodDeclaration.isAbstract() || methodDeclaration.getBody().isEmpty()) {
            return Optional.empty();
        }

        NodeList<Parameter> parameters = methodDeclaration.getParameters();
        if (parameters == null || parameters.stream().noneMatch(Parameter::isVarArgs)) {
            return Optional.empty();
        }

        Parameter varargsParameter = parameters.stream().filter(Parameter::isVarArgs).findFirst().get();
        String varargsParamName = varargsParameter.getNameAsString();

        if (parameters.stream().noneMatch(parameter -> !parameter.isVarArgs()
                && parameter.getType().equals(varargsParameter.getType()))) {
            return Optional.empty();
        }

        BlockStmt body = methodDeclaration.getBody().get();
        IfStmtVisitor ifStmtVisitor = new IfStmtVisitor();
        body.accept(ifStmtVisitor, null);
        List<IfStmt> ifStmts = ifStmtVisitor.getIfStmts();

        if (ifStmts.isEmpty()) {
            return Optional.of(DetectedInstance.from(methodDeclaration, perfume));
        }

        for (IfStmt ifStmt : ifStmts) {
            if (isIfStmtNotPerfumed(ifStmt, varargsParamName)) {
                return Optional.empty();
            }
        }

        return Optional.of(DetectedInstance.from(methodDeclaration, perfume));
    }

    /**
     * Checks whether the given {@link IfStmt} is not perfumed, meaning it checks for the length
     * of the Varargs-parameter and depending on that immediately quits the method.
     *
     * @param ifStmt The {@link IfStmt} to check.
     * @param varargsParamName The name of the Varargs-parameter of the method.
     * @return {@code true} if the {@link IfStmt} is not perfumed, as described above.
     */
    private boolean isIfStmtNotPerfumed(@NotNull IfStmt ifStmt, @NotNull String varargsParamName) {
        BinaryExprVisitor binaryExprVisitor = new BinaryExprVisitor();
        ifStmt.accept(binaryExprVisitor, null);
        List<BinaryExpr> binaryExpressions = binaryExprVisitor.getBinaryExpressions();

        // Go through all Binary expressions, see if they compare the Varargs length to an Integer
        for (BinaryExpr binaryExpr : binaryExpressions) {
            BinaryExpr.Operator operator = binaryExpr.getOperator();
            if (operator == null || !OPERATORS_FOR_LENGTH_CHECK.contains(operator)) {
                continue;
            }

            Expression left = binaryExpr.getLeft();
            Expression right = binaryExpr.getRight();

            boolean isNotPerfumed = false;
            if (left.isFieldAccessExpr() && right.isIntegerLiteralExpr()) {
                isNotPerfumed = doesCheckLengthAndEndMethod(ifStmt, binaryExpr, operator, left.asFieldAccessExpr(),
                        right.asIntegerLiteralExpr(), varargsParamName);

            } else if (left.isIntegerLiteralExpr() && right.isFieldAccessExpr()) {
                isNotPerfumed = doesCheckLengthAndEndMethod(ifStmt, binaryExpr, operator, right.asFieldAccessExpr(),
                        left.asIntegerLiteralExpr(), varargsParamName);
            }

            if (isNotPerfumed) {
                return true;
            }
        }

        return false;
    }

    /**
     * Checks whether the {@link IfStmt} and {@link BinaryExpr} combined perform a check on the length of the
     * Varargs-Parameter of the method and
     * whether it quits the method (through a return/throws-Statement) in that case.<br/>
     * E.g. returns {@code true} for the following case:<br/>
     * <pre>
     * void method(String... args) {
     *     if (args.length &lt; 5) return;
     *     doSomething();
     * }
     * </pre>
     * @param ifStmt The {@link IfStmt} to check.
     * @param binaryExpr The {@link BinaryExpr} that is looked at.
     * @param operator The operator of the {@link BinaryExpr} that is looked at.
     * @param fieldAccessExpr The part of the {@link BinaryExpr} that performs the field access on the varargs param.
     * @param numberToCompareExpr The part of the {@link BinaryExpr} that is compared to the value obtained through the
     *                            field access. Usually an Integer.
     * @param varargsParamName The name of the Varargs parameter of the method.
     * @return {@code true} if the {@link IfStmt} immediately quits the method in dependence of the length check of the Varargs.
     */
    private boolean doesCheckLengthAndEndMethod(@NotNull IfStmt ifStmt, @NotNull BinaryExpr binaryExpr,
                                                @NotNull BinaryExpr.Operator operator, @NotNull FieldAccessExpr fieldAccessExpr,
                                                @NotNull IntegerLiteralExpr numberToCompareExpr, @NotNull String varargsParamName) {

        // Check if the Field access is actually on the Varargs parameter
        NameExpr fieldName;
        if (fieldAccessExpr.getScope() == null || !fieldAccessExpr.getScope().isNameExpr()) {
            return false;
        } else {
            fieldName = fieldAccessExpr.getScope().asNameExpr();
        }

        if (!varargsParamName.equals(fieldName.getName().getIdentifier())
                || !"length".equals(fieldAccessExpr.getName().getIdentifier())) {
            return false;
        }

        // Dependent on the (negated) comparison, we look at the then/else branch
        Statement branchToCheck;
        boolean isNegated = binaryExpr.getParentNode().filter(node -> node instanceof EnclosedExpr)
                .flatMap(Node::getParentNode).map(node -> {
                    if (node instanceof UnaryExpr expr) {
                        return UnaryExpr.Operator.LOGICAL_COMPLEMENT.equals(expr.getOperator());
                    }

                    return false;
                }).orElse(false);

        switch (operator) {
            case LESS, LESS_EQUALS -> {
                branchToCheck = isNegated ? ifStmt.getElseStmt().orElse(null) : ifStmt.getThenStmt();
            }

            case GREATER, GREATER_EQUALS -> {
                branchToCheck = isNegated ? ifStmt.getThenStmt() : ifStmt.getElseStmt().orElse(null);
            }

            default -> {
                return false;
            }
        }

        return branchToCheck != null && isMethodImmediatelyTerminated(branchToCheck);
    }

    /**
     * Check if the branch (given as a {@link Statement}) immediately quits the method
     * through a return/throws statement.<br/>
     * Returns {@code true} for the following cases:
     * <ul>
     *     <li>The Statement itself is a return/throws statement</li>
     *     <li>The Statement is a {@link BlockStmt}, with its first contained Statement
     *     being a return/throws statement</li>
     * </ul>
     *
     * @param statement The statement (branch of an if-statement)
     * @return see above
     */
    private boolean isMethodImmediatelyTerminated(@NotNull Statement statement) {
        if (statement.isReturnStmt() || statement.isThrowStmt()) {
            return true;
        }

        BlockStmt block;
        if (statement.isBlockStmt()) {
            block = statement.asBlockStmt();
        } else {
            return false;
        }

        NodeList<Statement> statementsInBlock = block.getStatements();
        if (statementsInBlock != null && statementsInBlock.getFirst().isPresent()) {
            Statement first = statementsInBlock.getFirst().get();

            return first.isReturnStmt() || first.isThrowStmt();
        }

        return false;
    }

}
