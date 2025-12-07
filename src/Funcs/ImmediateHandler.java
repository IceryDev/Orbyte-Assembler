package Funcs;

public class ImmediateHandler {
    public final String BINARY_PREFIX = "0b";
    public final String HEXADECIMAL_PREFIX = "0x";
    public final int PREFIX_END_INDEX = 3;
    public final int DELIMITER_END_INDEX = 1;

    //Takes a negative value and shortens the value to represent the same value in a given number of bits.
    //If it cannot be represented within the bit range, returns -1.
    //If the value is not negative, returns the number itself.
    public int shortenNegative(int value, int bitCount){
        if (value >= 0) { return value; }
        else if (Math.pow(2, bitCount - 1) < (-value)) { return -1; }
        else { return ((int) Math.pow(2, bitCount) - (-value)); } //To make it easier to understand.
    }

    //Takes an immediate operand and turns it into an integer.
    //If no prefix exists the NumberFormatException must be handled.
    public int parseImmediateToInt(String lastOperand){
        String immediateString = lastOperand.substring(1, Math.min(PREFIX_END_INDEX, lastOperand.length()));
        int immediateValue;

        switch (immediateString) {
            case BINARY_PREFIX ->
                    immediateValue =
                            Integer.parseInt(lastOperand.substring(PREFIX_END_INDEX), 2);
            case HEXADECIMAL_PREFIX ->
                    immediateValue =
                            Integer.parseInt(lastOperand.substring(PREFIX_END_INDEX), 16);
            default -> immediateValue = Integer.parseInt(lastOperand.substring(DELIMITER_END_INDEX));
        }

        return immediateValue;
    }

    public boolean isInRange(int immediateValue, int immediateMaxBits, boolean negativeAllowed){
        if (immediateValue >= Math.pow(2, immediateMaxBits) || (!negativeAllowed && (immediateValue < 0))) { return false; }
        else return immediateValue >= 0 || (Math.pow(2, immediateMaxBits - 1) >= (-immediateValue));
    }
}
