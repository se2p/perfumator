package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.AccessSpecifier;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import lombok.EqualsAndHashCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.visitor.TypeVisitor;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import de.jsilbereisen.perfumator.util.MutablePair;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static de.jsilbereisen.perfumator.util.NodeUtil.as;
import static de.jsilbereisen.perfumator.util.NodeUtil.asOrElse;
import static de.jsilbereisen.perfumator.util.NodeUtil.getNameWithTypeParams;

/**
 * Detects the "Equals blueprint" {@link Perfume}. The Perfume is inspired by the book
 * "Effective Java" of Joshua Bloch, in which he describes a blueprint for holding true
 * to the contract of the {@link Object#equals} method when overriding it.<br/>
 * This detector checks whether the following steps are taken:
 *
 * <ul>
 *     <li>First: check for reference-equality of {@code this} and the given {@link Object}.
 *     If the reference is the same, return {@code true}.</li>
 *
 *     <li>Then: check the runtime type of the given object with the {@code instanceof} operator.<br/>
 *     The detector allows certain ways to do this:
 *     in an elfe-if branch of the reference-equality check or in an own if-statement right after the
 *     reference-equality check, as well as with or without pattern-matching.<br/>
 *     Either way, the condition must look like "!(other instanceof MyClass)", to avoid unnecessary else-branches,
 *     and {@code false} must be returned immediately if that condition yields {@code true}.</li>
 *
 *     <li>Last: check for a correct cast, if no pattern-matching was used in the instanceof-check.</li>
 * </ul>
 */
@EqualsAndHashCode
public class EqualsBlueprintDetector implements Detector<Perfume> {

    private static final String EQUALS_SIGNATURE = "public boolean equals(Object)";

    private Perfume perfume;

    private JavaParserFacade analysisContext;

    /**
     * Starting point for detecting the {@link Perfume}.
     * For the checked steps, see the classes' JavaDoc comment.
     * Enum- and Annotation-types are ignored as implementing {@code equals}
     * for those does not make sense.
     *
     * @param astRoot The root node of the AST in which the {@link Perfume} should be searched for.
     * @return The list of found Perfume instances.
     */
    @Override
    public @NotNull List<DetectedInstance<Perfume>> detect(@NotNull CompilationUnit astRoot) {
        List<DetectedInstance<Perfume>> detectedInstances = new ArrayList<>();

        TypeVisitor typeVisitor = new TypeVisitor();
        astRoot.accept(typeVisitor, null);

        // Ignore Enums and Annotations
        List<TypeDeclaration<?>> relevantTypes = new ArrayList<>();
        relevantTypes.addAll(typeVisitor.getClassOrInterfaceDeclarations());
        relevantTypes.addAll(typeVisitor.getRecordDeclarations());

        for (TypeDeclaration<?> type : relevantTypes) {
            Optional<DetectedInstance<Perfume>> detected = analyseType(type);

            detected.ifPresent(detectedInstances::add);
        }

        return detectedInstances;
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
     * Analyses a single type for the Perfume.
     *
     * @param type The type to analyse.
     * @return An {@link Optional} with the detected Perfume instance if one was found. Otherwise,
     * returns {@link Optional#empty()}.
     */
    private Optional<DetectedInstance<Perfume>> analyseType(@NotNull TypeDeclaration<?> type) {
        // Search for a method that overrides Object#equals
        Optional<MethodDeclaration> equalsMethod = type.getMethods().stream()
                .filter(method -> method.getAccessSpecifier().equals(AccessSpecifier.PUBLIC))
                .filter(method -> {
                    String methodDeclaration = method.getDeclarationAsString(true, true, false);
                    return EQUALS_SIGNATURE.equals(methodDeclaration);
                })
                .findFirst();

        if (equalsMethod.isEmpty()) {
            return Optional.empty();
        }

        Optional<BlockStmt> methodBody = equalsMethod.get().getBody();
        if (methodBody.isEmpty()) {
            return Optional.empty();
        }

        Optional<Parameter> param = equalsMethod.get().getParameterByType("Object");
        if (param.isEmpty()) {
            return Optional.empty();
        }
        String paramName = param.get().getNameAsString();

        // Need to capture generic type parameters with the name, to check for correct instanceof check & casting
        String typeNameWithGenerics = type.getNameAsString();
        if (type.isClassOrInterfaceDeclaration()) {
            typeNameWithGenerics = getNameWithTypeParams(type.asClassOrInterfaceDeclaration());
        }

        // Analyse the found equals-method
        Optional<DetectedInstance<Perfume>> detectedInstance = analyseEqualsMethod(methodBody.get(), paramName, typeNameWithGenerics);
        detectedInstance.ifPresent(det -> det.setTypeName(type.getNameAsString())); // Dont want generics params here

        return detectedInstance;
    }

    /**
     * Analyses the body of the {@code equals} method. See the classes' JavaDoc for the steps that are checked.
     *
     * @param equalsMethodBody The {@link BlockStmt} that represents the {@code equals} method's body.
     * @param paramName        The identifier of the {@link Object} parameter of the method.
     * @param typeName         The name of the type <b>with generic parameters</b> that contains this {@code equals} method.
     * @return An {@link Optional} with the detected Perfume instance if one was found. Otherwise,
     * returns {@link Optional#empty()}.
     */
    private Optional<DetectedInstance<Perfume>> analyseEqualsMethod(@NotNull BlockStmt equalsMethodBody,
                                                                    @NotNull String paramName,
                                                                    @NotNull String typeName) {
        NodeList<Statement> bodyStatements = equalsMethodBody.getStatements();

        if (bodyStatements.isEmpty()) {
            return Optional.empty();
        }

        Statement first = bodyStatements.get(0);
        if (!isCheckingReferenceEquality(first, paramName)) {
            return Optional.empty();
        }

        IfStmt firstIfStmt = first.asIfStmt();
        // In case the type is checked in else-if branch
        MutablePair<Boolean, Boolean> isCheckingType = new MutablePair<>(false, false);
        int indexOfCastStmt = -1;

        if (firstIfStmt.hasElseBranch()) {
            isCheckingType = isCheckingTypeInElseIf(firstIfStmt, paramName, typeName);
            indexOfCastStmt = 1;
        } else if (bodyStatements.size() > 1) {
            isCheckingType = isCheckingType(bodyStatements.get(1), paramName, typeName);
            indexOfCastStmt = 2;
        }

        // No instanceof type check => no perfume
        boolean isCheckingTypeCorrectly = isCheckingType.getFirst();
        boolean usesPatternMatching = isCheckingType.getSecond();
        if (!isCheckingTypeCorrectly) {
            return Optional.empty();
        } else if (usesPatternMatching) {
            return Optional.of(DetectedInstance.from(equalsMethodBody, perfume, typeName));
        }

        // If no pattern-matching is used, we need to look for a cast
        // No statement at the position where the cast is expected => no perfume
        if (indexOfCastStmt < 0 || bodyStatements.size() <= indexOfCastStmt) {
            return Optional.empty();
        }

        Statement statement = bodyStatements.get(indexOfCastStmt);
        boolean isCorrectCast = isCorrectCast(statement, paramName, typeName);

        if (!isCorrectCast) {
            return Optional.empty();
        }

        return Optional.of(DetectedInstance.from(equalsMethodBody, perfume, typeName));
    }

    /**
     * Analyses whether the given statement performs a check for reference-equality (==).
     * For this to be the case, the statement must be an if-statement, and the condition must look like
     * "{@code this == other}" (or "{@code other == this}"), where "other" is the equals-method's parameter identifier.
     * Also, in the then-branch, {@code true} must be returned.
     *
     * @param stmt      The statement to check.
     * @param paramName The parameter name of the {@code equals} method.
     * @return {@code true} if all the steps described above are taken correctly.
     */
    private boolean isCheckingReferenceEquality(@NotNull Statement stmt, @NotNull String paramName) {
        if (!stmt.isIfStmt()) {
            return false;
        }

        IfStmt ifStmt = stmt.asIfStmt();
        Expression expression = ifStmt.getCondition();

        if (!expression.isBinaryExpr()) {
            return false;
        }

        BinaryExpr binaryExpr = expression.asBinaryExpr();

        // Check if any equality check happens
        if (!BinaryExpr.Operator.EQUALS.equals(binaryExpr.getOperator())) {
            return false;
        }

        // Check if an equality check between "this" and some variable happens
        ThisExpr thisExpr = asOrElse(binaryExpr.getLeft(), ThisExpr.class,
                () -> as(binaryExpr.getRight(), ThisExpr.class));
        NameExpr nameExpr = asOrElse(binaryExpr.getLeft(), NameExpr.class,
                () -> as(binaryExpr.getRight(), NameExpr.class));
        if (thisExpr == null || nameExpr == null) {
            return false;
        }

        // Check if the variable is actually the method's parameter
        if (!paramName.equals(nameExpr.getNameAsString())) {
            return false;
        }

        // Now see if a "return true" is performed in the then-branch
        return returnsInThenWithExpectedValue(ifStmt, true);
    }

    /**
     * Checks whether an {@code instanceof} check for the correct type is performed in the first else-if branch
     * of the given if-statement. If that is the case, it is validated that the then-branch of this if-else returns
     * {@code false} immediately.
     *
     * @param ifStmt    The if-statement to check.
     * @param paramName The parameter name of the {@code equals} method.
     * @param typeName  The name of the type <b>with generic parameters</b> that contains this {@code equals} method.
     * @return {@code true} if all the steps described above are taken correctly.
     */
    private MutablePair<Boolean, Boolean> isCheckingTypeInElseIf(@NotNull IfStmt ifStmt, @NotNull String paramName,
                                                                 @NotNull String typeName) {
        Optional<Statement> elseBranch = ifStmt.getElseStmt();

        return elseBranch.map(statement -> isCheckingType(statement, paramName, typeName)).orElse(new MutablePair<>());
    }

    /**
     * Returns a {@link MutablePair} of booleans where the first value states whether a correct type check with the
     * {@code instanceof} operator is performed and the then-branch returns {@code false}.
     * The second value signals whether Pattern matching with the {@code instanceof} operator is used.
     *
     * @param statement The statement to check.
     * @param paramName The {@code equals} method's parameter name.
     * @param typeName  The name of the type <b>with generic parameters</b> that contains this {@code equals} method.
     * @return A {@link MutablePair} of booleans, with the semantics as described above.
     */
    @NotNull
    private MutablePair<Boolean, Boolean> isCheckingType(@NotNull Statement statement, @NotNull String paramName,
                                                         @NotNull String typeName) {
        MutablePair<Boolean, Boolean> ret = new MutablePair<>(false, false);
        IfStmt ifStmt = as(statement, IfStmt.class);
        if (ifStmt == null) {
            return ret;
        }

        // Do not support 'if (other instanceof Clazz) { return fieldChecks } else { return false; }'
        // because it adds unnecessary complexity to the method
        UnaryExpr unaryExpr = as(ifStmt.getCondition(), UnaryExpr.class);
        if (unaryExpr == null) {
            return ret;
        }
        if (unaryExpr.getOperator() != UnaryExpr.Operator.LOGICAL_COMPLEMENT) {
            return ret;
        }

        // Instanceof check needs to be in brackets
        EnclosedExpr enclosedExpr = as(unaryExpr.getExpression(), EnclosedExpr.class);
        if (enclosedExpr == null) {
            return ret;
        }

        // See whether an actual instanceof check is performed
        InstanceOfExpr innerExpr = as(enclosedExpr.getInner(), InstanceOfExpr.class);
        if (innerExpr == null) {
            return ret;
        }

        NameExpr nameExpr = as(innerExpr.getExpression(), NameExpr.class);
        ClassOrInterfaceType type = as(innerExpr.getType(), ClassOrInterfaceType.class);
        if (nameExpr == null || type == null) {
            return ret;
        }
        if (!paramName.equals(nameExpr.getNameAsString()) || !typeName.equals(type.asString())) {
            return ret;
        }

        // Check if the instanceof uses Pattern-matching
        innerExpr.getPattern().ifPresent(pattern -> {
            ClassOrInterfaceType patternType = as(pattern.getType(), ClassOrInterfaceType.class);

            if (patternType != null && typeName.equals(patternType.asString())) {
                ret.setSecond(true);
            }
        });

        return ret.setFirst(returnsInThenWithExpectedValue(ifStmt, false));
    }

    /**
     * Checks whether the statement performs a correct cast to the type that implements the {@code equals} method.
     *
     * @param statement The statement to check.
     * @param paramName The {@code equals} method's parameter name.
     * @param typeName  The name of the type <b>with generic parameters</b> that contains this {@code equals} method.
     * @return {@code true} if the statement performs a correct cast.
     */
    private boolean isCorrectCast(Statement statement, String paramName, String typeName) {
        // Statement needs to be an expression statement
        ExpressionStmt expressionStmt = as(statement, ExpressionStmt.class);
        if (expressionStmt == null) {
            return false;
        }

        // Needs to be a variable declaration
        VariableDeclarationExpr varDecl = as(expressionStmt.getExpression(), VariableDeclarationExpr.class);
        if (varDecl == null) {
            return false;
        }

        // Declaration should have exactly one variable
        NodeList<VariableDeclarator> variables = varDecl.getVariables();
        if (variables.size() != 1) {
            return false;
        }

        VariableDeclarator variable = variables.get(0);

        // Check the type
        ClassOrInterfaceType type = as(variable.getType(), ClassOrInterfaceType.class);
        if (type == null) {
            return false;
        }
        if (!typeName.equals(type.asString())) {
            return false;
        }

        // Check whether the variable is initialized through a cast to the correct type
        Optional<Expression> initializerExpr = variable.getInitializer();
        if (initializerExpr.isEmpty()) {
            return false;
        }

        CastExpr cast = as(initializerExpr.get(), CastExpr.class);
        if (cast == null) {
            return false;
        }

        NameExpr nameExpr = as(cast.getExpression(), NameExpr.class);
        ClassOrInterfaceType castType = as(cast.getType(), ClassOrInterfaceType.class);
        if (nameExpr == null || castType == null) {
            return false;
        }

        return paramName.equals(nameExpr.getNameAsString()) && typeName.equals(castType.asString());
    }

    /**
     * Checks whether the if-statement immediately performs a "return <i>boolean</i>" in the then-branch,
     * with the given expected boolean value being returned.
     *
     * @param ifStmt              The if-statement to check.
     * @param expectedReturnValue The expected boolean value to be returned.
     * @return {@code true} if the steps described above can be validated.
     */
    private boolean returnsInThenWithExpectedValue(@NotNull IfStmt ifStmt, boolean expectedReturnValue) {
        Statement then = ifStmt.getThenStmt();
        ReturnStmt returnStmt = null;

        // Might look like "if (...) return" or like "if (...) { return }"
        BlockStmt thenBlock = as(then, BlockStmt.class);
        if (thenBlock != null) {
            Optional<Statement> firstInThenBlock = thenBlock.getStatements().getFirst();

            if (firstInThenBlock.isPresent()) {
                returnStmt = as(firstInThenBlock.get(), ReturnStmt.class);
            }
        } else {
            returnStmt = as(then, ReturnStmt.class);
        }

        if (returnStmt == null) {
            return false;
        }

        Optional<Expression> returnExpr = returnStmt.getExpression();
        if (returnExpr.isEmpty()) {
            return false;
        }

        BooleanLiteralExpr booleanToReturn = as(returnExpr.get(), BooleanLiteralExpr.class);
        if (booleanToReturn == null) {
            return false;
        }

        return booleanToReturn.getValue() == expectedReturnValue;
    }
}
