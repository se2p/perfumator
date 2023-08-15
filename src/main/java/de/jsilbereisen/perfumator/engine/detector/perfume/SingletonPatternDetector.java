package de.jsilbereisen.perfumator.engine.detector.perfume;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumConstantDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.declarations.ResolvedClassDeclaration;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import com.github.javaparser.resolution.types.ResolvedReferenceType;
import com.github.javaparser.symbolsolver.javaparsermodel.JavaParserFacade;
import com.github.javaparser.utils.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import de.jsilbereisen.perfumator.engine.detector.Detector;
import de.jsilbereisen.perfumator.engine.visitor.TypeVisitor;
import de.jsilbereisen.perfumator.model.DetectedInstance;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.jsilbereisen.perfumator.util.NodeUtil.*;

/**
 * <p>
 * {@link Detector} for the "Singleton pattern" {@link Perfume}.
 * This Perfume captures the different three ways to implement a singleton that Joshua Bloch describes in his
 * book "Effective Java":
 * <ul>
 *     <li>
 *         A regular class is used, the field for the singleton instance is {@code public static final}, all
 *         constructors are private. So, the field is clearly visible in the API and directly accessible.
 *     </li>
 *     <li>
 *         A regular class is used, but the field for the singleton instance is {@code private static}, not
 *         necessarily {@code final} though, to allow for lazy initialization. In this case, a {@code public static}
 *         factory method is required in order to obtain the singleton instance, but it can for example initialize
 *         the instance lazily. Also, allows for example to change the singleton field to a collection/array of
 *         multiple registered instances, e.g. in multi-threaded environments, without changing the public API.
 *         All constructors must be private.
 *     </li>
 *     <li>
 *         An enum type is used, with only one single constant, which is the singleton instance. This is Joshua
 *         Bloch's preferred way, as one does not have to worry about (De-)Serialization bugs or Reflection attacks,
 *         which could cause multiple instances to exist.
 *     </li>
 * </ul>
 * </p>
 * <p>
 * Additionally, this detector checks whether the recommended steps for avoiding (De-)Serialization bugs are taken,
 * which are described by Joshua Bloch. This is only necessary if the singleton's type is a standard class, not an
 * {@link Enum}. The following steps are verified:
 * <ul>
 *     <li>
 *         All non-static fields have to be {@code transient}.
 *     </li>
 *     <li>
 *         An implementation for the {@code Object readResolve()} method (mentioned in the documentation of
 *         {@link Serializable}) has to be provided, with any access modified, and which returns the singleton instance.
 *         So, it should look something like this, the returned field name can vary:
 *         <pre>
 *         private Object readResolve() {
 *             return INSTANCE;
 *         }
 *         </pre>
 *     </li>
 * </ul>
 * </p>
 */
public class SingletonPatternDetector implements Detector<Perfume> {

    public static final Pair<String, String> SERIALIZABLE_CLASS = new Pair<>("Serializable", "java.io.Serializable");

    public static final String REQUIRED_METHOD_DESERIALIZATION = "readResolve";

    private Perfume perfume;

    private JavaParserFacade analysisContext;

    @Override
    public @NotNull List<DetectedInstance<Perfume>> detect(@NotNull CompilationUnit astRoot) {
        TypeVisitor typeVisitor = new TypeVisitor();
        astRoot.accept(typeVisitor, null);
        List<ClassOrInterfaceDeclaration> classes = typeVisitor.getClassOrInterfaceDeclarations();
        List<EnumDeclaration> enums = typeVisitor.getEnumDeclarations();

        if (classes.isEmpty() && enums.isEmpty()) {
            return Collections.emptyList();
        }

        List<DetectedInstance<Perfume>> detections = new ArrayList<>();

        for (ClassOrInterfaceDeclaration clazz : classes) {
            if (isPerfumedSingleton(clazz, astRoot)) {
                detections.add(DetectedInstance.from(clazz, perfume, clazz));
            }
        }

        for (EnumDeclaration enumDecl : enums) {
            if (isPerfumedSingleton(enumDecl)) {
                detections.add(DetectedInstance.from(enumDecl, perfume, enumDecl));
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
     * Checks if the conditions that are described in the classes' documentation are met for the given
     * {@link ClassOrInterfaceDeclaration}. This means specifically that serialization is also checked, if it is
     * determined to be a {@link Serializable}, preferably through symbol resolution, but if not possible, through
     * simple checking of the implemented types.
     *
     * @param clazz The class to check.
     * @param ast The ast that contains the given class declaration.
     * @return {@code true} if the class is a perfumed singleton.
     * @see SingletonPatternDetector Class documentation
     */
    private boolean isPerfumedSingleton(@NotNull ClassOrInterfaceDeclaration clazz, @NotNull CompilationUnit ast) {
        if (clazz.isInterface() || hasNonPrivateConstructor(clazz)) {
            return false;
        }

        // Find the field that holds the singleton instance
        Optional<FieldDeclaration> singletonField = findSingletonField(clazz);
        if (singletonField.isEmpty()) {
            return false;
        }

        // Get the declared variable. If the field is private, the class needs a static factory method.
        VariableDeclarator singletonFieldVar = singletonField.get().getVariable(0);
        if (singletonField.get().isPrivate()
                && !hasSingletonFactoryMethod(clazz, singletonFieldVar)) {
            return false;
        }

        // Check whether the class is assignable to Serializable. If not, we can already say its perfumed.
        if (!implementsSerializable(clazz, ast)) {
            return true;
        }

        // For a Serializable, we finally need to check the required steps (transient fields + readResolve)
        return meetsSerializableRequirements(clazz, singletonFieldVar);
    }

    @NotNull
    private Optional<FieldDeclaration> findSingletonField(@NotNull ClassOrInterfaceDeclaration clazz) {
        String className = clazz.getNameAsString();

        // Find all static fields
        List<FieldDeclaration> fields = clazz.getFields().stream()
                .filter(FieldDeclaration::isStatic).collect(Collectors.toList());

        // Find all candidate fields for the singleton-instance-field
        List<FieldDeclaration> foundInstanceFields = new ArrayList<>();
        for (FieldDeclaration field : fields) {
            if (isSingletonField(field, className)) {
                foundInstanceFields.add(field);
            }
        }

        // As it should be a singleton, we want to have only a single field that holds an instance of the class
        if (foundInstanceFields.size() != 1) {
            return Optional.empty();
        } else {
            return Optional.of(foundInstanceFields.get(0));
        }
    }

    private boolean isSingletonField(@NotNull FieldDeclaration field, @NotNull String className) {
        List<VariableDeclarator> variables = field.getVariables();
        if (variables.size() != 1) {
            return false;
        }

        VariableDeclarator variable = variables.get(0);
        Type type = variable.getType();
        if (!type.isClassOrInterfaceType()
                || !type.asClassOrInterfaceType().getNameAsString().equals(className)) {
            return false;
        }

        if ((field.isPublic() || field.isPrivate()) && field.isFinal()) {
            // If the field is final, it has to be instantiated
            Optional<Expression> initializer = variable.getInitializer();
            if (initializer.isEmpty()) {
                return false;
            }

            ObjectCreationExpr constructorCall = as(initializer.get(), ObjectCreationExpr.class);
            if (constructorCall == null) {
                return false;
            }

            return constructorCall.getType().getNameAsString().equals(className);
        }

        // If the field is private & non-final, allow for lazy initialization
        return field.isPrivate() && !field.isFinal();
    }

    private boolean hasSingletonFactoryMethod(@NotNull ClassOrInterfaceDeclaration clazz,
                                           @NotNull VariableDeclarator singletonFieldVar) {
        List<MethodDeclaration> potentialInstanceFactories = clazz.getMethods().stream()
                .filter(method -> method.isPublic() && method.isStatic()
                        && method.getParameters().isEmpty() && returnsClass(method, clazz))
                .collect(Collectors.toList());

        if (potentialInstanceFactories.size() != 1) {
            return false;
        }

        MethodDeclaration singletonFactoryMethod = potentialInstanceFactories.get(0);

        return returnsSingletonInstance(singletonFactoryMethod, singletonFieldVar);
    }

    private boolean implementsSerializable(@NotNull ClassOrInterfaceDeclaration clazz, @NotNull CompilationUnit ast) {
        // First try resolution - the "safest" way to tell if it is actually a "java.io.Serializable" (through any
        // inheritance)
        if (analysisContext != null) {
            Optional<ResolvedReferenceTypeDeclaration> resolved = resolveSafely(clazz, this, clazz.getNameAsString());

            if (resolved.isPresent()) {
                if (!resolved.get().isClass()) {
                    return false;
                }

                ResolvedClassDeclaration resolvedClass = resolved.get().asClass();
                Optional<List<ResolvedReferenceType>> implementedInterfaces = safeResolutionAction(resolvedClass::getAllInterfaces);
                if (implementedInterfaces.isEmpty()) {
                    return false;
                }

                return implementedInterfaces.get().stream().anyMatch(interfaze -> interfaze.getQualifiedName().equals(SERIALIZABLE_CLASS.b));
            }
        }

        // Check if any "Serializable" is explicitly implemented
        if (clazz.getImplementedTypes().stream().noneMatch(type -> type.getNameAsString().equals(SERIALIZABLE_CLASS.a))) {
            return false;
        }

        // Return true when either "java.io.*" or "java.io.Serializable" is imported
        return ast.getImports().stream().anyMatch(importDeclaration -> {
            if (importDeclaration.isStatic()) {
                return false;
            }

            if (importDeclaration.isAsterisk() && importDeclaration.getNameAsString().equals("java.io")) {
                return true;

            } else {
                return !importDeclaration.isAsterisk() && importDeclaration.getNameAsString().equals(SERIALIZABLE_CLASS.b);
            }
        });
    }

    private boolean meetsSerializableRequirements(@NotNull ClassOrInterfaceDeclaration clazz,
                                                  @NotNull VariableDeclarator singletonFieldVar) {
        // If there is any non-static field that is not transient -> WRONG
        if (clazz.getFields().stream().anyMatch(field -> !field.isStatic() && !field.isTransient())) {
            return false;
        }

        List<MethodDeclaration> readResolveCandidates = clazz.getMethods().stream()
                .filter(method -> method.getNameAsString().equals(REQUIRED_METHOD_DESERIALIZATION)
                        && method.getType().asString().equals("Object") && method.getParameters().isEmpty())
                .collect(Collectors.toList());

        if (readResolveCandidates.size() != 1) {
            return false; // Something is wrong then...
        }

        return returnsSingletonInstance(readResolveCandidates.get(0), singletonFieldVar);
    }

    private static boolean returnsSingletonInstance(@NotNull MethodDeclaration method,
                                                    @NotNull VariableDeclarator singletonFieldVar) {
        Optional<BlockStmt> body = method.getBody();
        if (body.isEmpty()) {
            return false;
        }

        Optional<Statement> last = body.get().getStatements().getLast();
        if (last.isEmpty()) {
            return false;
        }

        ReturnStmt returnStmt = as(last.get(), ReturnStmt.class);
        if (returnStmt == null) {
            return false;
        }

        Optional<Expression> returnExpr = returnStmt.getExpression();
        if (returnExpr.isEmpty()) {
            return false;
        }

        NameExpr returnedField = as(returnExpr.get(), NameExpr.class);
        if (returnedField == null) {
            return false;
        }

        return returnedField.getNameAsString().equals(singletonFieldVar.getNameAsString());
    }

    private boolean isPerfumedSingleton(@NotNull EnumDeclaration enumDecl) {
        List<EnumConstantDeclaration> constants = enumDecl.getEntries();
        if (constants.size() != 1) {
            return false;
        }

        // Require at least one non-static field. Why would we need an instance in the first place then?
        if (enumDecl.getFields().stream().allMatch(FieldDeclaration::isStatic)) {
            return false;
        }

        // Require at least one public, none-static method.
        return enumDecl.getMethods().stream().anyMatch(method -> method.isPublic() && !method.isStatic());
    }
}
