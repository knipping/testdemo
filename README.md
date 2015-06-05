Initial commit. Small programm to handle task described in: https://github.com/goeuro/dev-test

Program saves selected entries of JSON answer from API URL in csv-format. String entries are quoted. Double quotes in string entries are repeated (following RFC 4180).

External library needed: json-simple (Apache License 2.0), to be found at: https://code.google.com/p/json-simple/

GoEuroTest.java -- Programm source code. Needs above mentioned json-simple lib for compilation.
manifest.txt -- Manifest file used to create Jar.
GoEuroTest.jar -- Executable Jar file. Includes above mentioned json-simple lib.
