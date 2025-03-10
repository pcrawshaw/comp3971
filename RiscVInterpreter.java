// A simple RISV-V interpreter

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

public class RiscVInterpreter {
    private int[] registers = new int[32]; // 32 RISC-V registers (x0-x31), x0 is always 0
    private byte[] memory = new byte[1 << 20]; // 1MB of memory (byte-addressable)
    private int pc; // Program counter, points to the next instruction

    // Constants for memory layout
    private static final int TEXT_BASE = 0x0040; // Base address for .text section (entry point)
    private static final int DATA_BASE = 0x0400; // Base address for .data section

    // Load the simplified ELF file into memory
    // TODO: Add some error handling
    public void loadElf(String filePath) throws Exception {
        RandomAccessFile file = new RandomAccessFile(filePath, "r");

        // Read the 32-byte header
        byte[] header = new byte[32];
        file.readFully(header);
        ByteBuffer buf = ByteBuffer.wrap(header).order(java.nio.ByteOrder.LITTLE_ENDIAN);

        // Verify magic number (0x7F "ELF")
        int magic = buf.getInt(0);
        if (magic != 0x464C457F) {
            file.close();
            throw new Exception("Not a valid ELF file");
        }

        // Extract header fields
        int fileSize = buf.getInt(4);      // Total file size
        pc = buf.getInt(8);                // Entry point (initial PC)
        int textOffset = buf.getInt(12);   // Offset to .text section
        int textSize = buf.getInt(16);     // Size of .text section
        int dataOffset = buf.getInt(20);   // Offset to .data section
        int dataSize = buf.getInt(24);     // Size of .data section

        // Load .text section (instructions) into memory
        file.seek(textOffset);
        file.read(memory, TEXT_BASE, textSize);

        // Load .data section (global data) into memory
        file.seek(dataOffset);
        file.read(memory, DATA_BASE, dataSize);

        file.close();
        System.out.println("ELF loaded: PC=0x" + Integer.toHexString(pc) +
                ", Text size=" + textSize + ", Data size=" + dataSize);
    }

    // Main execution loop: process instructions one by one
    public void run() {
        while (true) {
            int instr = fetch();              // Stage 1: Get the instruction
            DecodedInstr decoded = decode(instr); // Stage 2: Decode it
            if (decoded == null) {
                System.out.println("Invalid instruction at PC=0x" + Integer.toHexString(pc - 4) + ": 0x" + Integer.toHexString(instr));
                break; // Stop on invalid instruction
            }
            int execResult = execute(decoded);    // Stage 3: Execute the operation
            memoryAccess(decoded, execResult);    // Stage 4: Memory ops (if needed)
            writeBack(decoded, execResult);       // Stage 5: Write results (if needed)
            printTrace(decoded);                  // Show what happened
            // Exit condition: jal x0, 0
            if (decoded.op.equals("jal") && decoded.rd == 0 && decoded.imm == 0) {
                System.out.println("Program exited at PC=0x" + Integer.toHexString(pc - 4));
                break;
            }        
        }
    }

    // Stage 1: Fetch the next 32-bit instruction from memory
    private int fetch() {
        // Read 4 bytes from memory at PC and convert to int (little-endian)
        int instr = ByteBuffer.wrap(memory, pc, 4).order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
        pc += 4; // Move PC to next instruction (4 bytes = 32 bits)
        return instr;
    }

    // Stage 2: Decode the 32-bit instruction into its parts
    private DecodedInstr decode(int instr) {
        int opcode = instr & 0x7F;          // Bits 6-0: Opcode
        int rd = (instr >> 7) & 0x1F;       // Bits 11-7: Destination register
        int funct3 = (instr >> 12) & 0x7;   // Bits 14-12: Function code
        int rs1 = (instr >> 15) & 0x1F;     // Bits 19-15: Source register 1
        int rs2 = (instr >> 20) & 0x1F;     // Bits 24-20: Source register 2
        int imm = 0;                        // Immediate value (varies by instruction)

        switch (opcode) {
            case 0x33: // R-type: Arithmetic (e.g., add)
                int funct7 = (instr >> 25) & 0x7F; // Bits 31-25: Extra function code
                if (funct3 == 0x0 && funct7 == 0x00) {
                    return new DecodedInstr("add", rd, rs1, rs2, 0, false);
                }
                break;

            case 0x13: // I-type: Immediate (e.g., addi)
                imm = (instr >> 20); // Bits 31-20: Immediate (sign-extended)
                if (imm >= 0x800) 
                    imm -= 0x1000; // Sign-extend 12-bit value
                if (funct3 == 0x0) {
                    return new DecodedInstr("addi", rd, rs1, 0, imm, false);
                }
                break;

            case 0x03: // I-type: Load (e.g., lw)
                imm = (instr >> 20); // Bits 31-20: Offset
                if (imm >= 0x800) 
                    imm -= 0x1000; // Sign-extend
                if (funct3 == 0x2) { // funct3=010 for lw
                    return new DecodedInstr("lw", rd, rs1, 0, imm, false);
                }
                break;

            case 0x63: // SB-type: Branch (e.g., beq)
                // SB-type immediate: [12|10:5|4:1|11] (bits rearranged)
                imm = ((instr >> 31) << 12) | (((instr >> 25) & 0x3F) << 5) |
                      (((instr >> 8) & 0xF) << 1) | (((instr >> 7) & 0x1) << 11);
                if (imm >= 0x1000) 
                    imm -= 0x2000; // Sign-extend 13-bit value
                if (funct3 == 0x0) {
                    return new DecodedInstr("beq", 0, rs1, rs2, imm, true);
                }
                break;

            case 0x6F: // UJ-type (jal)
                imm = ((instr >> 31) << 20) | (((instr >> 21) & 0x3FF) << 1) |
                        (((instr >> 20) & 0x1) << 11) | (((instr >> 12) & 0xFF) << 12);
                if (imm >= 0x80000) 
                    imm -= 0x100000;
                return new DecodedInstr("jal", rd, 0, 0, imm, true);

            default:
                return null; // Unknown opcode = invalid instruction
        }
        return null; // Unknown combination
    }

    // Stage 3: Execute the instruction's operation
    private int execute(DecodedInstr instr) {
        switch (instr.op) {
            case "add":
                return registers[instr.rs1] + registers[instr.rs2]; // Add two registers
            case "addi":
                return registers[instr.rs1] + instr.imm; // Add register and immediate
            case "lw":
                // Calculate memory address (rs1 + offset), return it for memory stage
                return registers[instr.rs1] + instr.imm;
            case "beq":
                // Compare registers; update PC if equal (offset in half-words)
                if (registers[instr.rs1] == registers[instr.rs2]) {
                    pc += (instr.imm << 1); // Shift imm left by 1 (2 bytes)
                }
                return 0;
            case "jal":
                int nextPc = pc; // PC already incremented in fetch
                pc += (instr.imm << 1); // Jump (imm in half-words)
                return nextPc; // Return address for rd
            default:
                return 0; // Default case (shouldn’t happen with valid decode)
        }
    }

    // Stage 4: Access memory (for loads and stores)
    private void memoryAccess(DecodedInstr instr, int result) {
        if (instr.op.equals("lw")) {
            // Load a 32-bit word from memory at the calculated address
            registers[instr.rd] = ByteBuffer.wrap(memory, result, 4)
                    .order(java.nio.ByteOrder.LITTLE_ENDIAN).getInt();
        }
        // Add store instructions (e.g., sw) here
    }

    // Stage 5: Write results back to registers
    private void writeBack(DecodedInstr instr, int result) {
        // Only write if rd is not x0 (x0 is always 0) and not a branch
        if (instr.rd != 0 && !instr.op.equals("beq")) {
            if (!instr.op.equals("lw")) { // lw writes in memoryAccess
                registers[instr.rd] = result;
            }
        }
    }

    // Print execution trace for debugging
    private void printTrace(DecodedInstr instr) {
        System.out.printf("PC=0x%x, Instr=%s, rd=x%d, rs1=x%d, rs2=x%d, imm=0x%x, x%d=%d\n",
                pc - 4, instr.op, instr.rd, instr.rs1, instr.rs2, instr.imm,
                instr.rd, registers[instr.rd]);
    }

    // Helper class to hold decoded instruction details
    private static class DecodedInstr {
        String op;              // Instruction name (e.g., "add", "lw")
        int rd, rs1, rs2, imm;  // Register indices and immediate value
        boolean isBranchOrJump; // True for branches/jumps

        DecodedInstr(String op, int rd, int rs1, int rs2, int imm, boolean isBranchOrJump) {
            this.op = op;
            this.rd = rd;
            this.rs1 = rs1;
            this.rs2 = rs2;
            this.imm = imm;
            this.isBranchOrJump = isBranchOrJump;
        }
    }

    private void dumpMemory(int start, int end) {
        System.out.printf("Memory dump from 0x%08x to 0x%08x:\n", start, end);
        for (int i = start; i < end; i += 4) {
            System.out.printf("0x%08x: %02x%02x%02x%02x\n", i,
            memory[i + 3], memory[i + 2], memory[i + 1], memory[i]);
        }
    }

    private void dumpRegisters() {
        System.out.println("Registers (values in hex):");
        for (int i = 0; i < 32; i++) {
            if (i < 10) System.out.print(" ");
            System.out.printf("x%d: %08x   ", i, registers[i]);
            if ((i + 1) % 4 == 0) 
                System.out.println();
        }
    }

    // Entry point: Load ELF and run the interpreter
    // TODO: Use command line arguments
    public static void main(String[] args) throws Exception {
        RiscVInterpreter rv = new RiscVInterpreter();
        rv.loadElf("arithmetic.elf"); // Replace with your ELF file path
        rv.run();
        rv.dumpMemory(0x0200, 0x0220);
        rv.dumpRegisters();
    }
}