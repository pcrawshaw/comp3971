# Instruction formats illustrated

Using the R-format example from class:

```asm
ADD X9, X20, X21
```

We saw that this is encoded as:
```
opcode = 1112
Rm = 21
shamt = 0
Rn = 20
Rd = 9
```

In binary: 
```
1000 1011 0001 0101 0000 0010 1000 1001 = 8B150289
```

## On an ARM system:

a very small ARMv8 assembly language program:

```
        .arch armv8-a
        .text

        add x9, x20, x21
```

The GCC compiler will "assemble" a program for us (it recognizes the .s file anme suffix)

```
 $ gcc -c r_format_example.s
```

We should now have a file called `r_format_example.o`. This is an *object* file. 
It contains the binary machine language version of the instructions in the
source code file - only a single ADD instruction in this example.

We can examine this file with some standard Linux tools.

First, use `objdump -d` to disassemble the binary object file. As the word
suggests, this reverses the assenbly process and gives us a textual assembly
language version of the program.

```
r_format_example.o:     file format elf64-littleaarch64


Disassembly of section .text:

0000000000000000 <.text>:
   0:   8b150289        add     x9, x20, x21
```

The '.text' section means *executable code* (Why? Google it and find out if
you are interested). The `objdump` utility understands and can display 
 information about object files.

We see the hexadecimal version of the instruction, which is (as it should be)
0x8b150289.

We can also do a more direct analysis of the object file using the `hexdump`
utility:

```
$ hexdump -C r_format_example.o

00000000  7f 45 4c 46 02 01 01 00  00 00 00 00 00 00 00 00  |.ELF............|
00000010  01 00 b7 00 01 00 00 00  00 00 00 00 00 00 00 00  |................|
00000020  00 00 00 00 00 00 00 00  f0 00 00 00 00 00 00 00  |................|
00000030  00 00 00 00 40 00 00 00  00 00 40 00 07 00 06 00  |....@.....@.....|
00000040  *89 02 15 8b* 00 00 00 00  00 00 00 00 00 00 00 00  |................|
00000050  00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  |................|
00000060  00 00 00 00 03 00 01 00  00 00 00 00 00 00 00 00  |................|
00000070  00 00 00 00 00 00 00 00  00 00 00 00 03 00 02 00  |................|
00000080  00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  |................|
00000090  00 00 00 00 03 00 03 00  00 00 00 00 00 00 00 00  |................|
000000a0  00 00 00 00 00 00 00 00  01 00 00 00 00 00 01 00  |................|
000000b0  00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  |................|
000000c0  00 24 78 00 00 2e 73 79  6d 74 61 62 00 2e 73 74  |.$x...symtab..st|
000000d0  72 74 61 62 00 2e 73 68  73 74 72 74 61 62 00 2e  |rtab..shstrtab..|
000000e0  74 65 78 74 00 2e 64 61  74 61 00 2e 62 73 73 00  |text..data..bss.|
000000f0  00 00 00 00 00 00 00 00  00 00 00 00 00 00 00 00  |................|
.
.
.
```

