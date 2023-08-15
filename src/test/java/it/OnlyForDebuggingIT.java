package it;

import com.github.javaparser.ast.Node;
import com.github.javaparser.resolution.declarations.ResolvedReferenceTypeDeclaration;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import test.AbstractJsonOutputTest;

import de.jsilbereisen.perfumator.engine.DetectionEngine;
import de.jsilbereisen.perfumator.engine.PerfumeDetectionEngine;
import de.jsilbereisen.perfumator.io.output.OutputConfiguration;
import de.jsilbereisen.perfumator.io.output.OutputFormat;
import de.jsilbereisen.perfumator.model.perfume.Perfume;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThatNoException;

@Disabled("Only for manual debugging purposes.")
class OnlyForDebuggingIT extends AbstractJsonOutputTest {

    private static final Path MANUAL_TEST_OUTPUT_DIR = Path.of("manual_test_results");

    private static final Path M2 = Path.of("C:", "Users", "Jakob", ".m2", "repository");

    private static final Path COMMONS_LANG3 = M2.resolve("org\\apache\\commons\\commons-lang3\\3.13" +
            ".0\\commons-lang3-3.13.0.jar");

    private static final Path COMMONS_IO = M2.resolve("commons-io\\commons-io\\2.13.0\\commons-io-2.13.0.jar");

    private static final Path HAMCREST = M2.resolve("org\\hamcrest\\hamcrest\\2.2\\hamcrest-2.2.jar");

    private static final Path JUPITER_PARAMS = M2.resolve("org\\junit\\jupiter\\junit-jupiter-params\\5.9" +
            ".3\\junit-jupiter-params-5.9.3.jar");

    private static final Path JUNIT_VINTAGE_ENGINE = M2.resolve("org\\junit\\vintage\\junit-vintage-engine\\5.9" +
            ".3\\junit-vintage-engine-5.9.3.jar");

    private static final Path LUBEN_ZSTD_JNI = M2.resolve("com\\github\\luben\\zstd-jni\\1.5.5-5\\" +
            "zstd-jni-1.5.5-5.jar");

    private static final Path BROTLI_DEC = M2.resolve("org\\brotli\\dec\\0.1.2\\dec-0.1.2.jar");

    private static final Path TUKAANI_XZ = M2.resolve("org\\tukaani\\xz\\1.9\\xz-1.9.jar");

    private static final Path OW2_ASM = M2.resolve("org\\ow2\\asm\\asm\\9.5\\asm-9.5.jar");

    private static final Path MOCKITO_CORE = M2.resolve("org\\mockito\\mockito-core\\4.11.0\\mockito-core-4.11.0.jar");

    private static final Path MOCKITO_JUNIT = M2.resolve("org\\mockito\\mockito-junit-jupiter\\4.11.0\\mockito-" +
            "junit-jupiter-4.11.0.jar");

    private static final Path MARSCHALL_MEMORYFILESYSTEM = M2.resolve("com\\github\\marschall\\memoryfilesystem\\2.6" +
            ".1\\memoryfilesystem-2.6.1.jar");

    private static final Path PAX_EXAM_CONTAINER_NATIVE = M2.resolve("org\\ops4j\\pax\\exam\\pax-exam-" +
            "container-native\\4.13.5\\pax-exam-container-native-4.13.5.jar");

    private static final Path PAX_EXAM_JUNIT = M2.resolve("org\\ops4j\\pax\\exam\\pax-exam-junit4" +
            "\\4.13.5\\pax-exam-junit4-4.13.5.jar");

    private static final Path PAX_EXAM_CM = M2.resolve("org\\ops4j\\pax\\exam\\pax-exam-cm\\4.13.5\\" +
            "pax-exam-cm-4.13.5.jar");

    private static final Path PAX_EXAM_LINK_MVN = M2.resolve("org\\ops4j\\pax\\exam\\pax-exam-link-mvn\\4.13.5\\" +
            "pax-exam-link-mvn-4.13.5.jar");

    private static final Path APACHE_FELIX_FRAMEWORK = M2.resolve("org\\apache\\felix\\org.apache.felix.framework\\" +
            "7.0.5\\org.apache.felix.framework-7.0.5.jar");

    private static final Path JAVAX_INJECT = M2.resolve("javax\\inject\\javax.inject\\1\\javax.inject-1.jar");

    private static final Path SLF4J_API = M2.resolve("org\\slf4j\\slf4j-api\\2.0.7\\slf4j-api-2.0.7.jar");

    private static final Path OSGI_CORE = M2.resolve("org\\osgi\\org.osgi.core\\6.0.0\\org.osgi.core-6.0.0.jar");

    private static final Path JUNIT_JUNIT = M2.resolve("junit\\junit\\4.13.2\\junit-4.13.2.jar");

    private static final Path JAVAX_SERVLET = M2.resolve("javax\\servlet\\servlet-api\\2.5\\servlet-api-2.5.jar");

    private static final Path JAVAX_JSP = M2.resolve("javax\\servlet\\jsp-api\\2.0\\jsp-api-2.0.jar");

    private static final Path JDOM = M2.resolve("jdom\\jdom\\1.0\\jdom-1.0.jar");

    private static final Path COMMONS_BEANUTILS = M2.resolve("commons-beanutils\\commons-beanutils\\1.9.4\\" +
            "commons-beanutils-1.9.4.jar");

    private static final Path MOCKRUNNER = M2.resolve("com\\mockrunner\\mockrunner-jdk1.3-j2ee1.3\\" +
            "0.4\\mockrunner-jdk1.3-j2ee1.3-0.4.jar");

    private static final Path JUNIT_JUPITER = M2.resolve("org\\junit\\jupiter\\junit-jupiter\\5.9.3\\junit-" +
            "jupiter-5.9.3.jar");

    private static final Path JUNIT_PIONEER = M2.resolve("org\\junit-pioneer\\junit-pioneer\\1.9.1\\" +
            "junit-pioneer-1.9.1.jar");

    private static final Path COMMONS_TEXT = M2.resolve("org\\apache\\commons\\commons-text\\1.10.0\\" +
            "commons-text-1.10.0.jar");

    private static final Path JMH = M2.resolve("org\\openjdk\\jmh\\jmh-generator-annprocess\\1.37\\" +
            "jmh-generator-annprocess-1.37.jar");

    private static final Path FINDBUGS = M2.resolve("com\\google\\code\\findbugs\\jsr305\\3.0.2\\jsr305-3.0.2.jar");

    private static final Path EASYMOCK = M2.resolve("org\\easymock\\easymock\\5.1.0\\easymock-5.1.0.jar");

    private static final Path COMMONS_STATISTICS_DISTRIBUTION = M2.resolve("org\\apache\\commons\\commons-" +
            "statistics-distribution\\1.0\\commons-statistics-distribution-1.0.jar");

    private static final Path COMMONS_MATH4_LEGACY_EXCEPTION = M2.resolve("org\\apache\\commons\\commons-math4-" +
            "legacy-exception\\4.0-SNAPSHOT\\commons-math4-legacy-exception-4.0-20230811.145206-772.jar");
    private static final Path COMMONS_RNG_CLIENT_API = M2.resolve("org\\apache\\commons\\commons-rng-client-api" +
            "\\1.5\\commons-rng-client-api-1.5.jar");
    private static final Path COMMONS_RNG_SAMPLING = M2.resolve("org\\apache\\commons\\commons-rng-sampling\\1.5\\" +
            "commons-rng-sampling-1.5.jar");
    private static final Path COMMONS_MATH4_CORE = M2.resolve("org\\apache\\commons\\commons-math4-core\\4.0" +
            "-SNAPSHOT\\commons-math4-core-4.0-20230811.145206-620.jar");

    /**
     * Test for investigating a {@link StackOverflowError} which occurred when running the analysis for
     * <b>commons-compress</b>.
     */
    @Test
    void commonsCompressStackOverflow() {
        DetectionEngine<Perfume> engine = PerfumeDetectionEngine.builder()
                .addDependency(COMMONS_LANG3)
                .addDependency(COMMONS_IO)
                .addDependency(HAMCREST)
                .addDependency(JUPITER_PARAMS)
                .addDependency(JUNIT_VINTAGE_ENGINE)
                .addDependency(LUBEN_ZSTD_JNI)
                .addDependency(BROTLI_DEC)
                .addDependency(TUKAANI_XZ)
                .addDependency(MOCKITO_CORE)
                .addDependency(MOCKITO_JUNIT)
                // Leads to a stack overflow, looks like the resolution runs into an infinite recursion/loop
                //.addDependency(OW2_ASM)
                .addDependency(MARSCHALL_MEMORYFILESYSTEM)
                .addDependency(PAX_EXAM_CM)
                .addDependency(PAX_EXAM_JUNIT)
                .addDependency(PAX_EXAM_LINK_MVN)
                .addDependency(PAX_EXAM_CONTAINER_NATIVE)
                .addDependency(APACHE_FELIX_FRAMEWORK)
                .addDependency(JAVAX_INJECT)
                .addDependency(SLF4J_API)
                .addDependency(OSGI_CORE)
                .build();

        assertThatNoException().isThrownBy(() ->
                engine.detectAndSerialize(Path.of("..", "evaluation-repos", "commons-compress"),
                        OutputConfiguration.from(OUTPUT_TEST_RESULTS_RESOURCES_ROOT_DIR), OutputFormat.JSON));
    }

    /**
     * Test for investigating a {@link StackOverflowError} which occurred when running the analysis for
     * <b>commons-jxpath</b>.
     */
    @Test
    void commonsJxpathStackOverflow() {
        DetectionEngine<Perfume> engine = PerfumeDetectionEngine.builder()
                // JUnit causes StackOverflow here!
                // looks like the resolution runs into an infinite recursion/loop
                //.addDependency(JUNIT_JUNIT)
                .addDependency(JAVAX_SERVLET)
                .addDependency(JAVAX_JSP)
                .addDependency(JDOM)
                .addDependency(COMMONS_BEANUTILS)
                .addDependency(MOCKRUNNER)
                .build();

        assertThatNoException().isThrownBy(() ->
                engine.detectAndSerialize(Path.of("..", "evaluation-repos", "commons-jxpath"),
                        OutputConfiguration.from(OUTPUT_TEST_RESULTS_RESOURCES_ROOT_DIR), OutputFormat.JSON));
    }

    /**
     * Test for investigating a {@link StackOverflowError} which occurred when running the analysis for
     * <b>commons-lang</b>.
     * Investigation showed, that the infinite loop/recursion that caused the {@link StackOverflowError}, was initiated
     * in a file that was generated by the maven build output. These files we do want to ignore anyway, so this
     * resulted in the addition of the {@link de.jsilbereisen.perfumator.util.PathUtil#isInBuildOutputDir} method,
     * to exclude files that are in a build output directory.
     */
    @Test
    void commonsLangStackOverflow() {
        DetectionEngine<Perfume> engine = PerfumeDetectionEngine.builder()
                .addDependency(JUNIT_JUPITER)
                .addDependency(JUNIT_PIONEER)
                .addDependency(HAMCREST)
                .addDependency(EASYMOCK)
                .addDependency(COMMONS_TEXT)
                .addDependency(JMH)
                .addDependency(FINDBUGS)
                .build();

        assertThatNoException().isThrownBy(() ->
                engine.detectAndSerialize(Path.of("..", "evaluation-repos", "commons-lang"),
                        OutputConfiguration.from(OUTPUT_TEST_RESULTS_RESOURCES_ROOT_DIR), OutputFormat.JSON));
    }

    /**
     * <p>
     * Test for investigating a {@link StackOverflowError} which occurred when running the analysis for
     * <b>commons-math</b>. The analysed class where the error rises is <i>AbstractIntegerDistribution</i>, the
     * the Detector is {@link de.jsilbereisen.perfumator.engine.detector.perfume.IteratorNextContractDetector}.
     * </p>
     * <p>
     * Investigation showed, that the infinite loop/recursion that caused the {@link StackOverflowError} stems
     * from a seeming incompatibility of {@link com.github.javaparser.symbolsolver.javassistmodel.JavassistInterfaceDeclaration}
     * and {@link com.github.javaparser.symbolsolver.reflectionmodel.ReflectionInterfaceDeclaration}, as
     * {@link com.github.javaparser.symbolsolver.reflectionmodel.ReflectionInterfaceDeclaration#isAssignableBy} calls
     * {@link ResolvedReferenceTypeDeclaration#canBeAssignedTo} (which isn't overridden by the {@code JavassistInterfaceDeclaration}),
     * so it uses the default implementation, which again calls {@link com.github.javaparser.symbolsolver.reflectionmodel.ReflectionInterfaceDeclaration#isAssignableBy}
     * and so on...
     * </p>
     * <p>
     * For current status, see <a href="https://github.com/jsilbereisen/perfumator-java/issues/66">the GitHub Issue</a>.
     * </p>
     */
    @Test
    void commonsMathStackOverflow() {
        // All other dependencies are ignored in this test, as the issue only rose in the one class
        // which would not have been perfumed either way.

        DetectionEngine<Perfume> engine = PerfumeDetectionEngine.builder()
                //.addDependency(COMMONS_STATISTICS_DISTRIBUTION)
                .addDependency(COMMONS_MATH4_LEGACY_EXCEPTION)
                .addDependency(COMMONS_RNG_CLIENT_API)
                //.addDependency(COMMONS_RNG_SAMPLING)
                .addDependency(COMMONS_MATH4_CORE)
                .build();

        assertThatNoException().isThrownBy(() ->
                engine.detectAndSerialize(Path.of("..", "evaluation-repos", "commons-math"),
                        OutputConfiguration.from(OUTPUT_TEST_RESULTS_RESOURCES_ROOT_DIR), OutputFormat.JSON));
    }

    /**
     * Test for investigating an {@link IllegalArgumentException} which occurred when running the analysis for
     * <b>jackson-databind</b>.
     * Even though the causing file was a test-resource (which was not in src/test/resources, but in another specific
     * resources directory), it seemed like a more general problem.
     * After debugging, it seems like {@link com.github.javaparser.symbolsolver.JavaSymbolSolver#toTypeDeclaration} just
     * ignores {@link com.github.javaparser.ast.body.RecordDeclaration}s at the moment.
     * So, we ignore records for the {@link de.jsilbereisen.perfumator.engine.detector.perfume.CopyConstructorDetector}.
     */
    @Test
    void jacksonDatabindCopyConstructorDetectorBug() {
        Path causingFile = Path.of("..","evaluation-repos", "jackson-databind" , "src",
                "test-jdk14", "java", "com", "fasterxml", "jackson", "databind", "records",
                "RecordUpdate3079Test.java");

        DetectionEngine<Perfume> engine = PerfumeDetectionEngine.builder().build();

        assertThatNoException().isThrownBy(() ->
                engine.detectAndSerialize(causingFile, OutputConfiguration.from(OUTPUT_TEST_RESULTS_RESOURCES_ROOT_DIR),
                        OutputFormat.JSON));
    }
}
