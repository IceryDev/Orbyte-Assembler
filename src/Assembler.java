import Funcs.BitPacker;
import Funcs.ImmediateHandler;
import Resources.Register;
import Resources.Instruction;

import java.io.*;

public class Assembler {

    public static final String COMMENT_DELIMITER = "//";
    public static final String IMMEDIATE_DELIMITER = "#";
    public static final String SCRIPT_EXTENSION = ".oas";

    public static final int IMMEDIATE_MAX_BIT_COUNT = 3;

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
                        System.err.printf("Syntax Error: Garbage following label at line %d", lineNo);
                        return;
                    }
                    System.out.println("I am a label");
                } //Check if instruction while removing the last S character if it exists
                else if (Instruction.isInstruction((flagsSet) ? tokens[0].substring(0, tokens[0].length() - 1) : tokens[0])) {
                    Instruction currentInstruction = Instruction.valueOf(
                                    ((flagsSet) ? tokens[0].substring(0, tokens[0].length() - 1) : tokens[0]).toUpperCase());

                    if (!currentInstruction.getHasFlagBit() && flagsSet) {
                        System.err.printf("Syntax Error: Operation %s cannot have a flag setter bit \"S\"",
                                currentInstruction.name());
                        return;
                    }

                    instructionNo++;
                    int operandCount = currentInstruction.getOpCount();
                    if(tokens.length < (operandCount + 1)){
                        System.err.printf("Syntax Error: Invalid number of operands at line %d, "+
                                        "required %d but provided %d", lineNo, (operandCount + 1), tokens.length);
                        return;
                    }
                    else if (tokens.length > (operandCount + 1) &&
                                !tokens[(operandCount + 1)].startsWith(COMMENT_DELIMITER)){
                        System.err.printf("Syntax Error: Garbage following instruction at line %d", lineNo);
                        return;
                    }

                    boolean containsImmediate = tokens[operandCount].startsWith(IMMEDIATE_DELIMITER);
                    if (containsImmediate && !currentInstruction.isImmediateAllowed()){
                        System.err.printf("Syntax Error: The instruction %s at line %d does"+
                                " not allow for an immediate operand", currentInstruction.name(), lineNo);
                        return;
                    }

                    switch (operandCount){
                        case 1: //Branch instruction
                            break;
                        case 2: //Shifts or Memory or Comparison
                            break;
                        case 3: //Operations
                            if (packer.bitBuffer == 0){
                                packer.appendBits(currentInstruction.getOpType(), Instruction.OPTYPE_BIT_COUNT);
                                packer.appendBits(currentInstruction.getOpcode(), Instruction.OPCODE_BIT_COUNT);

                                packer.appendBits((byte) ((flagsSet) ? 0b1 : 0b0), Instruction.FLAG_BIT_COUNT);
                            }

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
                                            immediateHandler.parseImmediateToInt(tokens[operandCount],
                                                                        IMMEDIATE_MAX_BIT_COUNT, false);

                                    if (immediateValue == -1){
                                        System.err.printf("Value Error: Immediate value size at line %d cannot be"+
                                                                            " larger than %d bits nor can be negative",
                                                                                        lineNo, IMMEDIATE_MAX_BIT_COUNT);
                                        return;
                                    }

                                    if(packer.appendBits((byte) immediateValue, Register.REGCODE_BIT_COUNT)){
                                        fileWriter.writeByte(packer.packedBits);
                                    }

                                } catch (NumberFormatException error) {
                                    System.err.printf("Syntax Error: The immediate operand at line %d must be"+
                                                            " either binary, decimal, or hexadecimal", lineNo);
                                    return;
                                }
                            }

                            break;
                        default:
                            break;
                    }
                }
                else{
                    System.err.printf("Syntax Error: Unknown instruction at line %d", lineNo);
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
