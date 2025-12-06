package Funcs;

// Access packedBits if the method returns true, to store the packed bits
public class BitPacker {
    public final int packSize = 8;
    public byte packedBits = 0;
    public byte bitBuffer = 0;
    public int bitCount = 0;

    // Appends the input bits to the input buffer from the right.
    // When the buffer is full, writes the byte-long bits to the variable packedBits.
    // Returns true if the buffer got full and ready to be written to a file.
    public boolean appendBits(byte bitsToLoad, int numberOfBits){
        if (bitCount + numberOfBits < packSize){
            bitBuffer <<= numberOfBits;
            bitBuffer |= bitsToLoad;
            bitCount += numberOfBits;
            return false;
        }
        else{
            int includedBitCount = (packSize - bitCount);
            byte tempBits = (byte)(bitsToLoad >> (numberOfBits - includedBitCount));
            bitBuffer <<= includedBitCount;
            bitBuffer |= tempBits;
            packedBits = bitBuffer;

            bitBuffer = (byte)(Math.pow(2, (numberOfBits - includedBitCount)) - 1);
            bitBuffer &= bitsToLoad;
            bitCount = numberOfBits - includedBitCount;
            return true;
        }
    }
}
