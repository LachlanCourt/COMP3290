# COMP3290 - Compiler Design
## Assignment 2
### Task

Implement a functional compiler for a custom programming language "CD22". The end goal will be to generate valid code for the target stack machine language "SM22"

Task 1:

Implement a Lexical Scanner as the first step of a functional compiler.

The Scanner should respond to a `getToken` function which returns the next best fit for a valid token from a given source file.
It should report any lexical errors as undefined tokens and output these errors at the end of stdout.
It should also generate a listing, which at this stage is a duplicate of the program with annotated line numbers, and any associated lexical errors reported at the bottom.

The Scanner should not perform any syntactical checking, and should report successful if the source code is made up of valid tokens, even if the code has syntactical or semantic errors. Included in this project is the start of the second step, the Syntactical Parser, however this was out of scope at this stage and as such only runs a debug routine to output the tokens found by the Scanner.

Task 2:

Implement a Top-Down Recursive Descent Parser as the second step of a functional compiler.

The Parser should receive a list of tokens from the scanner and using built in recursive grammar rules, attempt to interpret and apply context to these tokens to create a syntax tree. In addition to the preorder traversal used to output the tree at the end, the parser should also create a symbol table which will be used for semantic checking and code generation. Semantic checking was out of scope for this part of the project, however some checks for defined fields on structs have been implemented.

The Parser should be able to implement some form of error recovery in order to match as many valid errors as possible rather than just failing on the first one. The two methods of error recovery used here are Panic Mode where the parser attempts to find a token it can use to resynchronise, and Error Productions where colons, semicolons, commas, and epsilon are relatively interchangeable and won't break parsing, however an error will be present and execution will not continue onto code generation.

The program should also generate a program listing and a log file which copies the stdout.

Part of this task included converting the given grammar rules into an LL(1) style grammar, (Read left to right, Left recursive, 1 lookahead token at a time). This conversion and associated calculations is documented in [this spreadsheet](source_language_spec/Grammar_Conversion.xlsx)

Task 3:

Implement Semantic Analysis into the Parser, and finish with Code Generation in SM22 stack machine operation codes

Coming soon...

### Compile
`javac A2.java`

### Run
`[DEBUG=<boolean>] java A2 <filename>`

Where the filename refers to a CD22 source code file. Samples of these can be found in the [data](./data) directory, and included also is the [language specification](./source_language_spec) for further development.

The optional `DEBUG` environment variable will output a number of helpful data points which are usually abstracted from the user, including a token list, the symbol table, and the syntax tree in XML format which can be pasted into an interpreter for visual analysis
