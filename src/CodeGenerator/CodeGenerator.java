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

    public void initialise() {
        parser.run();
        syntaxTree = parser.getSyntaxTree();
    }

    /**
     * Run the parser, and then output the syntax tree. In order for the parser to run, it must have
     * already been initialised
     */
    public void run() {
        outputController.out(syntaxTree.toString());
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
        ArrayList<TreeNode> stats = new ArrayList<>();
        utils.flattenNodes(stats, mainNode.getMid(), TreeNode.TreeNodes.NSTATS);

        addStats(stats);

        postProcessCode();

        applyCodeOffsetToConstantOffsets();
        code += constantsCodeBlock;
        System.out.println(code);

        try {
            // TODO fix this output :)
            FileWriter writer = new FileWriter("../../../../Downloads/SM22/Test.mod");
            writer.write(code + "\n");
            writer.close();
        } catch (IOException e) {
        }
    }

    private void addStats(ArrayList<TreeNode> stats) {
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
                default:
                    break;
            }
        }
    }

    private void prepareConstants() {
        ArrayList<String> integers = new ArrayList<>();
        ArrayList<Integer> integersIds = new ArrayList<>();
        ArrayList<String> floats = new ArrayList<>();
        ArrayList<Integer> floatsIds = new ArrayList<>();
        ArrayList<String> strings = new ArrayList<>();
        ArrayList<Integer> stringsIds = new ArrayList<>();
        ArrayList<Integer> stringLengths = new ArrayList<>();

        Set<Map.Entry<Integer, Symbol>> literalSymbols =
            symbolTable.getEntireSymbolScope("@literals");
        for (Map.Entry<Integer, Symbol> entry : literalSymbols) {
            LiteralSymbol symbol = ((LiteralSymbol) entry.getValue());
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

        constantsCodeBlock = integers.size() + "\n";
        for (String i : integers) {
            constantsCodeBlock += i + "\n";
        }

        constantsCodeBlock = constantsCodeBlock.trim() + "\n" + floats.size() + "\n";
        for (String f : floats) {
            constantsCodeBlock += f + "\n";
        }

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
        if (lengthCount > 0) {
            stringSection += line + "0 ".repeat(8 - lengthCount).trim();
            stringSectionSize++;
        }

        constantsCodeBlock =
            constantsCodeBlock.trim() + "\n" + stringSectionSize + "\n" + stringSection;

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

    private void applyCodeOffsetToConstantOffsets() {
        for (Map.Entry<Integer, Integer> entry : literalSymbolIdToConstantCodeBlockMap.entrySet()) {
            entry.setValue(entry.getValue() + codeByteLength);
        }
    }

    private void postProcessCode() {
        int paddingOffset = 8 - (codeByteLength % 8);
        codeByteLength += paddingOffset;

        while (code.contains("@")) {
            String before = code.substring(0, code.indexOf("@"));
            String after = code.substring(code.indexOf("#") + 1);
            int value = Integer.parseInt(code.substring(code.indexOf("@") + 1, code.indexOf("#")));

            ArrayList<String> address = convertLargeInteger(value + codeByteLength, 4);
            String newAddress = "";
            for (String addressPart : address) {
                newAddress += addressPart + " ";
            }
            code = before + newAddress.trim() + after;
        }

        code += ("00 ".repeat(paddingOffset));
        ArrayList<String> codeArray = new ArrayList<>(Arrays.asList(code.split("\\s")));
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

    private void addToCode(int instruction, boolean isConstantReference) {
        code +=
            (isConstantReference ? "@" : "") + instruction + (isConstantReference ? "#" : "") + " ";
        codeByteLength += isConstantReference ? 4 : 1;
    }

    private void addToCode(int instruction) {
        addToCode(instruction, false);
    }

    private void addToCode(OpCodes instruction) {
        code += instruction.getValue() + " ";
        codeByteLength++;
    }

    private TreeNode findNodeByType(TreeNode node, TreeNode.TreeNodes type) {
        if (node == null || node.getNodeType() == type)
            return node;
        for (int i = 0; i < 3; i++) {
            TreeNode childNode = findNodeByType(node.getChildByIndex(i), type);
            if (childNode != null)
                return childNode;
        }
        return null;
    }

    private void printLine(TreeNode stat, boolean isPrintLine, String register) {
        switch (stat.getNodeType()) {
            case NSTRG:
                printLineAddSingleString(stat, isPrintLine, register);
                break;
            case NPRLST:
                ArrayList<TreeNode> printNodes = new ArrayList<>();
                utils.flattenNodes(printNodes, stat, TreeNode.TreeNodes.NPRLST);
                for (TreeNode printNode : printNodes) {
                    printLine(printNode, isPrintLine, register);
                }
                break;
            default:
                resolveExpression(stat, true);
                addToCode(OpCodes.PRINT_VAL);
                if (isPrintLine)
                    addToCode(OpCodes.NEWlINE);
        }
    }

    private void printLineAddSingleString(
        TreeNode stringStat, boolean isPrintLine, String register) {
        int stringOffset = literalSymbolIdToConstantCodeBlockMap.get(stringStat.getSymbolTableId());
        addToCode(OpCodes.getEnum(Integer.parseInt(9 + register)));
        addToCode(stringOffset, true);
        addToCode(OpCodes.PRINT_STR);
        addToCode(OpCodes.SPACE);
        if (isPrintLine)
            addToCode(OpCodes.NEWlINE);
    }

    private void printLine(TreeNode stat, boolean isPrintLine) {
        printLine(stat, isPrintLine, "0");
    }

    private ArrayList<String> convertLargeInteger(long largeInteger, int numberOfValues) {
        ArrayList<String> values = new ArrayList<>();
        String binaryString = Long.toBinaryString(largeInteger);
        binaryString = "0".repeat((numberOfValues * 8) - binaryString.length()) + binaryString;

        for (int i = 0; i < numberOfValues; i++) {
            String binaryValue = binaryString.substring(i * 8, (i + 1) * 8);
            values.add(String.valueOf(Integer.parseInt(binaryValue, 2)));
        }

        return values;
    }

    private void resolveExpression(TreeNode expressionNode) {
        resolveExpression(expressionNode, false);
    }

    private void resolveExpression(TreeNode expressionNode, boolean loadValue) {
        if (expressionNode == null)
            return;
        for (int i = 0; i < 3; i++) {
            if ((expressionNode.getNodeType() == TreeNode.TreeNodes.NASGN
                    || expressionNode.getNodeType() == TreeNode.TreeNodes.NPLEQ
                    || expressionNode.getNodeType() == TreeNode.TreeNodes.NMNEQ
                    || expressionNode.getNodeType() == TreeNode.TreeNodes.NSTEA
                    || expressionNode.getNodeType() == TreeNode.TreeNodes.NDVEQ)
                && i == 1) {
                loadValue = true;
                if (expressionNode.getNodeType() != TreeNode.TreeNodes.NASGN) {
                    addToCode(OpCodes.DUPLICATE);
                    addToCode(OpCodes.LOAD_VALUE_AT_ADDRESS);
                }
            }
            resolveExpression(expressionNode.getChildByIndex(i), loadValue);
        }

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
                String intLitValue =
                    ((LiteralSymbol) symbolTable.getSymbol(expressionNode.getSymbolTableId()))
                        .getVal();
                if ((Long.parseLong(intLitValue) >= Math.pow(2, 16))
                    || Long.parseLong(intLitValue) < 0) {
                    addToCode(OpCodes.LV_INSTRUCTION);
                    addToCode(literalSymbolIdToConstantCodeBlockMap.get(
                                  expressionNode.getSymbolTableId()),
                        true);
                } else {
                    long numberValue = Long.parseLong(intLitValue);
                    ArrayList<String> literalValue = convertLargeInteger(numberValue, 2);
                    addToCode(OpCodes.LOAD_HIGH);
                    addToCode(Integer.parseInt(literalValue.get(0)));
                    addToCode(Integer.parseInt(literalValue.get(1)));
                }
                break;
            case NFLIT:
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

        for (int i = 0; i < 3; i++) {
            resolveBooleanExpression(expressionNode.getChildByIndex(i));
        }

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

    private void addAddressToCode(int address) {
        ArrayList<String> byteAddress = convertLargeInteger(address, 4);
        for (String addressPart : byteAddress) {
            addToCode(Integer.parseInt(addressPart));
        }
    }

    private void input(TreeNode stat) {
        switch (stat.getNodeType()) {
            case NSIMV:
                Symbol symbol = symbolTable.getSymbol(stat.getSymbolTableId());
                if (symbol instanceof PrimitiveTypeSymbol) {
                    addToCode(OpCodes.LA_MEMORY);
                    addAddressToCode(memory.get("main").get(stat.getSymbolTableId()));
                    if (((PrimitiveTypeSymbol) symbol).getVal()
                        == SymbolTable.PrimitiveTypes.INTEGER) {
                        addToCode(OpCodes.READ_INT);
                    } else if (((PrimitiveTypeSymbol) symbol).getVal()
                        == SymbolTable.PrimitiveTypes.FLOAT) {
                        addToCode(OpCodes.READ_FLOAT);
                    }
                    addToCode(OpCodes.STORE);
                }
                // TODO extend this for structs and arrays
                break;
            case NVLIST:
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

    private void ifStatement(TreeNode stat) {
        // Save the previous opcodes so we can build this block fresh
        String previousCode = code;
        code = "";

        // Evaluate all the statements in order to determine the jump to position if the statement
        // is false
        ArrayList<TreeNode> stats = new ArrayList<>();
        utils.flattenNodes(stats, stat.getMid(), TreeNode.TreeNodes.NSTATS);
        addStats(stats);

        // Save the statements evaluated so we can build the boolean expression fresh
        String statCode = code;
        code = "";

        // Evaluate the boolean expression into op codes
        resolveBooleanExpression(stat.getLeft());

        // Save the boolean expression op codes and return code to it's state before the if
        // statement parsing
        String booleanCode = code;
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

        // Add the statements here
    }
}