# COMP3290 - Compiler Design
## Assignment 1
### Task
Implement a Lexical Scanner as the first step of a functional compiler for a custom programming language "CD22". The end goal will be to generate valid code for the target stack machine language "SM22"

The Scanner should respond to a `getToken` function which returns the next best fit for a valid token from a given source file.
It should report any lexical errors as undefined tokens and output these errors at the end of stdout.
It should also generate a listing, which at this stage is a duplicate of the program with annotated line numbers, and any associated lexical errors reported at the bottom.

The Scanner should not perform any syntactical checking, and should report successful if the source code is made up of valid tokens, even if the code has syntactical or semantic errors. Included in this project is the start of the second step, the Syntactical Parser, however this was out of scope at this stage and as such only runs a debug routine to output the tokens found by the Scanner.

### Compile
`javac A1.java`

### Run
`java A1 <filename>` where the filename refers to a CD22 source code file. Samples of these can be found in the [data](./data) directory, and included also is the [language specification](./source_language_spec) for further development
