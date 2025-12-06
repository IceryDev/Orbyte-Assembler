package Resources;

public enum Instruction {
    MOV((byte)0b00, (byte)0b0000, 2, true, true),
    SUM((byte)0b00, (byte)0b0001, 3, true, true), //Same as ADD in ARM
    DIF((byte)0b00, (byte)0b0010, 3, true, true), //DIFference, ARM equivalent - SUB
    DFR((byte)0b00, (byte)0b0011, 3, true, true), //DiFference Reverse, ARM equivalent - RSB
    AND((byte)0b00, (byte)0b0100, 3, true, true),
    LOR((byte)0b00, (byte)0b0101, 3, true, true), //Logical OR, ARM equivalent - ORR
    XOR((byte)0b00, (byte)0b0110, 3, true, true), //ARM equivalent - EOR
    NOT((byte)0b00, (byte)0b0111, 3, true, true),
    CMP((byte)0b00, (byte)0b1000, 2, false, true),
    LCP((byte)0b00, (byte)0b1001, 2, false, true), //Logical ComPare, ARM equivalent - TST


    LSL((byte)0b01, (byte)0b0000, 2, true, true),
    LSR((byte)0b01, (byte)0b0001, 2, true, true),
    ASR((byte)0b01, (byte)0b0010, 2, true, true),
    ROT((byte)0b01, (byte)0b0011, 2, true, true),
    STR((byte)0b10, (byte)0b0000, 2, false, false),
    PUL((byte)0b10, (byte)0b0001, 2, false, true), //PULl, ARM equivalent - LDR

    B((byte)0b11, (byte)0b0000, 1, false, false);



    byte opType = 0b0;
    byte opcode = 0b0;
    int operandCount = 0;
    boolean hasFlagBit = false;
    boolean immediateAllowed = false;

    Instruction (byte opType, byte opcode, int operandCount, boolean hasFlagBit, boolean immediateAllowed){
        this.opType = opType;
        this.opcode = opcode;
        this.operandCount = operandCount;
        this.hasFlagBit = hasFlagBit;
        this.immediateAllowed = immediateAllowed;
    }

    public static boolean isInstruction(String name){
        try {
            Instruction.valueOf(name.toUpperCase());
            return true;
        }
        catch (IllegalArgumentException error){
            return false;
        }
    }

    public byte getOpType(){
        return this.opType;
    }

    public byte getOpcode(){
        return this.opcode;
    }

    public int getOpCount(){
        return this.operandCount;
    }
}
