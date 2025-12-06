package Resources;

public enum Register { //In the case of expanding instruction size (in bits) you can make each 4 bits long
                       //Do not forget to change the value you pass to the packer (3 to 4).
    R0((byte) 0b000),
    R1((byte) 0b001),
    R2((byte) 0b010),
    R3((byte) 0b011),
    R4((byte) 0b100),
    R5((byte) 0b101),
    R6((byte) 0b110),
    R7((byte) 0b111),
    PC((byte) 0b111); //Same as R7

    byte regCode = 0b0;

    Register (byte regCode){
        this.regCode = regCode;
    }

    public static boolean isRegister(String name){
        try {
            Register.valueOf(name.toUpperCase());
            return true;
        }
        catch (IllegalArgumentException error){
            return false;
        }
    }
}
