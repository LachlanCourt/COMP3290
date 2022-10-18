package CodeGenerator;

import java.util.ArrayList;
import java.util.Arrays;

public enum OpCodes {
    HALT(0), NO_OP(1), TRAP(2), ZERO(3), FALSE(4), TRUE(5), SWAP_TYPE(7), SET_TYPE_INT(8), SET_TYPE_FLOAT(9), ADD(11), SUB(12), MUL(13), DIV(14), MOD(15), POW(16), REVERSE_ABSOLUTE(17), ABSOLUTE(18), GT(21), GE(22), LT(23), LE(24), EQ(25), NE(26), AND(31), OR(32), XOR(33), NOT(34), BT(35), BF(36), BR(37), LOAD_VALUE_AT_ADDRESS(40), LOAD_BYTE(41), LOAD_HIGH(42), STORE(43), STEP(51), ALLOCATE(52), ARRAY(53), INDEX(54), SIZE(55), DUPLICATE(56), READ_FLOAT(60), READ_INT(61), PRINT_VAL(62), PRINT_STR(63), PRINT_CHAR(64), NEWlINE(65), SPACE(66), RETURN_VAL(70), RETURN(71), JS2(72), LV_INSTRUCTION(80), LV_MEMORY(81), LV_FUNCTION(82), LA_INSTRUCTION(90), LA_MEMORY(91), LA_FUNCTION(92), ERROR(-1);

    private int value;

    OpCodes(int value_) {
        value = value_;
    }

    public int getValue() {
        return value;
    }

    public static OpCodes getEnum(int value) {
        switch(value) {
            case 80: return LV_INSTRUCTION;
            case 81: return LV_MEMORY;
            case 82: return LV_FUNCTION;
            case 90: return LA_INSTRUCTION;
            case 91: return LA_MEMORY;
            case 92: return LA_FUNCTION;
            default: return ERROR;
        }
    }
}
