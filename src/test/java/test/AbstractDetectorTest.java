package test;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;
import de.jsilbereisen.perfumator.util.PathUtil;

import java.io.IOException;
import java.nio.file.Path;

public class AbstractDetectorTest {

    protected static final JavaParser parser = new JavaParser();

    protected static CompilationUnit parseAstForFile(Path path) {
        assert PathUtil.isJavaSourceFile(path) : "Path does not point to an existing single Java Source file.";

        ParseResult<CompilationUnit> result;

        try {
            result = parser.parse(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        assert result != null && result.isSuccessful() && result.getResult().isPresent() :
                "Unable to parse/access the Java Source file at " + path;

        return result.getResult().get();
    }
}
