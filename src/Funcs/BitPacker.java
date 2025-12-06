package Funcs;

public class BitPacker {
    public byte packedBits = 0;
    public byte bitBuffer = 0;
    public int bitCount = 0;

    // Returns true if the buffer is full and ready to be written to a file.
    public boolean appendBits(byte bitsToLoad, int numberOfBits){
        if (bitCount + numberOfBits < 8){
            bitBuffer <<= numberOfBits;
            bitBuffer |= bitsToLoad;
            bitCount += numberOfBits;
            return false;
        }
        else{
            int includedBitCount = (8 - bitCount);
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
