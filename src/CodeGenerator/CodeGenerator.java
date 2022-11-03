/*******************************************************************************
 ****    COMP3290 Assignment 2
 ****    c3308061
 ****    Lachlan Court
 ****    10/09/2022
 ****    This class is a placeholder for the code generator, which currently
 ****    runs the Parser's debug routine
 *******************************************************************************/
package CodeGenerator;

import Common.*;
import Parser.Parser;
import Parser.TreeNode;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CodeGenerator {
    private final Parser parser;
    private final OutputController outputController;

    private TreeNode syntaxTree;
    private SymbolTable symbolTable;
    private final Utils utils;
    private String constantsCodeBlock;
    private final HashMap<Integer, Integer> literalSymbolIdToConstantCodeBlockMap;
    private HashMap<String, HashMap<Integer, Integer>> memory;

    private String code;
    private int codeByteLength;

    public CodeGenerator(Parser p_, OutputController oc_) {
        parser = p_;
        outputController = oc_;
        symbolTable = SymbolTable.getSymbolTable();
        utils = Utils.getUtils();
        literalSymbolIdToConstantCodeBlockMap = new HashMap<>();
        constantsCodeBlock = "";
        codeByteLength = 0;
        code = "";
        memory = new HashMap<>();
        memory.put("main", new HashMap<>());
    }

    /**
     * Initialise function runs the parser and pulls out the syntax tree
     */
    public void initialise() {
        parser.run();
        syntaxTree = parser.getSyntaxTree();
    }

    /**
     * Driver function to generate code from a syntax tree
     */
    public void run() {
        outputController.out(syntaxTree.toString());

        // Prepare the constant block at the bottom of the code
        prepareConstants();

        // Find main
        TreeNode mainNode = findNodeByType(syntaxTree, TreeNode.TreeNodes.NMAIN);
        ArrayList<TreeNode> localVars = new ArrayList<>();
        utils.flattenNodes(localVars, mainNode.getLeft(), TreeNode.TreeNodes.NSDLST);

        // Calculate required memory size
        int localVarSize = 0;
        for (int i = 0; i < localVars.size(); i++) {
            localVarSize++; // TODO this should go up more if it is a struct or array
            memory.get("main").put(localVars.get(i).getSymbolTableId(),
                i * 8); // TODO i may need to be offset if variables are structs or arrays
        }

        // Allocate memory space
        addToCode(OpCodes.LOAD_BYTE);
        addToCode(localVarSize);
        addToCode(OpCodes.ALLOCATE);

        // Set all variables to zero
        for (int i = 0; i < localVars.size(); i++) {
            addToCode(OpCodes.LA_MEMORY);
            addAddressToCode(i * 8);

            addToCode(OpCodes.ZERO);

            addToCode(OpCodes.STORE);
        }

        // Flatten the stats node of main and add them to the code
        ArrayList<TreeNode> stats = new ArrayList<>();
        utils.flattenNodes(stats, mainNode.getMid(), TreeNode.TreeNodes.NSTATS);
        addStats(stats);

        // Ensure we always have a halt, protects against programs that are exactly a multiple of 8
        // in length and don't auto pad with zeros
        addToCode(OpCodes.HALT);

        // Run the post processor to pad with zeros and replace the temporary references in the code with the exact values now that we know the address of the constants. Also break it up into neat 8 byte chunks
        postProcessCode();

        // After generating the code we now know what the exact addresses should be for constant references
        applyCodeOffsetToConstantOffsets();
        // Add the constants to the code
        code += constantsCodeBlock;
        System.out.println(code);

        outputController.outputCDFile(code);
    }

    /**
     * Add statements to the code
     * @param stats list of statements to be added
     */
    private void addStats(ArrayList<TreeNode> stats) {
        // Loop through the statements and call the relevant resolver based off of the node type
        for (TreeNode stat : stats) {
            switch (stat.getNodeType()) {
                case NPRLN:
                    printLine(stat.getLeft(), true);
                    break;
                case NPRINT:
                    printLine(stat.getLeft(), false);
                    addToCode(OpCodes.NEWlINE);
                    break;
                case NINPUT:
                    input(stat.getLeft());
                    break;
                case NASGN:
                case NPLEQ:
                case NMNEQ:
                case NSTEA:
                case NDVEQ:
                    resolveExpression(stat);
                    break;
                case NIFTH:
                    ifStatement(stat);
                    break;
                case NIFTE:
                    ifElseStatement(stat);
                    break;
                case NIFEF:
                    ifElseIfStatement(stat);
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Prepare the constant block
     */
    private void prepareConstants() {
        ArrayList<String> integers = new ArrayList<>();
        ArrayList<Integer> integersIds = new ArrayList<>();
        ArrayList<String> floats = new ArrayList<>();
        ArrayList<Integer> floatsIds = new ArrayList<>();
        ArrayList<String> strings = new ArrayList<>();
        ArrayList<Integer> stringsIds = new ArrayList<>();
        ArrayList<Integer> stringLengths = new ArrayList<>();

        // Get the list of literal symbols to loop through
        Set<Map.Entry<Integer, Symbol>> literalSymbols =
            symbolTable.getEntireSymbolScope("@literals");
        for (Map.Entry<Integer, Symbol> entry : literalSymbols) {
            // Cast the symbol for ease of calling member functions
            LiteralSymbol symbol = ((LiteralSymbol) entry.getValue());
            // Handle each type of symbol and add to the relevant lists
            if (symbol.getVal().startsWith("\"") && symbol.getVal().endsWith("\"")
                && symbol.getVal().length() > 1) {
                // String
                strings.add(symbol.getVal());
                stringsIds.add(symbolTable.getLiteralSymbolIdFromValue(symbol.getVal()));
            } else if (symbol.getVal().contains(".")) {
                // Float
                floats.add(symbol.getVal());
                floatsIds.add(symbolTable.getLiteralSymbolIdFromValue(symbol.getVal()));
            } else if (Long.parseLong(symbol.getVal()) >= Math.pow(2, 16)
                || Long.parseLong(symbol.getVal()) < 0) {
                // Integer that cannot be loaded inline
                integers.add(symbol.getVal());
                integersIds.add(symbolTable.getLiteralSymbolIdFromValue(symbol.getVal()));
            }
        }

        // Add integers to the constants block
        constantsCodeBlock = integers.size() + "\n";
        for (String i : integers) {
            constantsCodeBlock += i + "\n";
        }

        // Add floats to the constants block
        constantsCodeBlock = constantsCodeBlock.trim() + "\n" + floats.size() + "\n";
        for (String f : floats) {
            constantsCodeBlock += f + "\n";
        }


        // Convert strings to ascii and add to the constants block
        int stringSectionSize = 0;
        String stringSection = "";

        String line = "";
        int lengthCount = 0;
        for (String s : strings) {
            stringLengths.add(s.length() - 2); // Allow for " on either side
            for (Character c : s.substring(1, s.length() - 1).toCharArray()) {
                line += (int) c + " ";
                lengthCount++;
                if (lengthCount == 8) {
                    lengthCount = 0;
                    stringSectionSize++;
                    stringSection += line + "\n";
                    line = "";
                }
            }
            // Null pad between strings
            int stringLength = stringLengths.remove(stringLengths.size() - 1) + 1;
            stringLengths.add(stringLength);
            line += "0 ";
            lengthCount++;
            if (lengthCount == 8) {
                lengthCount = 0;
                stringSectionSize++;
                stringSection += line + "\n";
                line = "";
            }
        }
        // Pad remaining string section with zeros
        if (lengthCount > 0) {
            stringSection += line + "0 ".repeat(8 - lengthCount).trim();
            stringSectionSize++;
        }

        // Add the string block to the constants block
        constantsCodeBlock =
            constantsCodeBlock.trim() + "\n" + stringSectionSize + "\n" + stringSection;


        // Calculate a mapping between each symbol table ID and it's offset in the constants block for post processing
        int offset = 0;
        for (Integer integersId : integersIds) {
            literalSymbolIdToConstantCodeBlockMap.put(integersId, offset);
            offset += 8;
        }
        for (Integer floatsId : floatsIds) {
            literalSymbolIdToConstantCodeBlockMap.put(floatsId, offset);
            offset += 8;
        }
        for (int i = 0; i < stringsIds.size(); i++) {
            literalSymbolIdToConstantCodeBlockMap.put(stringsIds.get(i), offset);
            offset += stringLengths.get(i);
        }
    }

    /**
     * Add length of code to values in constants offset map
     */
    private void applyCodeOffsetToConstantOffsets() {
        for (Map.Entry<Integer, Integer> entry : literalSymbolIdToConstantCodeBlockMap.entrySet()) {
            entry.setValue(entry.getValue() + codeByteLength);
        }
    }

    /**
     *  Replace constant references with correct offsets after codegen, pad code with zeros, and break into 8 byte rows
     */
    private void postProcessCode() {
        // Calculate the padding offset that will be required to ensure constant offsets account for it
        int paddingOffset = 8 - (codeByteLength % 8);
        codeByteLength += paddingOffset;

        // Loop through and change every constant reference
        while (code.contains("@")) {
            // Get the code before and after the reference, and the address itself
            String before = code.substring(0, code.indexOf("@"));
            String after = code.substring(code.indexOf("#") + 1);
            int value = Integer.parseInt(code.substring(code.indexOf("@") + 1, code.indexOf("#")));

            // Calculate the new address as a 4 byte address
            ArrayList<String> address = convertLargeInteger(value + codeByteLength, 4);
            String newAddress = "";
            for (String addressPart : address) {
                newAddress += addressPart + " ";
            }
            // Reassemble code with the new address
            code = before + newAddress.trim() + after;
        }

        // Pad with zeros to a multiple of 8
        code += ("00 ".repeat(paddingOffset));
        // Split the code on a space to get an array of byte op codes
        ArrayList<String> codeArray = new ArrayList<>(Arrays.asList(code.split("\\s")));

        // Loop through the code array and generate rows of 8
        code = "";
        String line = "";
        int lineLength = 0;
        int codeLineNumber = 0;
        for (String instruction : codeArray) {
            line += instruction + " ";
            lineLength++;
            if (lineLength == 8) {
                code += line + "\n";
                line = "";
                lineLength = 0;
                codeLineNumber++;
            }
        }
        if (lineLength != 0) {
            code += line;
            codeLineNumber++;
        }
        code = codeLineNumber + "\n" + code;
    }

    /**
     * Add an instruction to the code, specifying if it is a constant reference that will need to be post processed
     * @param instruction the instruction to be added
     * @param isConstantReference a flag indicating whether it is a reference that should be post processed
     */
    private void addToCode(int instruction, boolean isConstantReference) {
        // Add the instruction to the code, with an accompanying top and tail if it is a reference
        code +=
            (isConstantReference ? "@" : "") + instruction + (isConstantReference ? "#" : "") + " ";
        // Addresses will be expanded to 4 bytes, so allow for this here even though it only takes up one space until post processing
        codeByteLength += isConstantReference ? 4 : 1;
    }

    /**
     * Add an instruction to the code
     * @param instruction the instruction to be added
     */
    private void addToCode(int instruction) {
        addToCode(instruction, false);
    }

    /**
     * Add an instruction to the code
     * @param instruction the instruction to be added
     */
    private void addToCode(OpCodes instruction) {
        code += instruction.getValue() + " ";
        codeByteLength++;
    }

    /**
     * Recursively find the node by a given node type
     * @param node node to be searched through
     * @param type a node type that is required
     * @return a node that matches the type that is somewhere in the tree provided by the node param
     */
    private TreeNode findNodeByType(TreeNode node, TreeNode.TreeNodes type) {
        // If the node has no children, or is the required node, return
        if (node == null || node.getNodeType() == type)
            return node;
        // Loop through the node's children and recursively call
        for (int i = 0; i < 3; i++) {
            TreeNode childNode = findNodeByType(node.getChildByIndex(i), type);
            if (childNode != null)
                return childNode;
        }
        return null;
    }

    /**
     * Printline operation
     * @param stat statement representing the printline data
     * @param isPrintLine flag indicating whether a newline should be added
     * @param register the memory register to use
     */
    private void printLine(TreeNode stat, boolean isPrintLine, String register) {
        switch (stat.getNodeType()) {
            case NSTRG:
                // If it is a string, it will need to be pulled from the constants block
                printLineAddSingleString(stat, isPrintLine, register);
                break;
            case NPRLST:
                // Flatten the list and recursively call
                ArrayList<TreeNode> printNodes = new ArrayList<>();
                utils.flattenNodes(printNodes, stat, TreeNode.TreeNodes.NPRLST);
                for (TreeNode printNode : printNodes) {
                    printLine(printNode, isPrintLine, register);
                }
                break;
            default:
                // Print the value indicated by the expression
                resolveExpression(stat, true);
                addToCode(OpCodes.PRINT_VAL);
                if (isPrintLine)
                    addToCode(OpCodes.NEWlINE);
        }
    }

    /**
     * Add code to print a single string
     * @param stringStat Statement indicating the string to print
     * @param isPrintLine Flag indicating if a newline should be appended
     * @param register The register to load the address from
     */
    private void printLineAddSingleString(
        TreeNode stringStat, boolean isPrintLine, String register) {
        // Get the offset using the symbol of the string statement and add it to the code
        int stringOffset = literalSymbolIdToConstantCodeBlockMap.get(stringStat.getSymbolTableId());
        addToCode(OpCodes.getEnum(Integer.parseInt(9 + register)));
        addToCode(stringOffset, true);
        // Print the data followed by a space, and a newline if necessary
        addToCode(OpCodes.PRINT_STR);
        addToCode(OpCodes.SPACE);
        if (isPrintLine)
            addToCode(OpCodes.NEWlINE);
    }

    /**
     * Overloaded function to automatically print from register 0, main
     * @param stat statement to be printed
     * @param isPrintLine flag indicating whether a newline should be appended
     */
    private void printLine(TreeNode stat, boolean isPrintLine) {
        printLine(stat, isPrintLine, "0");
    }

    /**
     * Utility function to convert an integer into a specified number of bytes
     * @param largeInteger number to be converted
     * @param numberOfValues number of bytes to be output
     * @return list of bytes
     */
    private ArrayList<String> convertLargeInteger(long largeInteger, int numberOfValues) {
        ArrayList<String> values = new ArrayList<>();
        // Convert the integer into a binary string
        String binaryString = Long.toBinaryString(largeInteger);
        // Left pad with a number of zeros that will correspond to the desired number of bytes
        binaryString = "0".repeat((numberOfValues * 8) - binaryString.length()) + binaryString;

        // Loop through the binary string and convert chunks of 8 back into string representations of integers
        for (int i = 0; i < numberOfValues; i++) {
            String binaryValue = binaryString.substring(i * 8, (i + 1) * 8);
            values.add(String.valueOf(Integer.parseInt(binaryValue, 2)));
        }

        return values;
    }

    /**
     * Overloaded function to automatically load address when resolving expresion
     * @param expressionNode node to be resolved
     */
    private void resolveExpression(TreeNode expressionNode) {
        resolveExpression(expressionNode, false);
    }

    /**
     * Resolve expression operation
     * @param expressionNode node to be resolved
     * @param loadValue flag indicating whether the expression should load by value, toggled during assignment operations
     */
    private void resolveExpression(TreeNode expressionNode, boolean loadValue) {
        if (expressionNode == null)
            return;
        for (int i = 0; i < 3; i++) {
            // Assignment operators should load by value for the remainder of their siblings
            if ((expressionNode.getNodeType() == TreeNode.TreeNodes.NASGN
                    || expressionNode.getNodeType() == TreeNode.TreeNodes.NPLEQ
                    || expressionNode.getNodeType() == TreeNode.TreeNodes.NMNEQ
                    || expressionNode.getNodeType() == TreeNode.TreeNodes.NSTEA
                    || expressionNode.getNodeType() == TreeNode.TreeNodes.NDVEQ)
                && i == 1) {
                loadValue = true;
                // Assignment operators don't need to keep track of the original value of the variable, mutator assignment operators do
                if (expressionNode.getNodeType() != TreeNode.TreeNodes.NASGN) {
                    addToCode(OpCodes.DUPLICATE);
                    addToCode(OpCodes.LOAD_VALUE_AT_ADDRESS);
                }
            }
            // Recursively resolve children
            resolveExpression(expressionNode.getChildByIndex(i), loadValue);
        }

        // Switch on the node type and add the corresponding operation for each
        switch (expressionNode.getNodeType()) {
            case NASGN:
                addToCode(OpCodes.STORE);
                break;
            case NPLEQ:
                addToCode(OpCodes.ADD);
                addToCode(OpCodes.STORE);
                break;
            case NMNEQ:
                addToCode(OpCodes.SUB);
                addToCode(OpCodes.STORE);
                break;
            case NSTEA:
                addToCode(OpCodes.MUL);
                addToCode(OpCodes.STORE);
                break;
            case NDVEQ:
                addToCode(OpCodes.DIV);
                addToCode(OpCodes.STORE);
                break;
            case NSIMV:
                // If on the left side of an assignment operator, we'll load by address. On the right we load by value
                if (loadValue) {
                    addToCode(OpCodes.LV_MEMORY);
                } else {
                    addToCode(OpCodes.LA_MEMORY);
                }
                addAddressToCode(memory.get("main").get(expressionNode.getSymbolTableId()));
                break;
            case NADD:
                addToCode(OpCodes.ADD);
                break;
            case NSUB:
                addToCode(OpCodes.SUB);
                break;
            case NMUL:
                addToCode(OpCodes.MUL);
                break;
            case NDIV:
                addToCode(OpCodes.DIV);
                break;
            case NMOD:
                addToCode(OpCodes.MOD);
                break;
            case NPOW:
                addToCode(OpCodes.POW);
                break;
            case NILIT:
                // Get the literal integer value from the symbol table
                String intLitValue =
                    ((LiteralSymbol) symbolTable.getSymbol(expressionNode.getSymbolTableId()))
                        .getVal();
                // Only load if it is a large number. Smaller numbers will be added manually into instruction space
                if ((Long.parseLong(intLitValue) >= Math.pow(2, 16))
                    || Long.parseLong(intLitValue) < 0) {
                    addToCode(OpCodes.LV_INSTRUCTION);
                    addToCode(literalSymbolIdToConstantCodeBlockMap.get(
                                  expressionNode.getSymbolTableId()),
                        true);
                } else {
                    // If the number is small enough we can load by value in the instruction space to save time and operations loading values
                    long numberValue = Long.parseLong(intLitValue);
                    ArrayList<String> literalValue = convertLargeInteger(numberValue, 2);
                    addToCode(OpCodes.LOAD_HIGH);
                    addToCode(Integer.parseInt(literalValue.get(0)));
                    addToCode(Integer.parseInt(literalValue.get(1)));
                }
                break;
            case NFLIT:
                // Get the literal float value from the symbol table and add it with a load value
                addToCode(OpCodes.LV_INSTRUCTION);
                addToCode(
                    literalSymbolIdToConstantCodeBlockMap.get(expressionNode.getSymbolTableId()),
                    true);
                break;
            default:
                // Shouldn't happen, nothing else should call this function if semantic checking has
                // done its job :)
                break;
        }
    }

    /**
     * Resolve boolean expression node
     * @param expressionNode node to be resolved
     */
    private void resolveBooleanExpression(TreeNode expressionNode) {
        if (expressionNode == null)
            return;

        // Logop expression nodes are left operation right rather than operation with left and right
        // children like regular expressions. Preprocess these nodes to add the left and right
        // siblings of the operation node as its children instead. Some weird family tree going on
        // here but don't question it
        if (expressionNode.getChildByIndex(1) != null
            && (expressionNode.getChildByIndex(1).getNodeType() == TreeNode.TreeNodes.NAND
                || expressionNode.getChildByIndex(1).getNodeType() == TreeNode.TreeNodes.NOR
                || expressionNode.getChildByIndex(1).getNodeType() == TreeNode.TreeNodes.NXOR)) {
            expressionNode.getMid().setNextChild(expressionNode.getLeft());
            expressionNode.getMid().setNextChild(expressionNode.getRight());
            expressionNode = expressionNode.getMid();
        }

        // Loop through children and recursively call
        for (int i = 0; i < 3; i++) {
            resolveBooleanExpression(expressionNode.getChildByIndex(i));
        }

        // Switch on the node tye and add the corresponding operation code
        switch (expressionNode.getNodeType()) {
            case NAND:
                addToCode(OpCodes.AND);
                break;
            case NOR:
                addToCode(OpCodes.OR);
                break;
            case NXOR:
                addToCode(OpCodes.XOR);
                break;
            case NTRUE:
                addToCode(OpCodes.TRUE);
                break;
            case NFALS:
                addToCode(OpCodes.FALSE);
                break;
            case NEQL:
                addToCode(OpCodes.SUB);
                addToCode(OpCodes.EQ);
                break;
            case NNEQ:
                addToCode(OpCodes.SUB);
                addToCode(OpCodes.NE);
                break;
            case NLEQ:
                addToCode(OpCodes.SUB);
                addToCode(OpCodes.LE);
                break;
            case NGEQ:
                addToCode(OpCodes.SUB);
                addToCode(OpCodes.GE);
                break;
            case NLSS:
                addToCode(OpCodes.SUB);
                addToCode(OpCodes.LT);
                break;
            case NGRT:
                addToCode(OpCodes.SUB);
                addToCode(OpCodes.GT);
                break;
            case NNOT:
                addToCode(OpCodes.NOT);
                break;
            default:
                resolveExpression(expressionNode, true);
        }
    }

    /**
     * Add an address to the instruction space
     * @param address to be added
     */
    private void addAddressToCode(int address) {
        // Convert to 4 bytes then add each part to the code
        ArrayList<String> byteAddress = convertLargeInteger(address, 4);
        for (String addressPart : byteAddress) {
            addToCode(Integer.parseInt(addressPart));
        }
    }

    /**
     * Input operation
     * @param stat statement node related to input
     */
    private void input(TreeNode stat) {
        switch (stat.getNodeType()) {
            case NSIMV:
                // Simple variable, get the symbol
                Symbol symbol = symbolTable.getSymbol(stat.getSymbolTableId());
                if (symbol instanceof PrimitiveTypeSymbol) {
                    // Load the address of the variable
                    addToCode(OpCodes.LA_MEMORY);
                    addAddressToCode(memory.get("main").get(stat.getSymbolTableId()));
                    if (((PrimitiveTypeSymbol) symbol).getVal()
                        == SymbolTable.PrimitiveTypes.INTEGER) {
                        // Read an integer
                        addToCode(OpCodes.READ_INT);
                    } else if (((PrimitiveTypeSymbol) symbol).getVal()
                        == SymbolTable.PrimitiveTypes.FLOAT) {
                        // Read a float
                        addToCode(OpCodes.READ_FLOAT);
                    }
                    // Save the value into the address loaded prior
                    addToCode(OpCodes.STORE);
                }
                // TODO extend this for structs and arrays
                break;
            case NVLIST:
                // Flatten list the recursively call
                ArrayList<TreeNode> inputNodes = new ArrayList<>();
                utils.flattenNodes(inputNodes, stat, TreeNode.TreeNodes.NVLIST);
                for (TreeNode inputNode : inputNodes) {
                    input(inputNode);
                }
                break;
            default:
                break;
        }
    }

    /**
     * If statement operation
     * @param stat statement to be resolved
     */
    private void ifStatement(TreeNode stat) {
        // Save the previous opcodes so we can build this block fresh
        String previousCode = code;
        code = "";

        // Evaluate the boolean expression into op codes
        resolveBooleanExpression(stat.getLeft());

        // Save the boolean expression op codes and return code to it's state before the if
        // statement parsing
        String booleanCode = code;
        code = "";

        // Evaluate all the statements in order to determine the jump to position if the statement
        // is false
        ArrayList<TreeNode> stats = new ArrayList<>();
        utils.flattenNodes(stats, stat.getMid(), TreeNode.TreeNodes.NSTATS);
        addStats(stats);

        // Save the statements evaluated and return the code to its state before parsing the block
        String statCode = code;
        code = previousCode;

        // Load in the jump to address that will be used for a false statement
        addToCode(OpCodes.LA_INSTRUCTION);
        // Allow for the 4 byte instruction about to be added, and the BRANCH op code added just
        // below as well
        addAddressToCode(codeByteLength + 5);

        // Add the boolean expression code which will evaluate to a true or false to determine if
        // the program should jump
        code += booleanCode;
        // Ensure we jump if false, otherwise continue through to the next op code
        addToCode(OpCodes.BF);

        // Finally add the statement code that is inside the if block
        code += statCode;
    }

    /**
     * If else statement operation
     * @param stat statement to be resolved
     */
    private void ifElseStatement(TreeNode stat) {
        // Save the previous opcodes so we can build this block fresh
        String previousCode = code;
        code = "";

        // Evaluate the boolean expression into op codes
        resolveBooleanExpression(stat.getLeft());

        // Save the boolean expression op codes and reset for the if block
        String booleanCode = code;
        code = "";

        // Evaluate all the if statements in order to determine the jump to position if the
        // statement is false
        ArrayList<TreeNode> ifStats = new ArrayList<>();
        utils.flattenNodes(ifStats, stat.getMid(), TreeNode.TreeNodes.NSTATS);
        addStats(ifStats);

        // Save the if statements evaluated so we can build the else block fresh
        String ifStatCode = code;
        code = "";

        // Save the address of the end of the if block so we can jump here if the statement is false
        int endOfIfAddress = codeByteLength;

        // Evaluate all the else statements in order to determine the jump to position if the
        // statement is false
        ArrayList<TreeNode> elseStats = new ArrayList<>();
        utils.flattenNodes(elseStats, stat.getRight(), TreeNode.TreeNodes.NSTATS);
        addStats(elseStats);

        // Save the statements evaluated and reset the code to the state it was before the block
        // started so we can start assembling
        String elseStatCode = code;
        code = previousCode;

        // Load in the jump to address that will be used for a false statement
        addToCode(OpCodes.LA_INSTRUCTION);
        // Jump to the start of the else if the statement is false. Add 13 to the address.
        // This accounts for the initial jump consisting of a 4 byte address, an LA and a BRANCH
        // command (6) and also accounts for the jump at the end of the if over the else block
        // consisting of a 4 byte address, an LA, BRANCH, and TRUE (7)
        addAddressToCode(endOfIfAddress + 13);

        // Add the boolean expression code which will evaluate to a true or false to determine where
        // the program should jump to
        code += booleanCode;
        // Jump to the else if false, otherwise continue on like normal into the if code
        addToCode(OpCodes.BF);

        // Add the statement code that is inside the if block
        code += ifStatCode;

        // Load in the jump to address that will be used if the if block has run to jump over the
        // else
        addToCode(OpCodes.LA_INSTRUCTION);
        // Allow for the 4 byte instruction about to be added, and the TRUE and BRANCH op codes
        // added just below as well
        addAddressToCode(codeByteLength + 6);
        addToCode(OpCodes.TRUE);
        addToCode(OpCodes.BT);

        // Finally add in the else statement code
        code += elseStatCode;
    }

    /**
     * If elseif statement operation
     * @param stat statement to be resolved
     */
    private void ifElseIfStatement(TreeNode stat) {
        // Save the previous opcodes so we can build this block fresh
        String previousCode = code;
        code = "";

        // Evaluate the boolean expression into op codes
        resolveBooleanExpression(stat.getLeft());

        // Save the boolean expression op codes and reset for the if block
        String ifBooleanCode = code;
        code = "";

        // Evaluate all the if statements in order to determine the jump to position if the
        // statement is false
        ArrayList<TreeNode> ifStats = new ArrayList<>();
        utils.flattenNodes(ifStats, stat.getMid(), TreeNode.TreeNodes.NSTATS);
        addStats(ifStats);

        // Save the if statements evaluated so we can build the else block fresh
        String ifStatCode = code;
        code = "";

        // Save the address of the end of the if block so we can jump here if the statement is false
        int endOfIfAddress = codeByteLength;

        // Evaluate the boolean expression into op codes
        resolveBooleanExpression(stat.getRight().getLeft());

        // Save the boolean expression op codes and reset for the if block
        String elseBooleanCode = code;
        code = "";

        // Evaluate all the else statements in order to determine the jump to position if the
        // statement is false
        ArrayList<TreeNode> elseStats = new ArrayList<>();
        utils.flattenNodes(elseStats, stat.getRight().getMid(), TreeNode.TreeNodes.NSTATS);
        addStats(elseStats);

        // Save the statements evaluated and reset the code to the state it was before the block
        // started so we can start assembling
        String elseStatCode = code;
        code = previousCode;

        // Load in the jump to address that will be used for a false statement
        addToCode(OpCodes.LA_INSTRUCTION);
        // Jump to the start of the else if the statement is false. Add 13 to the address.
        // This accounts for the initial jump consisting of a 4 byte address, an LA and a BRANCH
        // command (6) and also accounts for the jump at the end of the if over the else block
        // consisting of a 4 byte address, an LA, BRANCH, and TRUE (7)
        addAddressToCode(endOfIfAddress + 13);

        // Add the boolean expression code which will evaluate to a true or false to determine where
        // the program should jump to
        code += ifBooleanCode;
        // Jump to the else if false, otherwise continue on like normal into the if code
        addToCode(OpCodes.BF);

        // Add the statement code that is inside the if block
        code += ifStatCode;

        // Load in the jump to address that will be used if the if block has run to jump over the
        // else
        addToCode(OpCodes.LA_INSTRUCTION);
        // Allow for the 4 byte instruction about to be added, and the TRUE and BRANCH op codes
        // added just below as well
        addAddressToCode(codeByteLength + 12);
        addToCode(OpCodes.TRUE);
        addToCode(OpCodes.BT);

        // Load in the jump to address that will be used if the if block has run to jump over the
        // else
        addToCode(OpCodes.LA_INSTRUCTION);
        // Allow for the 4 byte instruction about to be added, and the TRUE and BRANCH op codes
        // added just below as well
        addAddressToCode(codeByteLength + 5);
        code += elseBooleanCode;
        addToCode(OpCodes.BF);

        // Finally add in the else statement code
        code += elseStatCode;
    }
}