# Perfumator for Java

Repository for the "Perfumator" project for Java. A simple command line application to detect certain
Code Perfumes in Java source code.

The application is packaged as an executable jar with dependencies. For packaging, navigate to the project folder in a
terminal and execute:
`mvn clean package`.

This creates the **target/** directory, where you can find the fat, executable jar with dependencies, named like
_perfumator-java-X.Y.Z-jar-with-dependencies.jar_, where _X.Y.Z_ is the version.

## Command Line interface

To start the tool from the command line, execute

`java -jar ARTIFACT -i INPUT_PATH -o OUTPUT_PATH [optional args ...]`,

where _ARTIFACT_ is the path to the executable, packaged with dependencies, Perfumator-JAR.
For an overview over all available command line arguments/options, use the `-h` or `--help` option (might be more up-to-date than this description).

The following arguments **must** be provided for analysis execution (not if `-h` or `--help` is given):

- `-i INPUT_PATH` or `--input INPUT_PATH`: Path to the directory/Java source file, which should be taken as the root directory for analysis (or be directly analysed if it is a single file). Will be recursively scanned for Java Source files. Notice that, if the project directory to analyse follows the typical Maven/Gradle directory structure, all Java source files that are placed in (a subdirectory of) **src/main/resources** or **src/test/resources** will be ignored in the analysis.
- `-o OUTPUT_PATH` or `--output-dir OUTPUT_PATH`: Path to an **empty** directory, where the analysis output should be stored. The standard analysis output (here: JSON format) consists of a file called _summary.json_, which presents a concise overview over all Perfumes that were analysed and the amount of detections, and a file called **detections.json**, which lists ALL instances of detections of any Perfume. There might be multiple **detection** files produced, if the amount of detections is higher than the batch size for one file. Then, the detection-files will be suffixed with ongoing numbers, starting with 1.

Additional, optional arguments:

- `-h` or `--help`: Prints the help-overview over all available commands, then terminates the application, no matter which other arguments are given.
- `-f FORMAT` or `--format FORMAT`: Sets the format for the output. **Currently, only JSON is supported.**
- `-l LANGUAGE_TAG` or `--language`: Sets the _preferred_ language for the tool. This means, if available, all log messages/error messages/output resources/perfumes have the given language. If any resource is not available in that language, the English version is used as a fallback. Currently, the only supported languages are English and German, including Perfume and CLI resources.

## API

If you want to use the Perfumator as a dependency, here are some starting points with the most important classes and methods.

### PerfumeDetectionEngine

This class is responsible for running the analysis. When using the default constructor, all Perfumes definitions are
loaded from the default location with the default resource language (English).
Also, a standard __JavaParser__ is initialized, with its **Java language level set to 17** (highest supported language level).
There are several other constructors that can be used to run the analysis with a different registry, other resources 
a differently configured parser.

By implementing the `DetectionEngine` interface, the engine offers two main methods for the analysis: `detect` and `detectAndSerialize`.

`detect` runs the analysis and returns a list of detections, while `detectAndSerialize` returns `void` and serializes the analysis results.
For huge projects, `detect` might cause an `OutOfMemoryError` (OOM), because it holds all detections in memory to return them,
while `detectAndSerialize` allows specification of a batch-size, after which the current list of detections in memory is flushed
to the output directory, to avoid OOMs.

Both methods take a path to the source file/directory to analyse and a Varargs list of Paths of dependencies as their parameters,
with `detectAndSerialize` additionally requiring arguments that specify the output format/configuration.

**Note** that if the directory to analyse follows a Maven/Gradle project structure (has sources in `src/main/java`,
resources in `src/main/resources`, and equivalent directories for the unit tests), then all Java source files that are
under the usual resources-directories are **ignored**.
