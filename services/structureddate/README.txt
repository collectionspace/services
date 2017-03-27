This project contains classes that model CollectionSpace structured dates, and a parser to create structured dates from a display date string.

Maven 3 is required to build. ANTLR 4 is used for parser generation.

The ANTLR 4 grammar is located in src/main/antlr4. During the build, source code is generated into target/generated-sources/antlr4. Be sure to add this as a source folder in your IDE, so that it will be able to find the generated classes.

Parser tests are located in src/test/resources/test-dates.yaml. The comments in that file describe how to add tests.