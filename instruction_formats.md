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

In binary and hexadecimal: 
```
1000 1011 0001 0101 0000 0010 1000 1001 = 8B150289
```

## On an ARMv8 system:

A very small ARMv8 assembly language program:

```asm
.arch armv8-a
.text

add x9, x20, x21
```

### Assemble a program

The GCC compiler will "assemble" a program for us (it recognizes the .s file 
name suffix):

```
 $ gcc -c r_format_example.s
```

We should now have a file called `r_format_example.o`. This is an *object* file. 
It contains the binary machine language version of the instructions in the
source code file - only a single `ADD` instruction in this example.

We can examine this file with some standard Linux tools.

### objdump

First, use `objdump -d` to disassemble the binary object file. As we might guess,
this reverses the assembly process and gives us a textual assembly
language version of the program.

*Aside: Can we have a "decompiler" (or should it be "discomplier"?) for a high-level language
like C or Java? Could such a program do the same "reversing" operation 
and give us our source code back if we start with a binary object file?*


```
r_format_example.o:     file format elf64-littleaarch64


Disassembly of section .text:

0000000000000000 <.text>:
   0:   8b150289        add     x9, x20, x21
```

The `objdump` utility understands and can display 
information about object files.

Note that the '.text' section means **executable code** (Why? Google it and find out if
you are interested). 

We see the hexadecimal version of the instruction, which is as we expect,
**0x8b150289**.


### hexdump

We can also do a more direct analysis of the object file using the `hexdump`
utility to dump the "raw" bytes of the object file:

First let's note that the file is 688 bytes in length:

```bash
peterc@ubuntu:~/comp3971$ ls -l r_format_example.o
-rw-rw---- 1 peterc peterc 688 Feb  2 19:19 r_format_example.o
```

We'll look at the first 256 bytes of the file:

<pre>
$ hexdump -n 256 -C r_format_example.o

00000000  7f 45 4c 46 02 01 01 00  00 00 00 00 00 00 00 00  |.ELF............|
00000010  01 00 b7 00 01 00 00 00  00 00 00 00 00 00 00 00  |................|
00000020  00 00 00 00 00 00 00 00  f0 00 00 00 00 00 00 00  |................|
00000030  00 00 00 00 40 00 00 00  00 00 40 00 07 00 06 00  |....@.....@.....|
00000040  <b>89 02 15 8b</b> 00 00 00 00  00 00 00 00 00 00 00 00  |................|
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
00000100
</pre>

We can see (on the fifth line of the output) the `ADD` instruction encoded 
as `89 02 15 8b`. Why is it "backwards"?

=== To do on your wown (NOT TO BE HANDED IN)

Repeat the above with I-forat and  D-format instructions.
