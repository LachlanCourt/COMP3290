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
                    i * 8); // TODO i may need to be offset if variables are large
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
                    resolveExpression(stat);
                    break;
                default:
                    break;
            }
        }

        postProcessCode();

        applyCodeOffsetToConstantOffsets();
        code += constantsCodeBlock;
        System.out.println(code);

        try {
            FileWriter writer = new FileWriter("../../../../Downloads/SM22/Test.mod");
            writer.write(code + "\n");
            writer.close();
        } catch (IOException e) {

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
            } else if (Long.parseLong(symbol.getVal()) >= Math.pow(2, 16) || Long.parseLong(symbol.getVal()) < 0) {
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
                resolveExpression(stat);
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
        // TODO fix this
        for (int i = 1; i < numberOfValues; i++) {
            values.add("00");
        }
        values.add(String.valueOf(largeInteger));
        return values;
    }

    private void resolveExpression(TreeNode expressionNode) {
        if (expressionNode == null) return;
        for (int i = 0; i < 3; i++) {
            resolveExpression(expressionNode.getChildByIndex(i));
        }

        switch (expressionNode.getNodeType()) {
            case NASGN:
                addToCode(OpCodes.STORE);
                break;
            case NSIMV:
                addToCode(OpCodes.LV_MEMORY);
                addAddressToCode(memory.get("main").get(expressionNode.getSymbolTableId()));
                break;
            case NADD:
                addToCode(OpCodes.ADD);
                break;
            case NILIT:
                String value = ((LiteralSymbol)symbolTable.getSymbol(expressionNode.getSymbolTableId())).getVal();
                if ((Long.parseLong(value) >= Math.pow(2, 16)) || Long.parseLong(value) < 0) {
                    addToCode(OpCodes.LV_INSTRUCTION);
                    addToCode(literalSymbolIdToConstantCodeBlockMap.get(expressionNode.getSymbolTableId()), true);
            } else {
                    long numberValue = Long.parseLong(value);
                    ArrayList<String> literalValue =  convertLargeInteger(numberValue, 2);
                    addToCode(OpCodes.LOAD_HIGH);
                    addToCode(Integer.parseInt(literalValue.get(0)));
                    addToCode(Integer.parseInt(literalValue.get(1)));
                }
                break;
            default:
                int i = 0;
                i+=3;
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
}