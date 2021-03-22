	.arch armv8-a
	.file	"f2c.c"
	.text
	.align	2
	.global	f2c
	.type	f2c, %function
f2c:
.LFB0:
	.cfi_startproc
	fcvt	d0, s0
	mov	x0, 4629700416936869888
	fmov	d1, x0
	fsub	d0, d0, d1
	adrp	x0, .LC0
	ldr	d1, [x0, #:lo12:.LC0]
	fmul	d0, d0, d1
	fcvt	s0, d0
	ret
	.cfi_endproc
.LFE0:
	.size	f2c, .-f2c
	.section	.rodata.cst8,"aM",@progbits,8
	.align	3
.LC0:
	.word	1908874354
	.word	1071761180
	.ident	"GCC: (Ubuntu 9.3.0-17ubuntu1~20.04) 9.3.0"
	.section	.note.GNU-stack,"",@progbits
