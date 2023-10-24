# The Perfumator

Repository for the "Perfumator" project for Java. A small command line application to detect certain
Code Perfumes in Java source code.

This project is part of the **Perfuming Java for learners: an adoption of Code Perfumes for Java** bachelor thesis by Jakob Silbereisen
at the Chair of Software Engineering II (University of Passau).

## How to build the Perfumator

Build requirements:

- JDK 17
- Apache Maven build tool (last built with version 3.9.1)

The application is packaged as an executable JAR with dependencies. For packaging, navigate to the project folder in a
terminal and execute:
`mvn clean package`.

This creates the **target/** directory, where you can find the executable JAR with dependencies, named like
_perfumator-java-X.Y.Z-jar-with-dependencies.jar_, where _X.Y.Z_ is the version.

To execute all tests (including integration tests), run `mvn verify`.

## Executing the Perfumator

To start the tool from the command line, execute

`java -jar ARTIFACT -i INPUT_PATH -o OUTPUT_PATH [additional options ...]`

in the terminal,
where _ARTIFACT_ is the path to the executable, packaged with dependencies, Perfumator-JAR.
For an overview over all available command line arguments/options, use the `-h` or `--help` option (might be more up-to-date than this description).

The following arguments **must** be provided for analysis execution (not if `-h` or `--help` is given):

- `-i INPUT_PATH` or `--input INPUT_PATH`: Path to the directory/Java source file, which should be taken as the root directory for analysis (or be directly analysed if it is a single file). Will be recursively scanned for Java Source files. Notice that, if the project directory to analyse follows the typical Maven/Gradle directory structure, all Java source files that are placed in (a subdirectory of) **src/main/resources** or **src/test/resources** will be ignored in the analysis.
- `-o OUTPUT_PATH` or `--output-dir OUTPUT_PATH`: Path to an **empty** directory, where the analysis output should be stored. The standard analysis output (here: JSON format) consists of a file called _summary.json_, which presents a concise overview over all Perfumes that were analysed and the amount of detections, and a file called **detections.json**, which lists ALL instances of detections of any Perfume. There might be multiple **detection** files produced, if the amount of detections is higher than the batch size for one file. Then, the detection-files will be suffixed with ongoing numbers, starting with 1.

Additional, optional arguments:

- `-h` or `--help`: Prints the help-overview over all available commands, then terminates the application, no matter which other arguments are given.
- `-f FORMAT` or `--format`: Sets the format for the output. **Currently, only JSON is supported.**
- `-l LANGUAGE_TAG` or `--language`: Sets the _preferred_ language for the tool. This means, if available, all log messages/error messages/output resources/perfumes have the given language. If any resource is not available in that language, the English version is used as a fallback. Currently, the only supported languages are English and German, including Perfume and CLI resources.
- `-d "path;path2;..."` or `--dependencies`: A list of dependencies for the analysis. A dependency can be JAR or simply a source root directory of a project. If you want to only analyse a single file, you should at least provide the project's source root (if the file is part of a project) as a dependency, for some context. If required external dependencies are missing, some Perfumes might not be detected under certain circumstances/at all.
- `-b BATCH_SIZE` or `--batch-size`: Sets the batch size (size for the listings of detections) for the serialized output, default: 10000. If you encounter an OOM error when running the analysis, lowering the batch size might help.

## API

### Main engine

The `de.jsilbereisen.perfumator.engine.PerfumeDetectionEngine` class is responsible for running the analysis.
An instance can be created via its inner class `Builder`, which offers the default configuration with a zero-arguments constructor.

When using the default configuration, all Perfumes definitions are
loaded from the default location (see the later sections for more information on the default locations) with the default resource language (English).
Also, a standard **JavaParser** is initialized, with its _Java language level set to 17_ (highest supported language level).

By implementing the `DetectionEngine` interface, the engine offers two main methods for the analysis: `detect` and `detectAndSerialize`.
`detect` runs the analysis and returns a list of detections, while `detectAndSerialize` returns `void` but serializes the analysis results.
For huge projects, `detect` might cause an `OutOfMemoryError` (OOM), because it holds all detections in memory to return them,
while `detectAndSerialize` allows specification of a batch-size, after which the current list of detections in memory is flushed
to the output directory, to avoid OOMs.

**Note** that if the directory to analyse follows a Maven/Gradle project structure (has sources in `src/main/java`,
resources in `src/main/resources`, and equivalent directories for the unit tests), then all Java source files that are
under the usual resources-directories are **ignored** by the engine for the analysis.

### Useful utility

The `de.jsilbereisen.perfumator.util.JsonDeserializationUtil` class offers utility methods for deserializing analysis results that
are produced by the application (perfume listings, summary).

If you want to write unit tests, e.g. for own Perfumes, the `src/test/java/test` directory contains for example abstract classes
that you can inherit from for common kinds of tests, as well as other test-related utility. 

## Adding a new Code Perfume ...

### ... with the default configurations

The Perfumator auto-detects Code Perfume definitions (JSON) in the `src/main/resources/de/jsilbereisen/perfumator/data/perfumes`
directory. It will look to load a Perfume instance with this information into memory, and also instantiate and link the associated Detector class.
The loaded Perfumes and Detectors and their links are kept in a `PerfumeRegistry` during runtime.

If you wish to add a new Code Perfume to the application, first start off by adding the JSON definition of your new Perfume
to this directory. It should contain the following static information (JSON properties):

- `name`, `description`, `additionalInformation`: Static information in English (default language).
- `detectorClassSimpleName`: Simple name (= class name, not fully qualified) of the `Detector` that is associated with this Perfume and that is responsible to detect the Perfume's code structure in the AST. With the default Perfumator configuration, the detectors for all Perfumes reside in the `src/main/java/de/jsilbereisen/perfumator/engine/detector/perfume` directory. The detector class **must** have a zero-arguments constructor (instantiated via Reflection) and **should** override `equals` and `hashCode` (using the `@EqualsAndHashCode` annotation from Lombok is recommended).
- `i18nBaseBundleName`: Base name of the _ResourceBundle_ for internationalizing the `name`, `description`, `sources` and `additionalInformation` properties of this Perfume. See the later section _Adding a resource bundle ..._ for more information on how to add the resource bundle.
- `sources`: List of sources that inspired this Perfume or where one can find additional information about it.
- `relatedPattern`: Enum constant of `de.jsilbereisen.perfumator.model.perfume.RelatedPattern`. This way, one can signal for example whether this Perfume solves a smell / bug pattern or depicts a Design pattern.

### ... with custom configurations

As the `PerfumeDetectionEngine.Builder` allows one to configure the `Registry<Perfume>` which is responsible for loading the Perfume definitions (plus invoke their internationalization if needed) and their detectors to use by the engine,
one can either create a `PerfumeRegistry` and supply custom values to the offered configuration constructor, or one can create a completely new implementation of the interface and supply that one.

This allows to store and load Perfume definitions and their detectors in custom locations. Also, the location where the internationalization resources can be found can be configured, or internationalization could be entirely dropped via a custom implementation, if not needed.

The addition of new Perfumes should then be similar to the previous section, just with the custom locations of the Perfumes, detectors and resources.


## Adding a resource bundle ...

### ... for a Code Perfume

To add a new resource bundle for a Code Perfume, you need to take the following
steps:

1. Create a base resource in the `src/main/resources/i18n/perfumes` folder.
2. Check that the name of the base resource **EXACTLY** matches (without the `.properties` file extension of course) the `i18nBaseBundleName` property in the associated Perfume definition JSON.
3. The base resource bundle can be left empty, if the associated Perfume JSON definition already contains the English field values (English is the default application language).
4. Now you can add support for other languages for your Perfume by adding other resources in the same folder and the same base name, extended with an _underscore_ ( "_" ) and the _language tag_, for example for the base resource/perfume "foo" and the language Spanish, you would add `foo_es.properties`.
5. Make sure that if you add a resource for a language, that the language is generally supported by the application, meaning the tag is defined in the `de.jsilbereisen.perfumator.io.LanguageTag` class. If not, feel free to simply add a new constant to the class for your desired language.

Please check out existing resources for other Perfumes for guidance on which properties you can internationalize.
Pay attention specifically to the `sources` property, as it is a _list_ of strings in the Perfume JSON definition.
This is handled in the resource bundles by adding multiple `source#X` mappings, where _X_ is an ongoing number (starting from 1), so that one can
specify the internationalized list content in the correct order.

### ... for the application itself

Simply add the resource for the desired language to the `resources/i18n/application` directory, either for the
_application_ base bundle (e.g. for Log messages or exception / error messages) or for the _commandline_ base bundle
(command line interface).

## Acknowledgements

This endeavor would not have been possible without the chair of Software Engineering II, especially my supervisor Prof.
Dr. Gordon Fraser and my advisor Philipp Straubinger, who continuously supported the work throughout the entire process,
from the topic selection until the final submission. Thank you for your patience and the invaluable feedback.

## License

This project is licensed under the Apache License v2.
