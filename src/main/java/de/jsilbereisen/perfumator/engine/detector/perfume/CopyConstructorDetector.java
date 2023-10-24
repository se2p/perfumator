package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.nodeTypes.NodeWithTypeParameters;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.Resolvable;
import com.github.javaparser.resolution.declarations.ResolvedParameterDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.visitor.TypeVisitor;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;
import de.jsilbereisen.perfumator.util.NodeUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static de.jsilbereisen.perfumator.util.NodeUtil.as;
import static de.jsilbereisen.perfumator.util.NodeUtil.resolveSafely;
import static de.jsilbereisen.perfumator.util.NodeUtil.safeResolutionAction;

// TODO: What if the field is a List, and just fieldname.addAll is called?
/**
 * <p>
 * {@link Detector} for the "Copy constructor" {@link Perfume}. Analyses applicable constructors of types in the AST
 * and checks whether fields are being copied.
 * </p>
 * <p>
 * For information on which fields are determined as having to be copied, see {@link #filterClassFields}. For
 * filtering of the constructors, see {@link #findCopyConstructors}.
 * For the analysis of the constructors, see {@link #isPerfumedCopyConstructor} and the methods use in it.
 * </p>
 * <p>
 *     <b>Note 2023-08-15</b>: Unable to currently support {@link RecordDeclaration}s for the {@link Perfume},
 *     as {@link com.github.javaparser.symbolsolver.JavaSymbolSolver#toTypeDeclaration} does not support Records.
 * </p>
 * <p>
 *     <b>Note 2023-08-15:</b> we avoid calling {@link de.jsilbereisen.perfumator.util.NodeUtil#safeCheckAssignableBy}
 *  here now for checking type compatibility of the constructor arg, because this causes a lot of issues with infinite recursion,
 *  and thus causing {@link StackOverflowError}s. This is most likely caused by a <i>JavaParser</i> bug, as {@link com.github.javaparser.symbolsolver.reflectionmodel.ReflectionInterfaceDeclaration#isAssignableBy}
 *  might call {@code other.canBeAssignedTo(this)}, and if {@code other} is a {@link com.github.javaparser.symbolsolver.javassistmodel.JavassistInterfaceDeclaration} or
 *  similar (basically something that does not override {@link ResolvedReferenceTypeDeclaration#canBeAssignedTo}),
 *  it will call back on the reflected class.<br/>
 *  Same issue as in {@link IteratorNextContractDetector} or {@link CloneBlueprintDetector}.
 * </p>
 */
@EqualsAndHashCode
@Slf4j
public class CopyConstructorDetector implements Detector<Perfume> {

    /**
     * Set of method names that could indicate that the method's purpose is copying an object.
     */
    public static final Set<String> COPY_METHODS = Set.of("clone", "copy", "duplicate", "new", "create", "createCopy",
            "doCopy", "doClone", "arrayCopy", "listCopy", "copyList", "of", "from", "by");

    private Perfume perfume;

    private JavaParserFacade analysisContext;

    @Override
    public @NotNull List<DetectedInstance<Perfume>> detect(@NotNull CompilationUnit astRoot) {
        List<DetectedInstance<Perfume>> detections = new ArrayList<>();

        TypeVisitor typeVisitor = new TypeVisitor();
        astRoot.accept(typeVisitor, null);
        List<ClassOrInterfaceDeclaration> types = typeVisitor.getClassOrInterfaceDeclarations();

        for (TypeDeclaration<?> type : types) {
            detections.addAll(analyseType(type));
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
    private List<DetectedInstance<Perfume>> analyseType(@NotNull TypeDeclaration<?> type) {
        List<ConstructorDeclaration> potentialCopyConstructors = findCopyConstructors(type);

        if (potentialCopyConstructors.isEmpty()) {
            return Collections.emptyList();
        }

        List<VariableDeclarator> fieldsToCopy = filterClassFields(type);

        List<DetectedInstance<Perfume>> detections = new ArrayList<>();

        for (ConstructorDeclaration constructor : potentialCopyConstructors) {
            if (isPerfumedCopyConstructor(constructor, fieldsToCopy)) {
                detections.add(DetectedInstance.from(constructor, perfume, type));
            }
        }

        return detections;
    }

    /**
     * <p>
     * Returns a list of declared class variables, that should be copied in a copy constructor.
     * This excludes static fields and final fields that are initialized in the field declaration.
     * </p>
     * <p>
     * Does NOT consider initializer-blocks:
     * <pre>
     *     private final int x;
     *     {
     *         x = 1;
     *     }
     * </pre>
     * </p>
     *
     * @param type The type with the fields.
     * @return A filtered list of {@link VariableDeclarator}s, which are relevant for being copied.
     */
    @NotNull
    private List<VariableDeclarator> filterClassFields(@NotNull TypeDeclaration<?> type) {
        List<VariableDeclarator> relevantClassFieldVars = new ArrayList<>();

        type.getFields().forEach(field -> {
            if (field.isStatic()) {
                return;
            }

            if (!field.isFinal()) {
                relevantClassFieldVars.addAll(field.getVariables());

            } else {
                // If class variable is final and initialized, does not need to be copied
                field.getVariables().forEach(variableDeclarator -> {
                    if (variableDeclarator.getInitializer().isEmpty()) {
                        relevantClassFieldVars.add(variableDeclarator);
                    }
                });
            }
        });

        return relevantClassFieldVars;
    }

    /**
     * Return a list of constructors declared in the given {@link TypeDeclaration}, if it is a
     * {@link ClassOrInterfaceDeclaration} or a {@link RecordDeclaration}, which would match the signature of a
     * typical copy-constructor.
     *
     * @param type The type from which to filter the constructors.
     * @return A list of possible copy-constructors that should be further analysed.
     */
    @NotNull
    private List<ConstructorDeclaration> findCopyConstructors(@NotNull TypeDeclaration<?> type) {
        return type.getConstructors().stream().filter(constructor -> {
            if (type.isClassOrInterfaceDeclaration()) {
                return isPossibleCopyConstructor(constructor, type.asClassOrInterfaceDeclaration());

            } else if (type.isRecordDeclaration()) {
                return isPossibleCopyConstructor(constructor, type.asRecordDeclaration());

            } else {
                return false;
            }
        }).collect(Collectors.toList());
    }

    /**
     * <p>
     * Checks if the given {@link ConstructorDeclaration} is a candidate for being a copy-constructor.
     * The following criteria are applied:<br/>
     * <ul>
     *     <li>The constructor is not private.</li>
     *     <li>The constructor has exactly one parameter.</li>
     *     <li>The parameters type matches the type that declares it.</li>
     * </ul>
     * </p>
     * <p>
     * <b>2023-08-15</b> Records currently are not supported by {@link com.github.javaparser.symbolsolver.JavaSymbolSolver#toTypeDeclaration},
     * do not consider {@link RecordDeclaration}s anymore in {@link #detect}, as well as wrap the resolution of the param in
     * try-catch, as it could be a record.
     * </p>
     * @param constructor The constructor to check
     * @param type The type that declares the given constructor
     * @return {@code true} if the constructor might be a copy constructor, looking at its declaration
     * @param <T>
     */
    private <T extends Node & NodeWithTypeParameters<T> & NodeWithSimpleName<T> & Resolvable<ResolvedReferenceTypeDeclaration>>
    boolean isPossibleCopyConstructor(@NotNull ConstructorDeclaration constructor, @NotNull T type) {
        if (constructor.isPrivate()) {
            return false;
        }

        List<Parameter> params = constructor.getParameters();
        if (params.size() != 1) {
            return false;
        }

        Parameter singleParam = params.get(0);
        Type paramType = singleParam.getType();

        if (paramType.asString().equals(NodeUtil.getNameWithTypeParams(type))) {
            return true;
        }

        Optional<ResolvedParameterDeclaration> resolvedParam = resolveSafely(singleParam, this,
                constructor.getDeclarationAsString());
        if (resolvedParam.isEmpty()) {
            return false;
        }

        Optional<ResolvedReferenceTypeDeclaration> resolvedTypeDeclaration = resolveSafely(type, this,
                type.getNameAsString());
        if (resolvedTypeDeclaration.isEmpty()) {
            return false;
        }

        Optional<ResolvedType> resolvedParamType;
        try {
            resolvedParamType = safeResolutionAction(resolvedParam.get()::getType);
        } catch (Exception e) {
            log.debug("Exception when getting the resolved Parameter type.", e);
            return false;
        }
        if (resolvedParamType.isEmpty() || !resolvedParamType.get().isReferenceType()) {
            return false;
        }

        ResolvedReferenceType resolvedRefType = resolvedParamType.get().asReferenceType();
        Optional<ResolvedReferenceTypeDeclaration> resolvedParamTypeDecl = resolvedRefType.getTypeDeclaration();
        if (resolvedParamTypeDecl.isEmpty()) {
            return false;
        }

        return resolvedTypeDeclaration.get().getQualifiedName().equals(resolvedParamTypeDecl.get().getQualifiedName());
    }

    /**
     * Analyses whether the constructor is perfumed, meaning if it can be recognized as (or is likely) a
     * copy-constructor. For that purpose, all assignments to the given list of fields are analysed, whether they
     * perform a detectable form of copying.
     *
     * @param constructor The {@link ConstructorDeclaration} to analyse.
     * @param fieldsToCopy The fields for which some value should be assigned by copying it from the constructors
     *                     parameter.
     * @return {@code true} if all of the given fields have at least one assignment to them which is detected as
     * performing a copy from the same field from the method's parameter.
     */
    private boolean isPerfumedCopyConstructor(@NotNull ConstructorDeclaration constructor,
                                              @NotNull List<VariableDeclarator> fieldsToCopy) {
        Set<String> fieldNamesToCopy = new HashSet<>();
        fieldsToCopy.forEach(fieldVar -> fieldNamesToCopy.add(fieldVar.getNameAsString()));

        List<AssignExpr> assignmentsToCopyFields = constructor.findAll(AssignExpr.class,
                assignExpr -> assignsToCopyField(assignExpr, fieldNamesToCopy));

        // No assignments to any fields that should be copied => not perfumed
        if (assignmentsToCopyFields.isEmpty()) {
            return false;
        }

        // We know at this point that the constructor has exactly one parameter
        Parameter param = constructor.getParameter(0);
        String paramName = param.getNameAsString();

        // For each assignment, check if it performs some recognizable form of copying of the value from the parameter
        for (AssignExpr assign : assignmentsToCopyFields) {
            if (performsFieldCopy(assign, paramName)) {
                fieldNamesToCopy.remove(((NodeWithSimpleName<?>) assign.getTarget()).getNameAsString());
            }
        }

        return fieldNamesToCopy.isEmpty();
    }

    /**
     * Checks if the given {@link AssignExpr} performs an assignment on a target field, where the name is in the
     * given set of field names.
     *
     * @param assignExpr The {@link AssignExpr} to check.
     * @param fieldNames Allowed field names for the target of an assignment.
     * @return {@code true} if the assignment is like {@code x = ...}, where {@code x} is in the given set of field
     * names.
     */
    private boolean assignsToCopyField(@NotNull AssignExpr assignExpr,
                                       @NotNull Set<String> fieldNames) {
        if (!assignExpr.getOperator().equals(AssignExpr.Operator.ASSIGN)) {
            return false;
        }

        FieldAccessExpr fieldAccessExpr = as(assignExpr.getTarget(), FieldAccessExpr.class);
        if (fieldAccessExpr != null) {
            return fieldNames.contains(fieldAccessExpr.getNameAsString());
        }

        NameExpr nameExpr = as(assignExpr.getTarget(), NameExpr.class);
        if (nameExpr != null) {
            return fieldNames.contains(nameExpr.getNameAsString());
        }

        return false;
    }

    /**
     * Checks if the given {@link AssignExpr} copies a field of an object with the name {@code paramName} into a field
     * with the same name. Recognized copying-patterns are direct field access, call of a "copy"-like (semantically,
     * determined by its name) method or usage of a (likely) copy-constructor.
     *
     * @param assign The {@link AssignExpr} to check.
     * @param paramName The name of the constructor's (which contains the given assignment expression) parameter.
     * @return {@code true} if a recognized form of field-copying is performed, as described above.
     * @see #checkCopyCallOnField
     * @see #checkCopyFieldByMethodWithParam
     */
    private boolean performsFieldCopy(@NotNull AssignExpr assign, @NotNull String paramName) {
        String assignedFieldName = ((NodeWithSimpleName<?>) assign.getTarget()).getNameAsString();

        // Check if the field is copied per direct access (e.g. this.x = other.x)
        FieldAccessExpr assignedValueField = as(assign.getValue(), FieldAccessExpr.class);
        if (assignedValueField != null) {
            NameExpr scope = as(assignedValueField.getScope(), NameExpr.class);
            String fieldName = assignedValueField.getNameAsString();

            if (scope != null && scope.getNameAsString().equals(paramName) && fieldName.equals(assignedFieldName)) {
                return true;
            }
        }

        // Check if the field is copied through some method call (e.g. this.x = other.x.clone() or this.x = Util.copy
        // (other.x))
        MethodCallExpr assignedValueThroughMethodCall = as(assign.getValue(), MethodCallExpr.class);
        if (assignedValueThroughMethodCall != null) {
            if (checkCopyCallOnField(assignedValueThroughMethodCall, assignedFieldName, paramName)) {
                return true;
            }

            if (checkCopyFieldByMethodWithParam(assignedValueThroughMethodCall, assignedFieldName, paramName)) {
                return true;
            }
        }

        // Check if some kind of copy constructor is used
        ObjectCreationExpr someConstructorCall = as(assign.getValue(), ObjectCreationExpr.class);
        if (someConstructorCall != null
                && someConstructorCall.getArguments().size() == 1
                && isFieldAccessExprOnCopyField(assignedFieldName, paramName, someConstructorCall.getArgument(0))) {
            return true;
        }

        return false;
    }

    /**
     * Checks whether a field copy is performed in the shape like<br/>
     * <pre>
     *     this.x = other.x.copy();
     * </pre>
     * For the recognized names for copy-methods, see {@link #COPY_METHODS}.
     */
    private boolean checkCopyCallOnField(@NotNull MethodCallExpr methodCall, @NotNull String assignedFieldName,
                                         @NotNull String paramName) {
        // Copy method call on the field that is to copy should not need any arguments
        if (!methodCall.getArguments().isEmpty()) {
            return false;
        }

        if (methodCall.getScope().isEmpty()) {
            return false;
        }

        if (!COPY_METHODS.contains(methodCall.getNameAsString())) {
            return false;
        }

        return isFieldAccessExprOnCopyField(assignedFieldName, paramName, methodCall.getScope().get());
    }

    /**
     * Checks whether a field copy is performed in the shape like<br/>
     * <pre>
     *     this.x = doCopy(other.x);
     * </pre>
     * or possibly through a static call like
     * <pre>
     *     this.x = StaticUtilClass.doCopy(other.x);
     * </pre>
     * So, we don't make assumptions on the scope of the method call.<br/>
     * Also, we don't restrict the parameters in that case (as long as there are any), for example:
     * <pre>
     *     this.someList = UtilClass.copy(other.someList, someFlag, somePredicate);
     * </pre>
     * For the recognized names for copy-methods, see {@link #COPY_METHODS}.
     */
    private boolean checkCopyFieldByMethodWithParam(@NotNull MethodCallExpr methodCall,
                                                    @NotNull String assignedFieldName,
                                                    @NotNull String paramName) {
        // Copy method call that takes the field as param should have at least one argument
        if (methodCall.getArguments().isEmpty()) {
            return false;
        }

        if (!COPY_METHODS.contains(methodCall.getNameAsString())) {
            return false;
        }

        // Is any of the arguments of the method call the field that we want to have copied?
        return methodCall.getArguments().stream().anyMatch(
                expression -> isFieldAccessExprOnCopyField(assignedFieldName, paramName, expression));
    }

    /**
     * Checks whether the given {@link Expression} is a {@link FieldAccessExpr} and if so, checks whether the field
     * access is performed on the field with the name {@code fieldNameOfCopyField} and on the 0bject/class (= scope)
     * with the name {@code paramName}.
     *
     * @param fieldNameOfCopyField Name of the expected field to be accessed.
     * @param paramName Name of the expected object/class, where the field should be accessed on.
     * @param expr The {@link Expression} to check.
     * @return {@code true} if the conditions, as described above, apply.
     */
    private boolean isFieldAccessExprOnCopyField(@NotNull String fieldNameOfCopyField, @NotNull String paramName,
                                             @NotNull Expression expr) {
        FieldAccessExpr methodCallScope = as(expr, FieldAccessExpr.class);
        if (methodCallScope == null) {
            return false;
        }

        // checks if correct field of the other object is used
        if (!methodCallScope.getNameAsString().equals(fieldNameOfCopyField)) {
            return false;
        }

        NameExpr objectWithField = as(methodCallScope.getScope(), NameExpr.class);
        if (objectWithField == null) {
            return false;
        }

        return objectWithField.getNameAsString().equals(paramName);
    }
}
