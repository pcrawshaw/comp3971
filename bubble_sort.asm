.text
    addi x5, x0, 0x0200
    addi x6, x0, 5
outer_loop:
    addi x7, x6, -1
    beq x7, x0, done
    addi x8, x5, 0
inner_loop:
    lw x9, 0(x8)
    lw x10, 4(x8)
    blt x9, x10, no_swap
    sw x10, 0(x8)
    sw x9, 4(x8)
no_swap:
    addi x8, x8, 4
    addi x7, x7, -1
    bne x7, x0, inner_loop
    jal x0, outer_loop
done:
    jal x0, 0
.data
    .word 5
    .word 2
    .word 4
    .word 1
    .word 3