package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.visitor.BinaryExprVisitor;
import de.jsilbereisen.perfumator.engine.visitor.TypeVisitor;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.jsilbereisen.perfumator.util.NodeUtil.as;
import static de.jsilbereisen.perfumator.util.NodeUtil.asOrElse;

/**
 * <p>
 * {@link Detector} for the "Defensive null check" Perfume. As missing {@code null} checks is a common
 * bug pattern and can lead to unpleasant {@link NullPointerException}s at runtime, the Perfume puts emphasis
 * on this problem. Precisely, the detector analyses all public (implemented, e.g. not abstract) methods on
 * whether they either give clear signals about the handling of {@code null} values for their non-primitive parameters
 * in their signature, e.g. via annotations, or whether these parameters
 * are checked against the {@code null} literal (e.g. "==" or "!=") in the method's body.
 * </p>
 * <p>
 * Additional details:
 * <ul>
 *     <li>Currently, varargs Parameters are ignored in the analysis (no influence towards being perfumed or not),
 *     even though they could also cause an NPE when passing a single {@code null} value.</li>
 *
 *     <li>The list of names of annotations that make a parameter "perfumed" (= does not need to be checked in
 *     the method's body anymore) is given by {@link #ANNOTATION_NAMES}</li>
 *
 *     <li>
 *         A method is checked when all of these conditions apply:
 *         <ul>
 *             <li>it is public</li>
 *             <li>it has at least one non-primitive parameter</li>
 *             <li>it is not abstract / has a default implementation if it is in an interface</li>
 *         </ul>
 *     </li>
 *
 *     <li>The method is then seen as perfumed, when all of its non-primitive (and non-varargs) parameters have
 *     a perfuming annotation ({@link #ANNOTATION_NAMES}) or are checked against {@code null} in the
 *     method's body. If the method has only primitive Parameters (or none at all), it is not
 *     perfumed.</li>
 * </ul>
 * </p>
 * <p>
 *     For future extensions, one might consider checking for common utility methods to check for {@code null},
 *     such as {@link java.util.Objects#requireNonNull}.
 * </p>
 * <p>
 *     The perfume was inspired by:<br/>
 *     H. Osman, M. Lungu and O. Nierstrasz, "Mining frequent bug-fix code changes," 2014 Software Evolution Week -
 *     IEEE Conference on Software Maintenance, Reengineering, and Reverse Engineering (CSMR-WCRE), Antwerp, Belgium, 2014, pp. 343-347, doi: 10.1109/CSMR-WCRE.2014.6747191
 * </p>
 */
public class DefensiveNullCheckDetector implements Detector<Perfume> {

    private static final List<String> ANNOTATION_NAMES = List.of("NotNull", "Nonnull", "Nullable");

    private Perfume perfume;

    @Override
    public @NotNull List<DetectedInstance<Perfume>> detect(@NotNull CompilationUnit astRoot) {
        List<DetectedInstance<Perfume>> detectedInstances = new ArrayList<>();

        TypeVisitor typeVisitor = new TypeVisitor();
        astRoot.accept(typeVisitor, null);

        List<TypeDeclaration<?>> types = typeVisitor.getAllTypeDeclarations();

        for (TypeDeclaration<?> type : types) {
            List<DetectedInstance<Perfume>> detectedInType = analyseType(type);

            detectedInstances.addAll(detectedInType);
        }

        return detectedInstances;
    }

    @Override
    public void setConcreteDetectable(@NotNull Perfume concreteDetectable) {
        perfume = concreteDetectable;
    }

    @NotNull
    private List<DetectedInstance<Perfume>> analyseType(@NotNull TypeDeclaration<?> type) {
        List<DetectedInstance<Perfume>> detectedInstances = new ArrayList<>();

        ClassOrInterfaceDeclaration classOrInterfaceDeclaration = as(type, ClassOrInterfaceDeclaration.class);

        // Find all public methods that are not abstract/non-default in interface
        List<MethodDeclaration> methodsToCheck = new ArrayList<>(type.getMethods()); // TODO: test other detectors whether they modify immutable getMethods() list
        methodsToCheck.removeIf(method -> !needsToBeChecked(method, classOrInterfaceDeclaration));

        for (MethodDeclaration method : methodsToCheck) {
            if (isMethodPerfumed(method)) {
                detectedInstances.add(DetectedInstance.from(method, perfume, type));
            }
        }

        return detectedInstances;
    }

    private boolean needsToBeChecked(@NotNull MethodDeclaration methodDeclaration,
                                     @Nullable ClassOrInterfaceDeclaration classOrInterfaceDeclaration) {
        boolean isInterface = classOrInterfaceDeclaration != null && classOrInterfaceDeclaration.isInterface();

        // Check all public methods that are not in an interface; if in interface, only check those that are default and non-private, non-protected
        boolean checkBecauseOfAccess = (methodDeclaration.isPublic() && !isInterface)
                || (isInterface && methodDeclaration.isDefault()
                    && !(methodDeclaration.hasModifier(Modifier.Keyword.PRIVATE)
                        || methodDeclaration.hasModifier(Modifier.Keyword.PROTECTED)));

        boolean hasNonPrimitiveParameter = methodDeclaration.getParameters().isNonEmpty()
                && methodDeclaration.getParameters().stream().anyMatch(parameter -> !parameter.getType().isPrimitiveType());

        return !methodDeclaration.isAbstract() && checkBecauseOfAccess && hasNonPrimitiveParameter;
    }

    private boolean isMethodPerfumed(@NotNull MethodDeclaration methodDeclaration) {
        List<Parameter> parameters = new ArrayList<>(methodDeclaration.getParameters()); // TODO: check other detectors for accidentally manipulating the AST

        // See if there is a non-primitive, non Varargs param with one of the perfuming annotations
        boolean anyParamRelevantWithAnnotation = parameters.stream().anyMatch(param -> isRelevantWithAnnotation(param));

        // Clear the list of parameters from all that do not need to be checked in the method's body
        parameters.removeIf(parameter -> !needsToBeChecked(parameter));

        // If we do not have any Parameters to check in the method's body and we have at least one param with a perfuming annotation => perfumed
        if (parameters.isEmpty() && anyParamRelevantWithAnnotation) {
            return true;
        }

        return areParametersCheckedForNull(methodDeclaration, parameters);
    }

    private boolean isRelevantWithAnnotation(@NotNull Parameter parameter) {
        if (parameter.getType().isPrimitiveType()) {
            return false;
        }
        if (parameter.isVarArgs()) {
            return false;
        }

        List<AnnotationExpr> annotations = new ArrayList<>(parameter.getAnnotations());
        if (annotations.isEmpty()) {
            return false;
        }

        return annotations.stream().anyMatch(annotationExpr -> ANNOTATION_NAMES.contains(annotationExpr.getNameAsString()));
    }

    private boolean needsToBeChecked(@NotNull Parameter parameter) {
        if (parameter.getType().isPrimitiveType()) {
            return false;
        }

        if (parameter.isVarArgs()) {
            return false;
        }

        NodeList<AnnotationExpr> annotations = parameter.getAnnotations();
        if (annotations.isEmpty()) {
            return true;
        }

        return annotations.stream().noneMatch(annotationExpr -> ANNOTATION_NAMES.contains(annotationExpr.getNameAsString()));
    }

    private boolean areParametersCheckedForNull(@NotNull MethodDeclaration methodDeclaration, @NotNull List<Parameter> parameters) {
        if (parameters.isEmpty()) {
            return false; // No parameters to check => no perfume.
        }

        Optional<BlockStmt> body = methodDeclaration.getBody();
        if (body.isEmpty()) {
            return false;
        }

        // Search for all Binary Expressions that perform a "==" check in the method.
        BinaryExprVisitor binaryExprVisitor = new BinaryExprVisitor();
        methodDeclaration.accept(binaryExprVisitor, null);
        List<BinaryExpr> binaryExprs = binaryExprVisitor.getBinaryExpressions();

        // Only checks for "==" and "!=" are interesting
        binaryExprs.removeIf(expr -> expr.getOperator() != BinaryExpr.Operator.EQUALS
                && expr.getOperator() != BinaryExpr.Operator.NOT_EQUALS);

        List<String> paramsThatNeedToBeChecked = parameters.stream().map(NodeWithSimpleName::getNameAsString)
                .collect(Collectors.toList());

        for (BinaryExpr binaryExpr : binaryExprs) {
            NameExpr nameExpr = asOrElse(binaryExpr.getLeft(), NameExpr.class,
                    () -> as(binaryExpr.getRight(), NameExpr.class));
            NullLiteralExpr nullLiteral = asOrElse(binaryExpr.getLeft(), NullLiteralExpr.class,
                    () -> as(binaryExpr.getRight(), NullLiteralExpr.class));

            if (nameExpr == null || nullLiteral == null) {
                continue;
            }

            // Remove the Parameter from the list of those that remain to be checked
            paramsThatNeedToBeChecked.remove(nameExpr.getNameAsString());
        }

        // All Params are (at least once) checked for null when the List is empty.
        return paramsThatNeedToBeChecked.isEmpty();
    }
}
