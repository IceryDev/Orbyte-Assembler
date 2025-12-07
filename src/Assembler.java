import Funcs.BitPacker;
import Funcs.ImmediateHandler;
import Resources.Register;
import Resources.Instruction;

import java.io.*;

public class Assembler {

    public static final String COMMENT_DELIMITER = "//";
    public static final String IMMEDIATE_DELIMITER = "#";
    public static final String ADDRESS_DELIMITER = "&";
    public static final String SCRIPT_EXTENSION = ".oas";

    public static final int IMMEDIATE_MAX_BIT_COUNT = 3;
    public static final int EXTENDED_IMM_MAX_BIT_COUNT = 6;

    public static void main(String[] args){
        if (args.length != 2) {
            System.err.println("Error: Invalid number of arguments. Correct format: java Assembler <input_file> <output_file>");
            return;
        }

        //Initialise Orbyte Assembly (OAS) file.
        String inputFilePath = args[0];
        String outputFilePath = args[1] + ".bin";
        BitPacker packer = new BitPacker();
        ImmediateHandler immediateHandler = new ImmediateHandler();

        try (BufferedReader fileReader = new BufferedReader(new FileReader(inputFilePath));
            DataOutputStream fileWriter = new DataOutputStream(new FileOutputStream(outputFilePath))){
            if (!inputFilePath.endsWith(SCRIPT_EXTENSION)){
                System.err.println("Error: Invalid file extension, file name must end with " + SCRIPT_EXTENSION);
                return;
            }

            //Read each line
            int lineNo = 1;
            int instructionNo = 0;
            String line;
            while ((line = fileReader.readLine()) != null){

                if (line.isEmpty() || line.startsWith(COMMENT_DELIMITER)) { lineNo++; continue; }
                line = line.trim();
                String[] tokens = line.split(" +");
                boolean flagsSet = tokens[0].toUpperCase().endsWith("S");
                //Check if label
                if (tokens[0].endsWith(":")){
                    if (tokens.length > 1 && !tokens[1].startsWith(COMMENT_DELIMITER)){
                        System.err.printf("Syntax Error: Garbage following label at line %d\n", lineNo);
                        return;
                    }
                    System.out.println("I am a label");
                } //Check if instruction while removing the last S character if it exists
                else if (Instruction.isInstruction((flagsSet) ? tokens[0].substring(0, tokens[0].length() - 1) : tokens[0])) {
                    Instruction currentInstruction = Instruction.valueOf(
                                    ((flagsSet) ? tokens[0].substring(0, tokens[0].length() - 1) : tokens[0]).toUpperCase());

                    if (!currentInstruction.getHasFlagBit() && flagsSet) {
                        System.err.printf("Syntax Error: Operation %s cannot have a flag setter bit \"S\"\n",
                                currentInstruction.name());
                        return;
                    }

                    instructionNo++;
                    int operandCount = currentInstruction.getOpCount();
                    if(tokens.length < (operandCount + 1)){
                        System.err.printf("Syntax Error: Invalid number of operands at line %d, "+
                                        "required %d but provided %d\n", lineNo, (operandCount + 1), tokens.length);
                        return;
                    }
                    else if (tokens.length > (operandCount + 1) &&
                                !tokens[(operandCount + 1)].startsWith(COMMENT_DELIMITER)){
                        System.err.printf("Syntax Error: Garbage following instruction at line %d\n", lineNo);
                        return;
                    }

                    boolean containsImmediate = tokens[operandCount].startsWith(IMMEDIATE_DELIMITER);
                    if (containsImmediate && !currentInstruction.isImmediateAllowed()){
                        System.err.printf("Syntax Error: The instruction %s at line %d does"+
                                " not allow for an immediate operand\n", currentInstruction.name(), lineNo);
                        return;
                    }

                    packer.appendBits(currentInstruction.getOpType(), Instruction.OPTYPE_BIT_COUNT);
                    packer.appendBits(currentInstruction.getOpcode(), Instruction.OPCODE_BIT_COUNT);

                    switch (operandCount){
                        case 1: //Branch instruction
                            break;
                        case 2: //Shifts or Memory or Comparison

                            if (currentInstruction.getOpType() == (byte) 0b10){ //Memory

                                int immediateValue = 0;
                                try{
                                    if (containsImmediate) {
                                        immediateValue = immediateHandler.parseImmediateToInt(tokens[operandCount]);
                                        packer.appendBits((byte) ((immediateValue < 0) ? 0b1 : 0b0), Instruction.FLAG_BIT_COUNT);
                                    }
                                    else packer.appendBits((byte) (0b0), Instruction.FLAG_BIT_COUNT);

                                    packer.appendBits((byte) ((containsImmediate) ? 0b1 : 0b0), Instruction.IMM_FLAG_BIT_COUNT);
                                }
                                catch (NumberFormatException error){
                                    System.err.printf("Syntax Error: The immediate operand at line %d must be"+
                                            " either binary, decimal, or hexadecimal\n", lineNo);
                                    return;
                                }

                                if (!Register.isRegister(tokens[1])){
                                    System.err.printf("Syntax Error: Invalid operand \"%s\" at line %d. Operands other than"+
                                                    " the last operand must be a register mnemonic (i.e. R0 or R1)\n",
                                            tokens[1], lineNo);
                                    return;
                                }

                                if(packer.appendBits(Register.valueOf(tokens[1]).getRegCode(), Register.REGCODE_BIT_COUNT)){
                                    fileWriter.writeByte(packer.packedBits);
                                }

                                if (containsImmediate){
                                    if (!immediateHandler.isInRange(immediateValue,
                                            Register.REGISTER_SIZE, true)){
                                        System.err.printf("Value Error: Immediate value size at line %d cannot be"+
                                                        " larger than %d bits\n",
                                                lineNo, Register.REGISTER_SIZE);
                                        return;
                                    }

                                    if(packer.appendBits((byte) 0b0, packer.packSize - packer.bitCount)){
                                        fileWriter.writeByte(packer.packedBits);
                                    }

                                    //These two together form a Little-Endian storage of a byte, I wanted uniform incrementing
                                    //to the program counter, that is why this is half-word-sized.
                                    //If I decide to make this processor 16-bit later on, this will change.
                                    if(packer.appendBits((byte) 0b0, Register.REGISTER_SIZE)){ //Fill the next byte with zeros
                                        fileWriter.writeByte(packer.packedBits);
                                    }

                                    if(packer.appendBits((byte) immediateHandler.shortenNegative(immediateValue,
                                            Register.REGISTER_SIZE), Register.REGISTER_SIZE)){
                                        fileWriter.writeByte(packer.packedBits);
                                    }
                                }
                                else{
                                    boolean isAddress = tokens[operandCount].startsWith(ADDRESS_DELIMITER);

                                    if (!Register.isRegister(tokens[operandCount].substring((isAddress) ? 1 : 0))){
                                        System.err.printf("Syntax Error: Invalid operand \"%s\" at line %d."+
                                                        " Non-immediate operand must be a register mnemonic (i.e. R0 or R1)\n",
                                                                    tokens[operandCount].substring((isAddress) ? 1 : 0), lineNo);
                                        return;
                                    }

                                    if(packer.appendBits(Register.valueOf(
                                            tokens[operandCount].substring((isAddress) ? 1 : 0)).getRegCode(),
                                                                            Register.REGCODE_BIT_COUNT)){
                                        fileWriter.writeByte(packer.packedBits);
                                    }

                                    if(packer.appendBits((byte) ((isAddress) ? 0b111 : 0b000),
                                                packer.packSize - packer.bitCount)){
                                        fileWriter.writeByte(packer.packedBits);
                                    }
                                }



                            }
                            /*else{

                            }*/


                            break;
                        case 3: //Operations
                            packer.appendBits((byte) ((flagsSet) ? 0b1 : 0b0), Instruction.FLAG_BIT_COUNT);
                            packer.appendBits((byte) ((containsImmediate) ? 0b1 : 0b0), Instruction.IMM_FLAG_BIT_COUNT);

                            for (int operand = 1; (operand < (containsImmediate ? operandCount : operandCount + 1)); operand++){
                                if (!Register.isRegister(tokens[operand])){
                                    System.err.printf("Syntax Error: Invalid operand \"%s\" at line %d. Operands other than"+
                                                                " the last operand must be a register mnemonic (i.e. R0 or R1)",
                                                                                                        tokens[operand], lineNo);
                                    return;
                                }

                                if(packer.appendBits(Register.valueOf(tokens[operand].toUpperCase()).getRegCode(),
                                                                                    Register.REGCODE_BIT_COUNT)){
                                    fileWriter.writeByte(packer.packedBits);
                                }
                            }

                            if (containsImmediate) {
                                try{
                                    int immediateValue =
                                            immediateHandler.parseImmediateToInt(tokens[operandCount]);

                                    if (!immediateHandler.isInRange(immediateValue,
                                            IMMEDIATE_MAX_BIT_COUNT, false)){
                                        System.err.printf("Value Error: Immediate value size at line %d cannot be"+
                                                                            " larger than %d bits nor can be negative\n",
                                                                                        lineNo, IMMEDIATE_MAX_BIT_COUNT);
                                        return;
                                    }

                                    if(packer.appendBits((byte) immediateHandler.shortenNegative(immediateValue,
                                                            IMMEDIATE_MAX_BIT_COUNT), Register.REGCODE_BIT_COUNT)){
                                        fileWriter.writeByte(packer.packedBits);
                                    }

                                } catch (NumberFormatException error) {
                                    System.err.printf("Syntax Error: The immediate operand at line %d must be"+
                                                            " either binary, decimal, or hexadecimal\n", lineNo);
                                    return;
                                }
                            }

                            break;
                        default:
                            break;
                    }
                }
                else{
                    System.err.printf("Syntax Error: Unknown instruction at line %d\n", lineNo);
                }

                lineNo++;
            }

        }
        catch (FileNotFoundException error){
            System.err.printf("Error: File %s not found.\n", inputFilePath);
            return;

        }
        catch (IOException error){
            System.err.println("Error: An error occurred while reading file: " + error.getMessage());
            return;
        }

    }



}
