package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.ThrowStmt;
import com.github.javaparser.resolution.declarations.*;
import com.github.javaparser.resolution.types.ResolvedPrimitiveType;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.utils.Pair;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.visitor.IfStmtVisitor;
import de.jsilbereisen.perfumator.engine.visitor.TypeVisitor;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static de.jsilbereisen.perfumator.util.NodeUtil.*;

/**
 * <p>
 * {@link Detector} for the "Iterator next() follows the contract" {@link Perfume}. Note that this
 * implementation checks whether {@link Iterator#hasNext()} or a boolean variable with a fitting name is used in
 * the if-Statement where the expected {@link NoSuchElementException} shall be thrown. So there are going to be
 * cases that are missed, due to unexpected variable names for example.
 * </p>
 * <p>
 *     <b>Note 2023-08-15:</b> we avoid calling {@link de.jsilbereisen.perfumator.util.NodeUtil#safeCheckAssignableBy}
 *     here now for checking a type implements {@link Iterator}, because this causes a lot of issues with infinite recursion,
 *     and thus causing {@link StackOverflowError}s. This is most likely caused by a <i>JavaParser</i> bug, as {@link com.github.javaparser.symbolsolver.reflectionmodel.ReflectionInterfaceDeclaration#isAssignableBy}
 *     might call {@code other.canBeAssignedTo(this)}, and if {@code other} is a {@link com.github.javaparser.symbolsolver.javassistmodel.JavassistInterfaceDeclaration} or
 *     similar (basically something that does not override {@link ResolvedReferenceTypeDeclaration#canBeAssignedTo}),
 *     it will call back on the reflected iterator.
 * </p>
 */
@Slf4j
@EqualsAndHashCode
public class IteratorNextContractDetector implements Detector<Perfume> {

    public static final String ITERATOR_QUALIFIED = "java.util.Iterator";

    public static final List<String> PERMITTED_CONDITION_VAR_NAMES = List.of("hasNext", "next", "isNext");

    private Perfume perfume;

    @Nullable
    private JavaParserFacade analysisContext;

    @Override
    public @NotNull List<DetectedInstance<Perfume>> detect(@NotNull CompilationUnit astRoot) {
        if (analysisContext == null) {
            log.debug("Detector \"" + getClass().getSimpleName() + "\" has no required analysis context "
                    + "(ReflectionTypeSolver). No Perfumes can be reliably detected, skipping analysis.");
            return Collections.emptyList();
        }

        List<DetectedInstance<Perfume>> detections = new ArrayList<>();

        TypeVisitor typeVisitor = new TypeVisitor();
        astRoot.accept(typeVisitor, null);
        List<ClassOrInterfaceDeclaration> types = typeVisitor.getClassOrInterfaceDeclarations();

        for (ClassOrInterfaceDeclaration type : types) {
            analyseType(type).ifPresent(detections::add);
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

    @NotNull
    private Optional<DetectedInstance<Perfume>> analyseType(@NotNull ClassOrInterfaceDeclaration type) {
        Optional<ResolvedType> iteratorTypeArgument = checkImplementsIterator(type);
        if (iteratorTypeArgument.isEmpty() || !iteratorTypeArgument.get().isReferenceType()) {
            return Optional.empty();
        }

        ResolvedReferenceType iteratorType = iteratorTypeArgument.get().asReferenceType();

        Optional<MethodDeclaration> nextMethod = type.getMethods().stream()
                .filter(method -> {
                    boolean hasCorrectName = method.getNameAsString().equals("next");
                    boolean hasCorrectReturnType = method.getType().asString().equals(iteratorType.getTypeDeclaration().map(ResolvedDeclaration::getName).orElse("....."));
                    boolean hasNoParameters = method.getParameters().isEmpty();

                    return method.isPublic() && hasCorrectName && hasCorrectReturnType && hasNoParameters;
                }).findFirst();

        if (nextMethod.isEmpty()) {
            return Optional.empty();
        }

        boolean isNextPerfumed = analyseNextMethod(nextMethod.get());

        return isNextPerfumed
                ? Optional.of(DetectedInstance.from(nextMethod.get(), perfume, type))
                : Optional.empty();
    }

    @NotNull
    private Optional<ResolvedType> checkImplementsIterator(@NotNull ClassOrInterfaceDeclaration type) {
        // Resolve the type's declaration
        Optional<ResolvedReferenceTypeDeclaration> resolvedTypeDecl = resolveSafely(type, this, type.getNameAsString());
        if (resolvedTypeDecl.isEmpty() || !resolvedTypeDecl.get().isClass()) {
            return Optional.empty();
        }

        ResolvedClassDeclaration resolvedClass = resolvedTypeDecl.get().asClass();
        Optional<List<ResolvedReferenceType>> implementedInterfaces = safeResolutionAction(resolvedClass::getAllInterfaces);

        if (implementedInterfaces.isEmpty()) {
            return Optional.empty();
        }

        Optional<ResolvedReferenceType> iterator = implementedInterfaces.get().stream().filter(interfaze -> interfaze.getQualifiedName().equals(ITERATOR_QUALIFIED))
                .findFirst();
        if (iterator.isEmpty()) {
            return Optional.empty();
        }

        // Find the type argument, which concrete Iterator is implemented
        List<Pair<ResolvedTypeParameterDeclaration, ResolvedType>> iteratorTypeParams = iterator.get().getTypeParametersMap();
        if (iteratorTypeParams.size() != 1) {
            return Optional.empty();
        }

        return Optional.of(iteratorTypeParams.get(0).b);
    }

    private boolean analyseNextMethod(@NotNull MethodDeclaration methodDeclaration) {
        if (methodDeclaration.getBody().isEmpty()) {
            return false;
        }

        BlockStmt body = methodDeclaration.getBody().get();

        IfStmtVisitor ifStmtVisitor = new IfStmtVisitor();
        body.accept(ifStmtVisitor, null);
        List<IfStmt> ifStmts = ifStmtVisitor.getIfStmts();

        return ifStmts.stream().anyMatch(ifStmt -> isPerfumed(ifStmt, body));
    }

    private boolean isPerfumed(@NotNull IfStmt ifStmt, @NotNull BlockStmt methodBody) {
        Expression condition = ifStmt.getCondition();

        // If we check like "if (!hasNext()) {...}" we need to check the then-branch for the throws-stmt
        UnaryExpr unary = as(condition, UnaryExpr.class);
        if (unary != null) {
            boolean checksForHasNext = checksForHasNext(unary.getExpression());
            boolean isComplemented = unary.getOperator().equals(UnaryExpr.Operator.LOGICAL_COMPLEMENT);

            if (checksForHasNext && isComplemented) {
                return checkForExpectedException(ifStmt.getThenStmt());
            }
        }

        // If we check like "if (hasNext()) {...}" we need to check the else-branch for the throws-stmt
        // or the last statement of the method's body
        if (checksForHasNext(condition)) {
            if (ifStmt.getElseStmt().isPresent()) {
                return checkForExpectedException(ifStmt.getElseStmt().get());

            } else if (methodBody.getStatements().getLast().isPresent()) {
                return checkForExpectedException(methodBody.getStatements().getLast().get());
            }
        }

        // No check for hasNext() or similar was performed ~> should not throw exception without checking
        return false;
    }

    private boolean checksForHasNext(Expression expression) {
        // Check if the interface's hasNext() method is called. If yes, return true
        MethodCallExpr methodCall = as(expression, MethodCallExpr.class);
        if (methodCall != null) {
            Optional<ResolvedMethodDeclaration> calledMethod = resolveSafely(methodCall, this, methodCall.getNameAsString());
            if (calledMethod.isEmpty()) {
                return false;
            }

            /*
             * If the method is called hasNext() it will (currently, 10.07.23) always be resolved from
             * the reflected interface, even if it is not implemented. See IteratorNextContractNotPerfumed.java test file.
             *
             * This should not be a "real" problem though, as the code just would not compile if you dont implement the method
             * anyway - so in real scenarios, this doesn't really make a difference.
             */
            // Validate the name of the called method
            if (!calledMethod.get().getName().equals("hasNext")) {
                return false;
            }

            // If the called method has any param, it cant be the correct hasNext() method
            if (calledMethod.get().getNumberOfParams() > 0) {
                return false;
            }

            // Validate the method indeed returns a boolean - that should at this point be enough that we can assume
            // that the right hasNext() method is called, given all the previous contextual checks
            ResolvedPrimitiveType returnType = as(calledMethod.get().getReturnType(), ResolvedPrimitiveType.class);
            if (returnType == null) {
                return false;
            }
            return returnType.isBoolean();
        }

        // Check if a boolean variable with a fitting name is used
        NameExpr variableName = as(expression, NameExpr.class);
        if (variableName != null) {
            if (!PERMITTED_CONDITION_VAR_NAMES.contains(variableName.getNameAsString())) {
                return false;
            }

            Optional<ResolvedValueDeclaration> resolvedVariable = resolveSafely(variableName, this, variableName.getNameAsString());
            if (resolvedVariable.isEmpty()) {
                return false;
            }

            ResolvedPrimitiveType varType = as(resolvedVariable.get().getType(), ResolvedPrimitiveType.class);
            if (varType == null) {
                return false;
            }

            return varType.isBoolean();
        }

        return false;
    }

    private boolean checkForExpectedException(@NotNull Statement statement) {
        BlockStmt block = as(statement, BlockStmt.class);
        if (block != null) {
            Optional<ThrowStmt> throwStmt = block.findFirst(ThrowStmt.class);
            return throwStmt.filter(this::checkThrowsStmt).isPresent();

        }

        ThrowStmt throwStmt = as(statement, ThrowStmt.class);
        if (throwStmt != null) {
            return checkThrowsStmt(throwStmt);
        }

        return false;
    }

    private boolean checkThrowsStmt(@NotNull ThrowStmt throwStmt) {
        ObjectCreationExpr exception = as(throwStmt.getExpression(), ObjectCreationExpr.class);
        if (exception == null) {
            return false;
        }

        // Resolution of the type seems a bit overkill here - it seems very unlikely in this context, that it
        // is not the right "NoSuchElementException"
        return exception.getType().getNameAsString().equals("NoSuchElementException");
    }

}
