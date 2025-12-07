package Resources;

public enum Instruction {
    MOV((byte)0b00, (byte)0b000, 2, true, true),
    SUM((byte)0b00, (byte)0b001, 3, true, true), //Same as ADD in ARM
    DIF((byte)0b00, (byte)0b010, 3, true, true), //DIFference, ARM equivalent - SUB
    DFR((byte)0b00, (byte)0b011, 3, true, true), //DiFference Reverse, ARM equivalent - RSB
    AND((byte)0b00, (byte)0b100, 3, true, true),
    LOR((byte)0b00, (byte)0b101, 3, true, true), //Logical OR, ARM equivalent - ORR
    XOR((byte)0b00, (byte)0b110, 3, true, true), //ARM equivalent - EOR
    NOT((byte)0b00, (byte)0b111, 3, true, true),

    CMP((byte)0b01, (byte)0b000, 2, false, true),
    LCP((byte)0b01, (byte)0b001, 2, false, true), //Logical ComPare, ARM equivalent - TST
    LSL((byte)0b01, (byte)0b010, 2, true, true),
    LSR((byte)0b01, (byte)0b011, 2, true, true),
    ASR((byte)0b01, (byte)0b100, 2, true, true),
    ROT((byte)0b01, (byte)0b101, 2, true, true),

    WRM((byte)0b10, (byte)0b110, 2, true, false), //WRite to Memory, ARM equivalent - STR
    FRM((byte)0b10, (byte)0b111, 2, true, true), //FRom Memory, ARM equivalent - LDR

    B((byte)0b11, (byte)0b000, 1, false, false);

    public static final int OPTYPE_BIT_COUNT = 2;
    public static final int OPCODE_BIT_COUNT = 3;
    public static final int FLAG_BIT_COUNT = 1;
    public static final int IMM_FLAG_BIT_COUNT = 1;

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

    public boolean getHasFlagBit(){
        return this.hasFlagBit;
    }

    public boolean isImmediateAllowed() {
        return immediateAllowed;
    }
}
