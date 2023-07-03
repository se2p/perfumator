package de.jsilbereisen.perfumator.engine.visitor;

import com.github.javaparser.ast.body.AnnotationDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class TypeVisitor extends VoidVisitorAdapter<Object> {

    @Getter(onMethod = @__({@NotNull}))
    private final List<ClassOrInterfaceDeclaration> classOrInterfaceDeclarations = new ArrayList<>();

    @Getter(onMethod = @__({@NotNull}))
    private final List<EnumDeclaration> enumDeclarations = new ArrayList<>();

    @Getter(onMethod = @__({@NotNull}))
    private final List<RecordDeclaration> recordDeclarations = new ArrayList<>();

    @Getter(onMethod = @__({@NotNull}))
    private final List<AnnotationDeclaration> annotationDeclarations = new ArrayList<>();

    @Override
    public void visit(ClassOrInterfaceDeclaration type, Object o) {
        classOrInterfaceDeclarations.add(type);

        super.visit(type, o);
    }

    @Override
    public void visit(EnumDeclaration type, Object o) {
        enumDeclarations.add(type);

        super.visit(type, o);
    }

    @Override
    public void visit(RecordDeclaration type, Object o) {
        recordDeclarations.add(type);

        super.visit(type, o);
    }

    @Override
    public void visit(AnnotationDeclaration type, Object o) {
        annotationDeclarations.add(type);

        super.visit(type, o);
    }

    @NotNull
    public List<TypeDeclaration<?>> getAllTypeDeclarations() {
        List<TypeDeclaration<?>> allTypes = new ArrayList<>();

        allTypes.addAll(classOrInterfaceDeclarations);
        allTypes.addAll(enumDeclarations);
        allTypes.addAll(recordDeclarations);
        allTypes.addAll(annotationDeclarations);

        return allTypes;
    }
}
