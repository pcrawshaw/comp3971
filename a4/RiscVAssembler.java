// A simple RISV-V assembler using a simplified ELF-like binary file format

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RiscVAssembler {
    // Instruction encoding constants
    private static final int OPCODE_LW = 0x03;
    private static final int OPCODE_SW = 0x23;
    private static final int OPCODE_ADDI = 0x13;
    private static final int OPCODE_ADD_SUB = 0x33;
    private static final int OPCODE_BEQ_BLT = 0x63;
    private static final int OPCODE_JAL = 0x6F;

    private List<Integer> textSection = new ArrayList<>(); // List of 32-bit instructions
    private List<Byte> dataSection = new ArrayList<>();    // Raw bytes for .data
    private Map<String, Integer> labels = new HashMap<>(); // Label -> byte offset in .text

    // Assemble an .asm file into an ELF binary
    public void assemble(String asmFilePath, String elfFilePath) throws Exception {

        System.out.println("Assembling " + asmFilePath + " into " + elfFilePath);
        // First Pass: Collect labels and count instructions
        BufferedReader reader = new BufferedReader(new FileReader(asmFilePath));
        String line;
        boolean inTextSection = false;
        int instrCount = 0; // Offset in .text section (in 32-bit words)

        while ((line = reader.readLine()) != null) {
            line = line.trim().split("#")[0].trim(); // Remove comments
            if (line.isEmpty()) continue;

            if (line.equals(".text")) {
                inTextSection = true;
                continue;
            } else if (line.equals(".data")) {
                inTextSection = false;
                continue;
            }

            if (inTextSection) {
                if (line.endsWith(":")) {
                    String label = line.substring(0, line.length() - 1);
                    labels.put(label, instrCount * 4); // Byte offset
                } else {
                    instrCount++; // Count each instruction
                }
            }
        }
        reader.close();

        // Second Pass: Encode instructions and data
        reader = new BufferedReader(new FileReader(asmFilePath));
        inTextSection = false;
        instrCount = 0;

        while ((line = reader.readLine()) != null) {
            line = line.trim().split("#")[0].trim(); // Remove comments
            if (line.isEmpty()) continue;

            if (line.equals(".text")) {
                inTextSection = true;
                continue;
            } else if (line.equals(".data")) {
                inTextSection = false;
                continue;
            }

            if (inTextSection) {
                if (!line.endsWith(":")) { // Skip labels in second pass
                    System.out.println("Reading instruction: " + line);
                    int instr = encodeInstruction(line, instrCount);
                    textSection.add(instr);
                    instrCount++;
                }
            } else {
                if (line.startsWith(".word")) {
                    String valueStr = line.split("\\s+")[1];
                    int value = parseNumber(valueStr);
                    ByteBuffer bb = ByteBuffer.allocate(4).order(java.nio.ByteOrder.LITTLE_ENDIAN);
                    bb.putInt(value);
                    for (byte b : bb.array()) dataSection.add(b);
                }
            }
        }
        reader.close();

        // Print labels
        // TODO: display in order of appearance in the file
        System.out.println("Labels:");
        for (Map.Entry<String, Integer> entry : labels.entrySet()) {
            System.out.println(entry.getKey()+" : "+entry.getValue());
        }

        // Write ELF file
        writeElfFile(elfFilePath);
    }

    // Encode a single assembly instruction into a 32-bit integer
    private int encodeInstruction(String line, int instrCount) {
        String[] parts = line.trim().split("[\\s,()]+");
        String mnemonic = parts[0].toLowerCase();
        int rd, rs1, rs2, imm;

        switch (mnemonic) {
            case "lw": // lw rd, imm(rs1)
                rd = parseReg(parts[1]);
                imm = parseNumber(parts[2]);
                rs1 = parseReg(parts[3]);
                // I-type encoding: imm[11:0] rs1 rd opcode
                return (imm << 20) | (rs1 << 15) | (0x2 << 12) | (rd << 7) | OPCODE_LW;

            case "sw": // sw rs2, imm(rs1)
                rs2 = parseReg(parts[1]);
                imm = parseNumber(parts[2]);
                rs1 = parseReg(parts[3]);
                // S-type encoding: imm[11:5] rs2 rs1 imm[4:0] opcode
                return ((imm >> 5) << 25) | (rs2 << 20) | (rs1 << 15) | (0x2 << 12) | ((imm & 0x1F) << 7) | OPCODE_SW;

            case "add": // add rd, rs1, rs2
                rd = parseReg(parts[1]);
                rs1 = parseReg(parts[2]);
                rs2 = parseReg(parts[3]);
                // R-type encoding: 0x33 rs2 rs1 rd opcode
                return (rs2 << 20) | (rs1 << 15) | (rd << 7) | OPCODE_ADD_SUB;

            case "sub": // sub rd, rs1, rs2
                rd = parseReg(parts[1]);
                rs1 = parseReg(parts[2]);
                rs2 = parseReg(parts[3]);
                // R-type encoding: 0x20 rs2 rs1 rd opcode
                return (0x20 << 25) | (rs2 << 20) | (rs1 << 15) | (rd << 7) | OPCODE_ADD_SUB;

            case "addi": // addi rd, rs1, imm
                rd = parseReg(parts[1]);
                rs1 = parseReg(parts[2]);
                imm = parseNumber(parts[3]);
                // I-type encoding: imm[11:0] rs1 rd opcode
                return (imm << 20) | (rs1 << 15) | (rd << 7) | OPCODE_ADDI;

            case "beq": // beq rs1, rs2, label/imm
                rs1 = parseReg(parts[1]);
                rs2 = parseReg(parts[2]);
                imm = parseOffset(parts[3], instrCount);
                // SB-type encoding: imm[12] imm[10:5] rs2 rs1 0x1 imm[4:1] imm[11] opcode
                return ((imm >> 12) << 31) | (((imm >> 5) & 0x3F) << 25) | (rs2 << 20) |
                       (rs1 << 15) | (((imm >> 1) & 0xF) << 8) | (((imm >> 11) & 0x1) << 7) | OPCODE_BEQ_BLT;

            case "blt": // blt rs1, rs2, label/imm
                rs1 = parseReg(parts[1]);
                rs2 = parseReg(parts[2]);
                imm = parseOffset(parts[3], instrCount);
                // SB-type encoding: imm[12] imm[10:5] rs2 rs1 imm[4:1] imm[11] opcode
                return ((imm >> 12) << 31) | (((imm >> 5) & 0x3F) << 25) | (rs2 << 20) |
                       (rs1 << 15) | (0x4 << 12) | (((imm >> 1) & 0xF) << 8) | (((imm >> 11) & 0x1) << 7) | OPCODE_BEQ_BLT;

            case "bne": // bne rs1, rs2, label/imm
                rs1 = parseReg(parts[1]);
                rs2 = parseReg(parts[2]);
                imm = parseOffset(parts[3], instrCount);
                // SB-type encoding: imm[12] imm[10:5] rs2 rs1 0x1 imm[4:1] imm[11] opcode
                return ((imm >> 12) << 31) | (((imm >> 5) & 0x3F) << 25) | (rs2 << 20) |
                       (rs1 << 15) | (0x1 << 12) | (((imm >> 1) & 0xF) << 8) | (((imm >> 11) & 0x1) << 7) | OPCODE_BEQ_BLT;

            case "jal": // jal rd, offset
                rd = parseReg(parts[1]);
                imm = parseOffset(parts[2], instrCount);
                // UJ-type encoding: imm[20] imm[10:1] imm[11] imm[19:12] rd opcode
                return ((imm >> 20) << 31) | (((imm >> 1) & 0x3FF) << 21) | (((imm >> 11) & 0x1) << 20) |
                (((imm >> 12) & 0xFF) << 12) | (rd << 7) | OPCODE_JAL;
            default:
                throw new IllegalArgumentException("Unknown instruction: " + mnemonic);
        }
    }

    // Parse register name (e.g., "x5" -> 5)
    private int parseReg(String reg) {
        return Integer.parseInt(reg.substring(1)); // Strip "x" and convert to int
    }

    // Parse a number (decimal or hexadecimal with 0x prefix)
    private int parseNumber(String value) {
        if (value.startsWith("0x") || value.startsWith("0X")) {
            return Integer.parseInt(value.substring(2), 16); // Parse hex without "0x"
        }
        return Integer.parseInt(value); // Parse decimal
    }

    // Parse immediate or label offset (in bytes, adjusted for current PC)
    private int parseOffset(String value, int instrCount) {
        try {
            return parseNumber(value); // Direct immediate (decimal or hex)
        } catch (NumberFormatException e) {
            // Label: calculate offset from current position
            Integer target = labels.get(value);
            if (target == null) {
                throw new IllegalArgumentException("Undefined label: " + value);
            }
            int current = instrCount * 4; // Current byte offset
            return target - current - 4;  // Offset relative to next instruction
        }
    }

    // Write the assembled ELF file
    private void writeElfFile(String elfFilePath) throws IOException {
        FileOutputStream fos = new FileOutputStream(elfFilePath);
        ByteBuffer bb = ByteBuffer.allocate(4).order(java.nio.ByteOrder.LITTLE_ENDIAN);

        // Header (32 bytes)
        fos.write(0x7F); fos.write('E'); fos.write('L'); fos.write('F'); // Magic
        int fileSize = 0x20 + (textSection.size() * 4) + dataSection.size();
        bb.putInt(fileSize); fos.write(bb.array()); bb.clear();         // File size
        bb.putInt(0x0040); fos.write(bb.array()); bb.clear();           // Entry point
        bb.putInt(0x20); fos.write(bb.array()); bb.clear();               // .text offset
        bb.putInt(textSection.size() * 4); fos.write(bb.array()); bb.clear(); // .text size
        bb.putInt(0x20 + textSection.size() * 4); fos.write(bb.array()); bb.clear(); // .data offset
        bb.putInt(dataSection.size()); fos.write(bb.array()); bb.clear(); // .data size
        fos.write(new byte[]{0, 0, 0, 0});                             // Reserved

        // .text section
        for (int instr : textSection) {
            System.out.printf("Writing instruction: 0x%08x%n", instr);
            bb.putInt(instr); 
            fos.write(bb.array()); 
            bb.clear();
        }

        // .data section
        for (byte b : dataSection) 
            fos.write(b);

        fos.close();
        System.out.println("ELF file written: " + elfFilePath);
    }

    // Main method to test the assembler
    // TODO: Use command-line arguments for input/output files
    public static void main(String[] args) throws Exception {
        RiscVAssembler assembler = new RiscVAssembler();

        // Assemble arithmetic example
        assembler.assemble("arithmetic.asm", "arithmetic.elf");

        // Assemble bubble sort example
        assembler.assemble("bubble_sort.asm", "bubble_sort.elf");
    }
}