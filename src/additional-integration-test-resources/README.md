### Additional Test Resources

This directory is required for properly testing
the class **PerfumeDetectionEngine**, which is supposed to ignore
Java source files that are resources (in the directory _src/main/resources_
or _src/test/resources_) in its analysis.

As a consequence, it would ignore all files under the _src/test/resources_ directory
of this project as well, so one can not just put a sample project to analyse
in integration tests there.

To solve this, this directory is also used as test-resources for
integration tests.