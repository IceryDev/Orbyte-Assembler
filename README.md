# 🛰️ Orbyte Assembler

### About
A two-pass assembler for a future breadboard-based microprocessor project. Works with files with the ".oas" (Orbyte Assembly) extension. 
See the example.oas file for reference syntax and Instruction.java file to see each instruction's ARM correspondent instruction.

### Properties
- 16-bit instruction set with 24 instructions (Arithmetic, Logic, Shift, Memory, Branching)
- Encoding for 8 8-bit registers R0, R1, R2, ..., R7 and the Program Counter (PC) which is in R7
- Labels and PC offset calculation for branching

### General Instruction Structure
- 3-operand instruction general format: <op> <Rd> <Rn> <Rm>/#<imm3>
- 2-operand instruction general format: <op> <Rd> <Rn>/#<imm6>
- Memory instruction general format (WRM/FRM): <op> <Rd> <Rn>/&<Ra>/#<imm8>
- Label format: <name>:
- Branch instruction format: B<cond> <Label>

Where <op>, <Rd>, <Rn>, <Rm>, <Ra>, <imm3/6/8>, and <cond> signifies the instruction name, destination register, register operand 1, 
register operand 2, register operand containing address, immediate value with size 3/6/8 bits, and the condition for branching.

### Delimiters
- "#": Precedes an immediate operand
- "&": Precedes a register operand to be considered as an address
- "//": Precedes comments (Multi-line comments not supported)
- ":": Succeeds a label name

### Running the Assembler
Make sure that you are in the src directory and run:
```bash
java Assembler.java <.oas file> <output file>
```
You only need to specify the .oas file's extension.
