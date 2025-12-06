import Funcs.BitPacker;
import Resources.Register;
import Resources.Instruction;

import java.io.*;

public class Assembler {

    public static void main(String[] args){
        if (args.length != 2) {
            System.err.println("Error: Invalid number of arguments. Correct format: java Assembler <input_file> <output_file>");
            return;
        }

        //Initialise Orbyte Assembly (OAS) file.
        String inputFilePath = args[0];
        String outputFilePath = args[1] + ".bin";
        String[] fileContents = null;
        BitPacker packer = new BitPacker();

        try (BufferedReader fileReader = new BufferedReader(new FileReader(inputFilePath));
            DataOutputStream fileWriter = new DataOutputStream(new FileOutputStream(outputFilePath))){
            if (!inputFilePath.endsWith(".oas")){
                System.err.println("Error: Invalid file extension, file name must end with .oas");
                return;
            }

            //Read each line
            int lineNo = 1;
            String line;
            while ((line = fileReader.readLine()) != null){

                if (line.isEmpty()) { lineNo++; continue; }
                String[] tokens = line.split(" +");
                boolean flagsSet = tokens[0].toUpperCase().endsWith("S");
                //Check if label
                if (tokens[0].endsWith(":")){
                    if (tokens.length > 1 && !tokens[1].startsWith("//")){
                        System.err.printf("Syntax Error: Garbage following label at line %d", lineNo);
                        return;
                    }
                    System.out.println("I am a label");
                } //Check if instruction while removing the last S character if it exists
                else if (Instruction.isInstruction((flagsSet) ? tokens[0].substring(0, tokens[0].length() - 1) : tokens[0])) {
                    Instruction currentInstruction = Instruction.valueOf(
                                    ((flagsSet) ? tokens[0].substring(0, tokens[0].length() - 1) : tokens[0]).toUpperCase());

                    if(tokens.length < (currentInstruction.getOpCount() + 1)){
                        System.err.printf("Syntax Error: Invalid number of operands at line %d, required %d but provided %d", lineNo,
                                        (currentInstruction.getOpCount() + 1), tokens.length);
                        return;
                    }
                    else if (tokens.length > (currentInstruction.getOpCount() + 1) &&
                                !tokens[(currentInstruction.getOpCount() + 1)].startsWith("//")){
                        System.err.printf("Syntax Error: Garbage following instruction at line %d", lineNo);
                        return;
                    }

                    switch (currentInstruction.getOpCount()){
                        case 1: //Branch instruction
                        case 2: //Shifts or Memory or Comparison
                        case 3: //Operations
                        default:
                            break;

                    }

                }

                lineNo++;

                /*for (String token : tokens){

                    if (!Instruction.isInstruction(token)) {
                        System.err.println("Error: Assembly failed, unknown operation " + token);
                        return;
                    }
                    Instruction temp = Instruction.valueOf(token);

                    if (packer.appendBits(temp.getOpType(), 2)){
                        fileWriter.writeByte(packer.packedBits);
                    }
                    if (packer.appendBits(temp.getOpcode(), 4)){
                        fileWriter.writeByte(packer.packedBits);
                    }
                }*/
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
