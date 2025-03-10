
.text
    addi x5, x0, 0x0400  # Set x5 to DATA_BASE
    lw x6, 0(x5)         # x6 = a (offset 0 from 0x0400)
    lw x7, 4(x5)         # x7 = b (offset 4)
    lw x8, 8(x5)         # x8 = c (offset 8)
    lw x9, 12(x5)        # x9 = d (offset 12)
    add x10, x6, x7      # x10 = a + b
    add x11, x8, x9      # x11 = c + d
    sub x12, x10, x11    # x12 = (a + b) - (c + d)
    sw x12, 16(x5)       # Store result in f (offset 16)
    jal x0, 0            # Exit
.data
    .word 1              # a
    .word 2              # b
    .word 3              # c
    .word 4              # d
    .word 0              # f (result)