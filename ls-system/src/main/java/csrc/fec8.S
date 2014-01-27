	.file	"fec.c"
	.text
	.type	addmul1, @function
addmul1:
.LFB64:
	.cfi_startproc
	movslq	%ecx, %rcx
	addq	%rdi, %rcx
	movzbl	%dl, %eax
	salq	$8, %rax
	leaq	gf_mul_table(%rip), %r8
	addq	%r8, %rax
	cmpq	%rcx, %rdi
	jae	.L1
	movq	%rdi, %rdx
	notq	%rdx
	addq	%rcx, %rdx
	andl	$7, %edx
	movzbl	(%rsi), %r9d
	movzbl	(%rax,%r9), %r10d
	xorb	%r10b, (%rdi)
	addq	$1, %rdi
	addq	$1, %rsi
	cmpq	%rcx, %rdi
	jne	.L31
	rep ret
.L3:
	movzbl	(%rsi), %r10d
	movzbl	(%rax,%r10), %r11d
	xorb	%r11b, (%rdi)
	addq	$1, %rdi
	leaq	1(%rsi), %rdx
	movzbl	1(%rsi), %esi
	movzbl	(%rax,%rsi), %r9d
	xorb	%r9b, (%rdi)
	leaq	1(%rdi), %r10
	movzbl	1(%rdx), %r11d
	movzbl	(%rax,%r11), %r8d
	xorb	%r8b, (%r10)
	leaq	2(%rdi), %rsi
	movzbl	2(%rdx), %r9d
	movzbl	(%rax,%r9), %r10d
	xorb	%r10b, (%rsi)
	leaq	3(%rdi), %r11
	movzbl	3(%rdx), %r8d
	movzbl	(%rax,%r8), %esi
	xorb	%sil, (%r11)
	leaq	4(%rdi), %r9
	movzbl	4(%rdx), %r10d
	movzbl	(%rax,%r10), %r11d
	xorb	%r11b, (%r9)
	leaq	5(%rdi), %rsi
	movzbl	5(%rdx), %r8d
	movzbl	(%rax,%r8), %r9d
	xorb	%r9b, (%rsi)
	leaq	6(%rdi), %r10
	movzbl	6(%rdx), %r11d
	movzbl	(%rax,%r11), %esi
	xorb	%sil, (%r10)
	addq	$7, %rdi
	leaq	7(%rdx), %rsi
	cmpq	%rcx, %rdi
	jne	.L3
.L1:
	rep ret
.L31:
	testq	%rdx, %rdx
	je	.L3
	cmpq	$1, %rdx
	.p2align 4,,4
	je	.L25
	cmpq	$2, %rdx
	je	.L26
	cmpq	$3, %rdx
	je	.L27
	cmpq	$4, %rdx
	je	.L28
	cmpq	$5, %rdx
	je	.L29
	cmpq	$6, %rdx
	je	.L30
	movzbl	(%rsi), %r11d
	movzbl	(%rax,%r11), %r8d
	xorb	%r8b, (%rdi)
	addq	$1, %rdi
	addq	$1, %rsi
.L30:
	movzbl	(%rsi), %edx
	movzbl	(%rax,%rdx), %r9d
	xorb	%r9b, (%rdi)
	addq	$1, %rdi
	addq	$1, %rsi
.L29:
	movzbl	(%rsi), %r10d
	movzbl	(%rax,%r10), %r11d
	xorb	%r11b, (%rdi)
	addq	$1, %rdi
	addq	$1, %rsi
.L28:
	movzbl	(%rsi), %r8d
	movzbl	(%rax,%r8), %edx
	xorb	%dl, (%rdi)
	addq	$1, %rdi
	addq	$1, %rsi
.L27:
	movzbl	(%rsi), %r9d
	movzbl	(%rax,%r9), %r10d
	xorb	%r10b, (%rdi)
	addq	$1, %rdi
	addq	$1, %rsi
.L26:
	movzbl	(%rsi), %r11d
	movzbl	(%rax,%r11), %r8d
	xorb	%r8b, (%rdi)
	addq	$1, %rdi
	addq	$1, %rsi
.L25:
	movzbl	(%rsi), %edx
	movzbl	(%rax,%rdx), %r9d
	xorb	%r9b, (%rdi)
	addq	$1, %rdi
	addq	$1, %rsi
	cmpq	%rcx, %rdi
	jne	.L3
	rep ret
	.cfi_endproc
.LFE64:
	.size	addmul1, .-addmul1
	.section	.rodata.str1.8,"aMS",@progbits,1
	.align 8
.LC0:
	.string	"-- malloc failure allocating %s\n"
	.text
	.type	my_malloc, @function
my_malloc:
.LFB62:
	.cfi_startproc
	pushq	%rbx
	.cfi_def_cfa_offset 16
	.cfi_offset 3, -16
	movq	%rsi, %rbx
	movslq	%edi, %rdi
	call	malloc@PLT
	testq	%rax, %rax
	jne	.L33
	movq	%rbx, %rcx
	leaq	.LC0(%rip), %rdx
	movl	$1, %esi
	movq	stderr@GOTPCREL(%rip), %rax
	movq	(%rax), %rdi
	movl	$0, %eax
	call	__fprintf_chk@PLT
	movl	$1, %edi
	call	exit@PLT
.L33:
	popq	%rbx
	.cfi_def_cfa_offset 8
	ret
	.cfi_endproc
.LFE62:
	.size	my_malloc, .-my_malloc
	.section	.rodata.str1.1,"aMS",@progbits,1
.LC1:
	.string	" ## __LINE__ ## "
	.text
	.globl	invert_vdm
	.type	invert_vdm, @function
invert_vdm:
.LFB67:
	.cfi_startproc
	pushq	%r15
	.cfi_def_cfa_offset 16
	.cfi_offset 15, -16
	pushq	%r14
	.cfi_def_cfa_offset 24
	.cfi_offset 14, -24
	pushq	%r13
	.cfi_def_cfa_offset 32
	.cfi_offset 13, -32
	pushq	%r12
	.cfi_def_cfa_offset 40
	.cfi_offset 12, -40
	pushq	%rbp
	.cfi_def_cfa_offset 48
	.cfi_offset 6, -48
	pushq	%rbx
	.cfi_def_cfa_offset 56
	.cfi_offset 3, -56
	subq	$24, %rsp
	.cfi_def_cfa_offset 80
	movl	%esi, %r13d
	cmpl	$1, %esi
	je	.L52
	movq	%rdi, %r12
	leaq	.LC1(%rip), %rsi
	movl	%r13d, %edi
	call	my_malloc
	movq	%rax, %rbx
	leaq	.LC1(%rip), %rsi
	movl	%r13d, %edi
	call	my_malloc
	movq	%rax, %rbp
	leaq	.LC1(%rip), %rsi
	movl	%r13d, %edi
	call	my_malloc
	movq	%rax, %r14
	testl	%r13d, %r13d
	jle	.L37
	movl	$0, %eax
	leal	-1(%r13), %ecx
	andl	$7, %ecx
	movb	$0, (%rbx,%rax)
	movzbl	1(%r12), %edx
	movb	%dl, (%r14,%rax)
	leal	1(%r13), %edi
	addq	$1, %rax
	cmpl	%eax, %r13d
	jg	.L107
	jmp	.L37
.L38:
	movb	$0, (%rbx,%rax)
	movslq	%edi, %rcx
	movzbl	(%r12,%rcx), %edx
	movb	%dl, (%r14,%rax)
	addl	%r13d, %edi
	leaq	1(%rax), %r8
	movb	$0, 1(%rbx,%rax)
	movslq	%edi, %rsi
	movzbl	(%r12,%rsi), %r9d
	movb	%r9b, 1(%r14,%rax)
	addl	%r13d, %edi
	movb	$0, 2(%rbx,%rax)
	movslq	%edi, %rax
	movzbl	(%r12,%rax), %r10d
	movb	%r10b, 1(%r14,%r8)
	addl	%r13d, %edi
	movb	$0, 2(%rbx,%r8)
	movslq	%edi, %r11
	movzbl	(%r12,%r11), %r15d
	movb	%r15b, 2(%r14,%r8)
	addl	%r13d, %edi
	movb	$0, 3(%rbx,%r8)
	movslq	%edi, %rcx
	movzbl	(%r12,%rcx), %edx
	movb	%dl, 3(%r14,%r8)
	addl	%r13d, %edi
	movb	$0, 4(%rbx,%r8)
	movslq	%edi, %rsi
	movzbl	(%r12,%rsi), %r9d
	movb	%r9b, 4(%r14,%r8)
	addl	%r13d, %edi
	movb	$0, 5(%rbx,%r8)
	movslq	%edi, %rax
	movzbl	(%r12,%rax), %r10d
	movb	%r10b, 5(%r14,%r8)
	addl	%r13d, %edi
	movb	$0, 6(%rbx,%r8)
	movslq	%edi, %r11
	movzbl	(%r12,%r11), %r15d
	movb	%r15b, 6(%r14,%r8)
	addl	%r13d, %edi
	leaq	7(%r8), %rax
	cmpl	%eax, %r13d
	jg	.L38
.L37:
	movslq	%r13d, %rdi
	subq	$1, %rdi
	movq	%rdi, (%rsp)
	addq	%rbx, %rdi
	movzbl	(%r14), %ecx
	movb	%cl, (%rdi)
	cmpl	$1, %r13d
	jg	.L39
	jmp	.L40
.L43:
	movzbl	(%r10), %r9d
	movl	%esi, %edi
	cmpl	%esi, %ecx
	jle	.L41
	movzbl	%r9b, %edx
	salq	$8, %rdx
	movl	%esi, %r11d
	subl	%ecx, %r11d
	notl	%r11d
	andl	$3, %r11d
	movslq	%esi, %r8
	movzbl	1(%rbx,%r8), %r15d
	addq	%rdx, %r15
	movzbl	(%rax,%r15), %r15d
	xorb	%r15b, (%rbx,%r8)
	addl	$1, %edi
	cmpl	%edi, %ecx
	jne	.L108
	jmp	.L41
.L42:
	movslq	%edi, %r8
	movzbl	1(%rbx,%r8), %r11d
	addq	%rdx, %r11
	movzbl	(%rax,%r11), %r15d
	xorb	%r15b, (%rbx,%r8)
	addl	$1, %edi
	movslq	%edi, %r15
	movzbl	1(%rbx,%r15), %r11d
	addq	%rdx, %r11
	movzbl	(%rax,%r11), %r8d
	xorb	%r8b, (%rbx,%r15)
	leal	1(%rdi), %r15d
	movslq	%r15d, %r8
	movzbl	1(%rbx,%r8), %r11d
	addq	%rdx, %r11
	movzbl	(%rax,%r11), %r15d
	xorb	%r15b, (%rbx,%r8)
	leal	2(%rdi), %r8d
	movslq	%r8d, %r15
	movzbl	1(%rbx,%r15), %r11d
	addq	%rdx, %r11
	movzbl	(%rax,%r11), %r8d
	xorb	%r8b, (%rbx,%r15)
	addl	$3, %edi
	cmpl	%edi, %ecx
	jne	.L42
.L41:
	addq	$1, %r10
	subl	$1, %esi
.L50:
	xorb	%r9b, 0(%rbp)
	testl	%esi, %esi
	jne	.L43
	movq	8(%rsp), %rbp
.L40:
	testl	%r13d, %r13d
	jle	.L44
	movl	$0, %r8d
	leal	-2(%r13), %r9d
	leaq	gf_mul_table(%rip), %rax
	movl	%r13d, %edi
	movq	(%rsp), %r10
.L48:
	movzbl	(%r14,%r8), %r11d
	movb	$1, 0(%rbp,%r10)
	movl	%r9d, %edx
	testl	%r9d, %r9d
	js	.L51
	movzbl	%r11b, %ecx
	salq	$8, %rcx
	movl	%r9d, %esi
	andl	$1, %esi
	movslq	%r9d, %r11
	movzbl	1(%rbp,%r11), %r15d
	addq	%rcx, %r15
	movzbl	(%rax,%r15), %r15d
	xorb	1(%rbx,%r11), %r15b
	movb	%r15b, 0(%rbp,%r11)
	xorb	1(%rax,%rcx), %r15b
	subl	$1, %edx
	cmpl	$-1, %edx
	jne	.L109
	jmp	.L45
.L46:
	movslq	%edx, %rsi
	movzbl	1(%rbp,%rsi), %r11d
	addq	%rcx, %r11
	movzbl	(%rax,%r11), %r11d
	xorb	1(%rbx,%rsi), %r11b
	movb	%r11b, 0(%rbp,%rsi)
	movzbl	%r15b, %r15d
	addq	%rcx, %r15
	xorb	(%rax,%r15), %r11b
	subl	$1, %edx
	movslq	%edx, %rsi
	movzbl	1(%rbp,%rsi), %r15d
	addq	%rcx, %r15
	movzbl	(%rax,%r15), %r15d
	xorb	1(%rbx,%rsi), %r15b
	movb	%r15b, 0(%rbp,%rsi)
	movzbl	%r11b, %r11d
	addq	%rcx, %r11
	xorb	(%rax,%r11), %r15b
	subl	$1, %edx
	cmpl	$-1, %edx
	jne	.L46
	jmp	.L45
.L49:
	movzbl	0(%rbp,%rdx), %r15d
	addq	%rsi, %r15
	movzbl	(%rax,%r15), %r15d
	movslq	%ecx, %r11
	movb	%r15b, (%r12,%r11)
	leaq	1(%rdx), %r11
	addl	%edi, %ecx
	movzbl	1(%rbp,%rdx), %edx
	addq	%rsi, %rdx
	movzbl	(%rax,%rdx), %r15d
	movslq	%ecx, %rdx
	movb	%r15b, (%r12,%rdx)
	addl	%edi, %ecx
	movzbl	1(%rbp,%r11), %r15d
	addq	%rsi, %r15
	movzbl	(%rax,%r15), %r15d
	movslq	%ecx, %rdx
	movb	%r15b, (%r12,%rdx)
	addl	%edi, %ecx
	movzbl	2(%rbp,%r11), %r15d
	addq	%rsi, %r15
	movzbl	(%rax,%r15), %r15d
	movslq	%ecx, %rdx
	movb	%r15b, (%r12,%rdx)
	leaq	3(%r11), %rdx
	addl	%edi, %ecx
	cmpl	%edx, %r13d
	jg	.L49
.L110:
	addq	$1, %r8
	cmpl	%r8d, %r13d
	jg	.L48
.L44:
	movq	%rbx, %rdi
	call	free@PLT
	movq	%rbp, %rdi
	call	free@PLT
	movq	%r14, %rdi
	call	free@PLT
	jmp	.L52
.L51:
	movl	$1, %r15d
.L45:
	movzbl	%r15b, %esi
	movl	%r8d, %ecx
	leaq	inverse(%rip), %rdx
	movzbl	(%rdx,%rsi), %esi
	salq	$8, %rsi
	leal	-1(%r13), %r11d
	andl	$3, %r11d
	movzbl	0(%rbp), %r15d
	addq	%rsi, %r15
	movzbl	(%rax,%r15), %r15d
	movslq	%r8d, %rdx
	movb	%r15b, (%r12,%rdx)
	movl	$1, %edx
	addl	%edi, %ecx
	cmpl	%edx, %r13d
	jg	.L96
	jmp	.L110
.L39:
	movzbl	1(%r14), %r9d
	leaq	2(%r14), %r10
	leal	-2(%r13), %esi
	leal	-1(%r13), %ecx
	leaq	gf_mul_table(%rip), %rax
	movq	%rbp, 8(%rsp)
	movq	%rdi, %rbp
	jmp	.L50
.L52:
	movl	$0, %eax
	addq	$24, %rsp
	.cfi_remember_state
	.cfi_def_cfa_offset 56
	popq	%rbx
	.cfi_def_cfa_offset 48
	popq	%rbp
	.cfi_def_cfa_offset 40
	popq	%r12
	.cfi_def_cfa_offset 32
	popq	%r13
	.cfi_def_cfa_offset 24
	popq	%r14
	.cfi_def_cfa_offset 16
	popq	%r15
	.cfi_def_cfa_offset 8
	ret
.L96:
	.cfi_restore_state
	testl	%r11d, %r11d
	je	.L49
	cmpl	$1, %r11d
	je	.L97
	cmpl	$2, %r11d
	je	.L98
	movzbl	1(%rbp), %r11d
	addq	%rsi, %r11
	movzbl	(%rax,%r11), %r15d
	movslq	%ecx, %r11
	movb	%r15b, (%r12,%r11)
	addq	$1, %rdx
	addl	%edi, %ecx
.L98:
	movzbl	0(%rbp,%rdx), %r15d
	addq	%rsi, %r15
	movzbl	(%rax,%r15), %r15d
	movslq	%ecx, %r11
	movb	%r15b, (%r12,%r11)
	addq	$1, %rdx
	addl	%edi, %ecx
.L97:
	movzbl	0(%rbp,%rdx), %r15d
	addq	%rsi, %r15
	movzbl	(%rax,%r15), %r15d
	movslq	%ecx, %r11
	movb	%r15b, (%r12,%r11)
	addq	$1, %rdx
	addl	%edi, %ecx
	cmpl	%edx, %r13d
	jg	.L49
	jmp	.L110
.L109:
	testl	%esi, %esi
	je	.L46
	movslq	%edx, %rsi
	movzbl	1(%rbp,%rsi), %r11d
	addq	%rcx, %r11
	movzbl	(%rax,%r11), %r11d
	xorb	1(%rbx,%rsi), %r11b
	movb	%r11b, 0(%rbp,%rsi)
	movzbl	%r15b, %r15d
	addq	%rcx, %r15
	xorb	(%rax,%r15), %r11b
	movl	%r11d, %r15d
	subl	$1, %edx
	cmpl	$-1, %edx
	jne	.L46
	jmp	.L45
.L108:
	testl	%r11d, %r11d
	je	.L42
	cmpl	$1, %r11d
	.p2align 4,,4
	je	.L99
	cmpl	$2, %r11d
	je	.L100
	movslq	%edi, %r8
	movzbl	1(%rbx,%r8), %r11d
	addq	%rdx, %r11
	movzbl	(%rax,%r11), %r15d
	xorb	%r15b, (%rbx,%r8)
	addl	$1, %edi
.L100:
	movslq	%edi, %r8
	movzbl	1(%rbx,%r8), %r11d
	addq	%rdx, %r11
	movzbl	(%rax,%r11), %r15d
	xorb	%r15b, (%rbx,%r8)
	addl	$1, %edi
.L99:
	movslq	%edi, %r8
	movzbl	1(%rbx,%r8), %r11d
	addq	%rdx, %r11
	movzbl	(%rax,%r11), %r15d
	xorb	%r15b, (%rbx,%r8)
	addl	$1, %edi
	cmpl	%edi, %ecx
	jne	.L42
	jmp	.L41
.L107:
	testl	%ecx, %ecx
	je	.L38
	cmpl	$1, %ecx
	.p2align 4,,6
	je	.L101
	cmpl	$2, %ecx
	.p2align 4,,3
	je	.L102
	cmpl	$3, %ecx
	.p2align 4,,2
	je	.L103
	cmpl	$4, %ecx
	.p2align 4,,2
	je	.L104
	cmpl	$5, %ecx
	.p2align 4,,2
	je	.L105
	cmpl	$6, %ecx
	.p2align 4,,2
	je	.L106
	movb	$0, (%rbx,%rax)
	movslq	%edi, %rsi
	movzbl	(%r12,%rsi), %r8d
	movb	%r8b, (%r14,%rax)
	addl	%r13d, %edi
	addq	$1, %rax
.L106:
	movb	$0, (%rbx,%rax)
	movslq	%edi, %r9
	movzbl	(%r12,%r9), %r10d
	movb	%r10b, (%r14,%rax)
	addl	%r13d, %edi
	addq	$1, %rax
.L105:
	movb	$0, (%rbx,%rax)
	movslq	%edi, %r11
	movzbl	(%r12,%r11), %r15d
	movb	%r15b, (%r14,%rax)
	addl	%r13d, %edi
	addq	$1, %rax
.L104:
	movb	$0, (%rbx,%rax)
	movslq	%edi, %rcx
	movzbl	(%r12,%rcx), %edx
	movb	%dl, (%r14,%rax)
	addl	%r13d, %edi
	addq	$1, %rax
.L103:
	movb	$0, (%rbx,%rax)
	movslq	%edi, %rsi
	movzbl	(%r12,%rsi), %r8d
	movb	%r8b, (%r14,%rax)
	addl	%r13d, %edi
	addq	$1, %rax
.L102:
	movb	$0, (%rbx,%rax)
	movslq	%edi, %r9
	movzbl	(%r12,%r9), %r10d
	movb	%r10b, (%r14,%rax)
	addl	%r13d, %edi
	addq	$1, %rax
.L101:
	movb	$0, (%rbx,%rax)
	movslq	%edi, %r11
	movzbl	(%r12,%r11), %r15d
	movb	%r15b, (%r14,%rax)
	addl	%r13d, %edi
	addq	$1, %rax
	cmpl	%eax, %r13d
	jg	.L38
	jmp	.L37
	.cfi_endproc
.LFE67:
	.size	invert_vdm, .-invert_vdm
	.globl	init_fec
	.type	init_fec, @function
init_fec:
.LFB68:
	.cfi_startproc
	movb	$1, gf_exp(%rip)
	movl	$0, 4+gf_log(%rip)
	movb	$2, 1+gf_exp(%rip)
	movl	$1, 8+gf_log(%rip)
	movb	$4, 2+gf_exp(%rip)
	movl	$2, 16+gf_log(%rip)
	movb	$8, 3+gf_exp(%rip)
	movl	$3, 32+gf_log(%rip)
	movb	$16, 4+gf_exp(%rip)
	movl	$4, 64+gf_log(%rip)
	movb	$29, 8+gf_exp(%rip)
	movb	$32, 5+gf_exp(%rip)
	movl	$5, 128+gf_log(%rip)
	movb	$64, 6+gf_exp(%rip)
	movl	$6, 256+gf_log(%rip)
	movb	$-128, 7+gf_exp(%rip)
	movl	$7, 512+gf_log(%rip)
	movl	$8, 116+gf_log(%rip)
	leaq	9+gf_exp(%rip), %rdx
	movl	$9, %ecx
.L120:
	movq	%rdx, %rsi
	movzbl	-1(%rdx), %eax
	cmpb	$-128, %al
	jb	.L117
	addl	%eax, %eax
	xorb	8+gf_exp(%rip), %al
	movb	%al, (%rdx)
	jmp	.L118
.L117:
	addl	%eax, %eax
	movb	%al, (%rdx)
.L118:
	movzbl	(%rsi), %edi
	leaq	gf_log(%rip), %r8
	movl	%ecx, (%r8,%rdi,4)
	addl	$1, %ecx
	movl	%ecx, %r9d
	leaq	1(%rdx), %r10
	movzbl	-1(%r10), %r11d
	cmpb	$-128, %r11b
	jb	.L161
	jmp	.L171
.L160:
	movl	$255, gf_log(%rip)
	leaq	gf_exp(%rip), %rdx
	leaq	255+gf_exp(%rip), %r10
	movzbl	(%rdx), %ecx
	movb	%cl, 255(%rdx)
	addq	$1, %rdx
	movzbl	(%rdx), %r9d
	movb	%r9b, 255(%rdx)
	movzbl	1(%rdx), %r11d
	movb	%r11b, 256(%rdx)
	addq	$2, %rdx
.L122:
	movzbl	(%rdx), %eax
	movb	%al, 255(%rdx)
	leaq	1(%rdx), %rsi
	movzbl	1(%rdx), %edi
	movb	%dil, 255(%rsi)
	movzbl	1(%rsi), %edx
	movb	%dl, 256(%rsi)
	movzbl	2(%rsi), %ecx
	movb	%cl, 257(%rsi)
	movzbl	3(%rsi), %r8d
	movb	%r8b, 258(%rsi)
	movzbl	4(%rsi), %r9d
	movb	%r9b, 259(%rsi)
	movzbl	5(%rsi), %r11d
	movb	%r11b, 260(%rsi)
	movzbl	6(%rsi), %eax
	movb	%al, 261(%rsi)
	movzbl	7(%rsi), %edi
	movb	%dil, 262(%rsi)
	leaq	8(%rsi), %rdx
	cmpq	%r10, %rdx
	jne	.L122
	movb	$0, inverse(%rip)
	movb	$1, 1+inverse(%rip)
	movl	$255, %r10d
	movl	%r10d, %eax
	subl	8+gf_log(%rip), %eax
	cltq
	leaq	gf_exp(%rip), %rsi
	movzbl	(%rsi,%rax), %ecx
	movb	%cl, 2+inverse(%rip)
	movl	$1, %ecx
	leaq	8+gf_log(%rip), %r9
	movl	%r10d, %r11d
	subl	(%r9,%rcx,4), %r11d
	movslq	%r11d, %rdx
	leaq	gf_exp(%rip), %rax
	movzbl	(%rax,%rdx), %r9d
	leaq	2+inverse(%rip), %rsi
	movb	%r9b, (%rsi,%rcx)
	addq	$1, %rcx
.L125:
	leaq	8+gf_log(%rip), %r8
	movl	%r10d, %r11d
	subl	(%r8,%rcx,4), %r11d
	movl	%r11d, %eax
	cltq
	leaq	gf_exp(%rip), %rdi
	movzbl	(%rdi,%rax), %edx
	leaq	2+inverse(%rip), %r9
	movb	%dl, (%r9,%rcx)
	leaq	1(%rcx), %r8
	leaq	8+gf_log(%rip), %rsi
	movl	%r10d, %r11d
	subl	(%rsi,%r8,4), %r11d
	movslq	%r11d, %rdx
	leaq	gf_exp(%rip), %rdi
	movzbl	(%rdi,%rdx), %r9d
	leaq	2+inverse(%rip), %rsi
	movb	%r9b, 1(%rsi,%rcx)
	leaq	8+gf_log(%rip), %rcx
	movl	%r10d, %r11d
	subl	4(%rcx,%r8,4), %r11d
	movslq	%r11d, %rdx
	leaq	gf_exp(%rip), %rdi
	movzbl	(%rdi,%rdx), %r9d
	leaq	2+inverse(%rip), %rsi
	movb	%r9b, 1(%rsi,%r8)
	leaq	8+gf_log(%rip), %rcx
	movl	%r10d, %r11d
	subl	8(%rcx,%r8,4), %r11d
	movslq	%r11d, %rdx
	leaq	gf_exp(%rip), %rdi
	movzbl	(%rdi,%rdx), %r9d
	leaq	2+inverse(%rip), %rsi
	movb	%r9b, 2(%rsi,%r8)
	leaq	8+gf_log(%rip), %rcx
	movl	%r10d, %r11d
	subl	12(%rcx,%r8,4), %r11d
	movslq	%r11d, %rdx
	leaq	gf_exp(%rip), %rdi
	movzbl	(%rdi,%rdx), %r9d
	leaq	2+inverse(%rip), %rsi
	movb	%r9b, 3(%rsi,%r8)
	leaq	8+gf_log(%rip), %rcx
	movl	%r10d, %r11d
	subl	16(%rcx,%r8,4), %r11d
	movslq	%r11d, %rdx
	leaq	gf_exp(%rip), %rdi
	movzbl	(%rdi,%rdx), %r9d
	leaq	2+inverse(%rip), %rsi
	movb	%r9b, 4(%rsi,%r8)
	leaq	5(%r8), %rcx
	cmpq	$254, %rcx
	jne	.L125
	pushq	%rbx
	.cfi_def_cfa_offset 16
	.cfi_offset 3, -16
	leaq	gf_log(%rip), %r8
	leaq	1024+gf_log(%rip), %r9
	leaq	gf_mul_table(%rip), %rdi
	jmp	.L124
.L130:
	leaq	gf_log(%rip), %rax
	movl	%r10d, %ebx
	addl	(%rax,%r11,4), %ebx
	movl	%ebx, %esi
	cmpl	$254, %ebx
	jle	.L126
.L127:
	subl	$255, %esi
	movl	%esi, %edx
	sarl	$8, %edx
	movzbl	%sil, %esi
	addl	%edx, %esi
	cmpl	$254, %esi
	jg	.L127
.L126:
	movzbl	%sil, %ecx
	leaq	gf_exp(%rip), %rax
	movzbl	(%rax,%rcx), %ebx
	movb	%bl, (%rdi,%r11)
	addq	$1, %r11
	cmpq	$256, %r11
	jne	.L130
	addq	$4, %r8
	addq	$256, %rdi
	cmpq	%r9, %r8
	je	.L129
.L124:
	movl	(%r8), %r10d
	movl	$0, %r11d
	jmp	.L130
.L129:
	leaq	gf_mul_table(%rip), %r10
	leaq	65536+gf_mul_table(%rip), %r11
	movq	%r10, %r8
.L132:
	movb	$0, (%r10)
	movb	$0, (%r8)
	leaq	1(%r8), %rdi
	movb	$0, 256(%r10)
	movb	$0, 1(%r8)
	movb	$0, 512(%r10)
	movb	$0, 1(%rdi)
	movb	$0, 768(%r10)
	movb	$0, 2(%rdi)
	movb	$0, 1024(%r10)
	movb	$0, 3(%rdi)
	movb	$0, 1280(%r10)
	movb	$0, 4(%rdi)
	movb	$0, 1536(%r10)
	movb	$0, 5(%rdi)
	movb	$0, 1792(%r10)
	movb	$0, 6(%rdi)
	addq	$2048, %r10
	leaq	7(%rdi), %r8
	cmpq	%r11, %r10
	jne	.L132
	movl	$1, fec_initialized(%rip)
	popq	%rbx
	.cfi_restore 3
	.cfi_def_cfa_offset 8
	ret
.L161:
	addl	%r11d, %r11d
	movb	%r11b, 1(%rdx)
	jmp	.L172
.L171:
	addl	%r11d, %r11d
	xorb	8+gf_exp(%rip), %r11b
	movb	%r11b, 1(%rdx)
.L172:
	movzbl	(%r10), %esi
	leaq	gf_log(%rip), %rdx
	movl	%r9d, (%rdx,%rsi,4)
	leal	1(%rcx), %eax
	movzbl	(%r10), %edi
	cmpb	$-128, %dil
	jae	.L173
	addl	%edi, %edi
	movb	%dil, 1(%r10)
	jmp	.L174
.L173:
	addl	%edi, %edi
	xorb	8+gf_exp(%rip), %dil
	movb	%dil, 1(%r10)
.L174:
	movzbl	1(%r10), %r8d
	leaq	gf_log(%rip), %r9
	movl	%eax, (%r9,%r8,4)
	leal	2(%rcx), %r11d
	movzbl	1(%r10), %esi
	cmpb	$-128, %sil
	jae	.L175
	addl	%esi, %esi
	movb	%sil, 2(%r10)
	jmp	.L176
.L175:
	addl	%esi, %esi
	xorb	8+gf_exp(%rip), %sil
	movb	%sil, 2(%r10)
.L176:
	movzbl	2(%r10), %eax
	leaq	gf_log(%rip), %rdx
	movl	%r11d, (%rdx,%rax,4)
	leal	3(%rcx), %edi
	movzbl	2(%r10), %r8d
	cmpb	$-128, %r8b
	jae	.L177
	addl	%r8d, %r8d
	movb	%r8b, 3(%r10)
	jmp	.L178
.L177:
	addl	%r8d, %r8d
	xorb	8+gf_exp(%rip), %r8b
	movb	%r8b, 3(%r10)
.L178:
	movzbl	3(%r10), %r9d
	leaq	gf_log(%rip), %r11
	movl	%edi, (%r11,%r9,4)
	leal	4(%rcx), %eax
	movzbl	3(%r10), %esi
	cmpb	$-128, %sil
	jae	.L179
	addl	%esi, %esi
	movb	%sil, 4(%r10)
	jmp	.L180
.L179:
	addl	%esi, %esi
	xorb	8+gf_exp(%rip), %sil
	movb	%sil, 4(%r10)
.L180:
	movzbl	4(%r10), %edi
	leaq	gf_log(%rip), %rdx
	movl	%eax, (%rdx,%rdi,4)
	addl	$5, %ecx
	leaq	5(%r10), %rdx
	cmpl	$255, %ecx
	jne	.L120
	jmp	.L160
	.cfi_endproc
.LFE68:
	.size	init_fec, .-init_fec
	.section	.rodata.str1.1
.LC2:
	.string	"bad parameters to fec_free\n"
	.text
	.globl	fec_free
	.type	fec_free, @function
fec_free:
.LFB69:
	.cfi_startproc
	pushq	%rbx
	.cfi_def_cfa_offset 16
	.cfi_offset 3, -16
	movq	%rdi, %rbx
	testq	%rdi, %rdi
	je	.L183
	movq	16(%rdi), %rdi
	movl	8(%rbx), %eax
	xorl	12(%rbx), %eax
	xorl	$-20181524, %eax
	xorq	%rdi, %rax
	cmpq	%rax, (%rbx)
	je	.L184
.L183:
	movq	stderr@GOTPCREL(%rip), %rdx
	movq	(%rdx), %rcx
	movl	$27, %edx
	movl	$1, %esi
	leaq	.LC2(%rip), %rdi
	call	fwrite@PLT
	jmp	.L182
.L184:
	call	free@PLT
	movq	%rbx, %rdi
	.p2align 4,,5
	call	free@PLT
.L182:
	popq	%rbx
	.cfi_def_cfa_offset 8
	.p2align 4,,1
	ret
	.cfi_endproc
.LFE69:
	.size	fec_free, .-fec_free
	.section	.rodata.str1.8
	.align 8
.LC3:
	.string	"Invalid parameters k %d n %d GF_SIZE %d\n"
	.section	.rodata.str1.1
.LC4:
	.string	"new_code"
	.text
	.globl	fec_new
	.type	fec_new, @function
fec_new:
.LFB70:
	.cfi_startproc
	pushq	%r15
	.cfi_def_cfa_offset 16
	.cfi_offset 15, -16
	pushq	%r14
	.cfi_def_cfa_offset 24
	.cfi_offset 14, -24
	pushq	%r13
	.cfi_def_cfa_offset 32
	.cfi_offset 13, -32
	pushq	%r12
	.cfi_def_cfa_offset 40
	.cfi_offset 12, -40
	pushq	%rbp
	.cfi_def_cfa_offset 48
	.cfi_offset 6, -48
	pushq	%rbx
	.cfi_def_cfa_offset 56
	.cfi_offset 3, -56
	subq	$56, %rsp
	.cfi_def_cfa_offset 112
	movl	%edi, %ebp
	movl	%esi, %r15d
	cmpl	$0, fec_initialized(%rip)
	jne	.L188
	movl	$0, %eax
	call	init_fec@PLT
.L188:
	cmpl	$256, %ebp
	jg	.L189
	cmpl	$256, %r15d
	jg	.L189
	cmpl	%r15d, %ebp
	jle	.L190
.L189:
	movl	$255, %r9d
	movl	%r15d, %r8d
	movl	%ebp, %ecx
	leaq	.LC3(%rip), %rdx
	movl	$1, %esi
	movq	stderr@GOTPCREL(%rip), %rbx
	movq	(%rbx), %rdi
	movl	$0, %eax
	call	__fprintf_chk@PLT
	movl	$0, %eax
	jmp	.L191
.L190:
	leaq	.LC4(%rip), %rsi
	movl	$24, %edi
	call	my_malloc
	movq	%rax, %r14
	movq	%rax, 32(%rsp)
	movl	%ebp, 8(%rax)
	movl	%r15d, 12(%rax)
	movl	%r15d, %ebx
	imull	%ebp, %ebx
	leaq	.LC1(%rip), %rsi
	movl	%ebx, %edi
	call	my_malloc
	movq	%rax, 16(%r14)
	movl	%ebp, 24(%rsp)
	movl	%r15d, %edx
	xorl	$-20181524, %edx
	xorl	%ebp, %edx
	xorq	%rdx, %rax
	movq	%rax, (%r14)
	leaq	.LC1(%rip), %rsi
	movl	%ebx, %edi
	call	my_malloc
	movq	%rax, %r12
	movb	$1, (%rax)
	cmpl	$1, %ebp
	jle	.L192
	leaq	1(%rax), %rsi
	leal	-2(%rbp), %ecx
	leaq	2(%rax,%rcx), %r8
	movq	%rsi, %r9
	notq	%r9
	addq	%r8, %r9
	andl	$7, %r9d
	movb	$0, 1(%rax)
	leaq	2(%rax), %r10
	cmpq	%r8, %r10
	jne	.L275
	jmp	.L192
.L193:
	movb	$0, (%r10)
	movb	$0, 1(%r10)
	movb	$0, 2(%r10)
	movb	$0, 3(%r10)
	movb	$0, 4(%r10)
	movb	$0, 5(%r10)
	movb	$0, 6(%r10)
	movb	$0, 7(%r10)
	addq	$8, %r10
	cmpq	%r8, %r10
	jne	.L193
.L192:
	movslq	%ebp, %rbx
	leaq	(%r12,%rbx), %r11
	leal	-1(%r15), %r13d
	testl	%r13d, %r13d
	jle	.L194
	movl	$0, %r10d
	movl	24(%rsp), %r14d
	leal	-1(%r14), %r14d
	addq	$1, %r14
	jmp	.L195
.L198:
	movq	%rdx, %rsi
	movl	%ecx, %eax
	cmpl	$254, %ecx
	jle	.L196
.L197:
	subl	$255, %eax
	movl	%eax, %r9d
	sarl	$8, %r9d
	movzbl	%al, %eax
	addl	%r9d, %eax
	cmpl	$254, %eax
	jg	.L197
.L196:
	addq	$1, %rdx
	addl	%r8d, %ecx
.L200:
	movzbl	%al, %eax
	leaq	gf_exp(%rip), %r9
	movzbl	(%r9,%rax), %eax
	movb	%al, (%rsi)
	cmpq	%rdi, %rdx
	jne	.L198
.L199:
	addl	$1, %r10d
	addq	%rbx, %r11
	cmpl	%r13d, %r10d
	je	.L194
.L195:
	testl	%ebp, %ebp
	jle	.L199
	movl	%r10d, %r8d
	leaq	(%r11,%r14), %rdi
	movl	%r10d, %ecx
	leaq	1(%r11), %rdx
	movq	%r11, %rsi
	movl	$0, %eax
	jmp	.L200
.L194:
	movl	%ebp, %esi
	movq	%r12, %rdi
	call	invert_vdm@PLT
	subl	%ebp, %r15d
	movl	%r15d, 16(%rsp)
	movl	%ebp, %eax
	imull	%ebp, %eax
	cltq
	movq	%rax, 40(%rsp)
	movq	32(%rsp), %r11
	movq	%rax, %r13
	addq	16(%r11), %r13
	addq	%r12, %rax
	movq	%rax, 8(%rsp)
	testl	%r15d, %r15d
	jle	.L201
	movl	%ebp, 20(%rsp)
	movl	$0, %r11d
	movl	$0, %r14d
	movl	24(%rsp), %r15d
	subl	$1, %r15d
	addq	$1, %r15
	movq	%r15, 24(%rsp)
	leaq	gf_mul_table(%rip), %r9
	jmp	.L202
.L207:
	leaq	(%r12,%rdi), %rdx
	movq	%r10, %rax
	movq	%r10, %rsi
	notq	%rsi
	addq	%r8, %rsi
	andl	$3, %esi
	movzbl	(%rdx), %r15d
	movzbl	(%r10), %ecx
	salq	$8, %rcx
	addq	%r15, %rcx
	movzbl	(%r9,%rcx), %ecx
	addq	$1, %rax
	addq	%rbx, %rdx
	cmpq	%r8, %rax
	jne	.L276
	jmp	.L203
.L204:
	movzbl	(%rdx), %r15d
	movzbl	(%rax), %esi
	salq	$8, %rsi
	addq	%r15, %rsi
	xorb	(%r9,%rsi), %cl
	leaq	1(%rax), %rsi
	addq	%rbx, %rdx
	movzbl	(%rdx), %r15d
	movzbl	1(%rax), %eax
	salq	$8, %rax
	addq	%r15, %rax
	xorb	(%r9,%rax), %cl
	addq	%rbx, %rdx
	movzbl	(%rdx), %r15d
	movzbl	1(%rsi), %eax
	salq	$8, %rax
	addq	%r15, %rax
	xorb	(%r9,%rax), %cl
	addq	%rbx, %rdx
	movzbl	(%rdx), %r15d
	movzbl	2(%rsi), %eax
	salq	$8, %rax
	addq	%r15, %rax
	xorb	(%r9,%rax), %cl
	leaq	3(%rsi), %rax
	addq	%rbx, %rdx
	cmpq	%r8, %rax
	jne	.L204
.L203:
	leal	(%r11,%rdi), %eax
	cltq
	movb	%cl, 0(%r13,%rax)
	addq	$1, %rdi
	cmpl	%edi, %ebp
	jg	.L207
.L206:
	addl	$1, %r14d
	addl	20(%rsp), %r11d
	cmpl	16(%rsp), %r14d
	je	.L201
.L202:
	testl	%ebp, %ebp
	jle	.L206
	movslq	%r11d, %r10
	addq	8(%rsp), %r10
	movl	$0, %edi
	movq	24(%rsp), %r8
	leaq	(%r10,%r8), %r8
	jmp	.L207
.L201:
	movq	32(%rsp), %r13
	movq	16(%r13), %rdi
	movq	40(%rsp), %rdx
	movl	$0, %esi
	call	memset@PLT
	movq	16(%r13), %r8
	testl	%ebp, %ebp
	jle	.L208
	addq	$1, %rbx
	leal	-1(%rbp), %ecx
	andl	$7, %ecx
	movb	$1, (%r8)
	movl	$1, %edx
	addq	%rbx, %r8
	cmpl	%ebp, %edx
	jne	.L277
	jmp	.L208
.L209:
	movb	$1, (%r8)
	addl	$1, %edx
	addq	%rbx, %r8
	movb	$1, (%r8)
	addq	%rbx, %r8
	movb	$1, (%r8)
	addq	%rbx, %r8
	movb	$1, (%r8)
	addq	%rbx, %r8
	movb	$1, (%r8)
	addq	%rbx, %r8
	movb	$1, (%r8)
	addq	%rbx, %r8
	movb	$1, (%r8)
	addq	%rbx, %r8
	movb	$1, (%r8)
	addl	$7, %edx
	addq	%rbx, %r8
	cmpl	%ebp, %edx
	jne	.L209
.L208:
	movq	%r12, %rdi
	call	free@PLT
	movq	32(%rsp), %rax
.L191:
	addq	$56, %rsp
	.cfi_remember_state
	.cfi_def_cfa_offset 56
	popq	%rbx
	.cfi_def_cfa_offset 48
	popq	%rbp
	.cfi_def_cfa_offset 40
	popq	%r12
	.cfi_def_cfa_offset 32
	popq	%r13
	.cfi_def_cfa_offset 24
	popq	%r14
	.cfi_def_cfa_offset 16
	popq	%r15
	.cfi_def_cfa_offset 8
	ret
.L277:
	.cfi_restore_state
	testl	%ecx, %ecx
	je	.L209
	cmpl	$1, %ecx
	je	.L261
	cmpl	$2, %ecx
	.p2align 4,,3
	je	.L262
	cmpl	$3, %ecx
	.p2align 4,,2
	je	.L263
	cmpl	$4, %ecx
	.p2align 4,,2
	je	.L264
	cmpl	$5, %ecx
	.p2align 4,,2
	je	.L265
	cmpl	$6, %ecx
	.p2align 4,,2
	je	.L266
	movb	$1, (%r8)
	addl	$1, %edx
	addq	%rbx, %r8
.L266:
	movb	$1, (%r8)
	addl	$1, %edx
	addq	%rbx, %r8
.L265:
	movb	$1, (%r8)
	addl	$1, %edx
	addq	%rbx, %r8
.L264:
	movb	$1, (%r8)
	addl	$1, %edx
	addq	%rbx, %r8
.L263:
	movb	$1, (%r8)
	addl	$1, %edx
	addq	%rbx, %r8
.L262:
	movb	$1, (%r8)
	addl	$1, %edx
	addq	%rbx, %r8
.L261:
	movb	$1, (%r8)
	addl	$1, %edx
	addq	%rbx, %r8
	cmpl	%ebp, %edx
	jne	.L209
	jmp	.L208
.L276:
	testq	%rsi, %rsi
	je	.L204
	cmpq	$1, %rsi
	.p2align 4,,4
	je	.L267
	cmpq	$2, %rsi
	je	.L268
	movzbl	(%rdx), %r15d
	movzbl	1(%r10), %esi
	salq	$8, %rsi
	addq	%r15, %rsi
	xorb	(%r9,%rsi), %cl
	addq	$1, %rax
	addq	%rbx, %rdx
.L268:
	movzbl	(%rdx), %r15d
	movzbl	(%rax), %esi
	salq	$8, %rsi
	addq	%r15, %rsi
	xorb	(%r9,%rsi), %cl
	addq	$1, %rax
	addq	%rbx, %rdx
.L267:
	movzbl	(%rdx), %r15d
	movzbl	(%rax), %esi
	salq	$8, %rsi
	addq	%r15, %rsi
	xorb	(%r9,%rsi), %cl
	addq	$1, %rax
	addq	%rbx, %rdx
	cmpq	%r8, %rax
	jne	.L204
	jmp	.L203
.L275:
	testq	%r9, %r9
	je	.L193
	cmpq	$1, %r9
	.p2align 4,,4
	je	.L269
	cmpq	$2, %r9
	je	.L270
	cmpq	$3, %r9
	je	.L271
	cmpq	$4, %r9
	je	.L272
	cmpq	$5, %r9
	je	.L273
	cmpq	$6, %r9
	je	.L274
	movb	$0, (%r10)
	addq	$1, %r10
.L274:
	movb	$0, (%r10)
	addq	$1, %r10
.L273:
	movb	$0, (%r10)
	addq	$1, %r10
.L272:
	movb	$0, (%r10)
	addq	$1, %r10
.L271:
	movb	$0, (%r10)
	addq	$1, %r10
.L270:
	movb	$0, (%r10)
	addq	$1, %r10
.L269:
	movb	$0, (%r10)
	addq	$1, %r10
	cmpq	%r8, %r10
	jne	.L193
	jmp	.L192
	.cfi_endproc
.LFE70:
	.size	fec_new, .-fec_new
	.section	.rodata.str1.1
.LC5:
	.string	"Invalid index %d (max %d)\n"
	.text
	.globl	fec_encode
	.type	fec_encode, @function
fec_encode:
.LFB71:
	.cfi_startproc
	pushq	%r15
	.cfi_def_cfa_offset 16
	.cfi_offset 15, -16
	pushq	%r14
	.cfi_def_cfa_offset 24
	.cfi_offset 14, -24
	pushq	%r13
	.cfi_def_cfa_offset 32
	.cfi_offset 13, -32
	pushq	%r12
	.cfi_def_cfa_offset 40
	.cfi_offset 12, -40
	pushq	%rbp
	.cfi_def_cfa_offset 48
	.cfi_offset 6, -48
	pushq	%rbx
	.cfi_def_cfa_offset 56
	.cfi_offset 3, -56
	subq	$24, %rsp
	.cfi_def_cfa_offset 80
	movq	%rsi, %r15
	movq	%rdx, 8(%rsp)
	movl	%r8d, %r14d
	movl	8(%rdi), %r13d
	cmpl	%ecx, %r13d
	jle	.L288
	movslq	%r8d, %rdx
	movslq	%ecx, %r14
	movq	(%rsi,%r14,8), %rsi
	movq	8(%rsp), %rdi
	call	memmove@PLT
	jmp	.L287
.L288:
	movl	12(%rdi), %eax
	cmpl	%eax, %ecx
	jge	.L290
	imull	%r13d, %ecx
	movslq	%ecx, %rbp
	addq	16(%rdi), %rbp
	movslq	%r8d, %rdx
	movl	$0, %esi
	movq	8(%rsp), %rdi
	call	memset@PLT
	testl	%r13d, %r13d
	jle	.L287
	movl	$0, %ebx
	leal	-1(%r13), %r12d
	andl	$7, %r12d
	movzbl	0(%rbp,%rbx), %edx
	testb	%dl, %dl
	je	.L294
	jmp	.L335
.L292:
	movzbl	0(%rbp,%rbx), %eax
	testb	%al, %al
	je	.L291
	movzbl	%al, %edx
	movq	(%r15,%rbx,8), %rsi
	movl	%r14d, %ecx
	movq	8(%rsp), %rdi
	call	addmul1
.L291:
	leaq	1(%rbx), %r12
	movzbl	1(%rbp,%rbx), %ebx
	testb	%bl, %bl
	je	.L309
	jmp	.L336
.L290:
	leal	-1(%rax), %r8d
	leaq	.LC5(%rip), %rdx
	movl	$1, %esi
	movq	stderr@GOTPCREL(%rip), %rbx
	movq	(%rbx), %rdi
	movl	$0, %eax
	call	__fprintf_chk@PLT
.L287:
	addq	$24, %rsp
	.cfi_remember_state
	.cfi_def_cfa_offset 56
	popq	%rbx
	.cfi_def_cfa_offset 48
	popq	%rbp
	.cfi_def_cfa_offset 40
	popq	%r12
	.cfi_def_cfa_offset 32
	popq	%r13
	.cfi_def_cfa_offset 24
	popq	%r14
	.cfi_def_cfa_offset 16
	popq	%r15
	.cfi_def_cfa_offset 8
	ret
.L335:
	.cfi_restore_state
	movzbl	%dl, %edx
	movq	(%r15,%rbx,8), %rsi
	movl	%r14d, %ecx
	movq	8(%rsp), %rdi
	call	addmul1
.L294:
	addq	$1, %rbx
	cmpl	%ebx, %r13d
	jle	.L287
	testl	%r12d, %r12d
	je	.L292
	cmpl	$1, %r12d
	je	.L329
	cmpl	$2, %r12d
	je	.L330
	cmpl	$3, %r12d
	je	.L331
	cmpl	$4, %r12d
	je	.L332
	cmpl	$5, %r12d
	je	.L333
	cmpl	$6, %r12d
	je	.L334
	movzbl	0(%rbp,%rbx), %ecx
	testb	%cl, %cl
	je	.L295
	movzbl	%cl, %edx
	movq	(%r15,%rbx,8), %rsi
	movl	%r14d, %ecx
	movq	8(%rsp), %rdi
	call	addmul1
.L295:
	addq	$1, %rbx
.L334:
	movzbl	0(%rbp,%rbx), %esi
	testb	%sil, %sil
	je	.L297
	movzbl	%sil, %edx
	movq	(%r15,%rbx,8), %rsi
	movl	%r14d, %ecx
	movq	8(%rsp), %rdi
	call	addmul1
.L297:
	addq	$1, %rbx
.L333:
	movzbl	0(%rbp,%rbx), %edi
	testb	%dil, %dil
	je	.L299
	movzbl	%dil, %edx
	movq	(%r15,%rbx,8), %rsi
	movl	%r14d, %ecx
	movq	8(%rsp), %rdi
	call	addmul1
.L299:
	addq	$1, %rbx
.L332:
	movzbl	0(%rbp,%rbx), %r8d
	testb	%r8b, %r8b
	je	.L301
	movzbl	%r8b, %edx
	movq	(%r15,%rbx,8), %rsi
	movl	%r14d, %ecx
	movq	8(%rsp), %rdi
	call	addmul1
.L301:
	addq	$1, %rbx
.L331:
	movzbl	0(%rbp,%rbx), %r9d
	testb	%r9b, %r9b
	je	.L303
	movzbl	%r9b, %edx
	movq	(%r15,%rbx,8), %rsi
	movl	%r14d, %ecx
	movq	8(%rsp), %rdi
	call	addmul1
.L303:
	addq	$1, %rbx
.L330:
	movzbl	0(%rbp,%rbx), %r10d
	testb	%r10b, %r10b
	je	.L305
	movzbl	%r10b, %edx
	movq	(%r15,%rbx,8), %rsi
	movl	%r14d, %ecx
	movq	8(%rsp), %rdi
	call	addmul1
.L305:
	addq	$1, %rbx
.L329:
	movzbl	0(%rbp,%rbx), %r11d
	testb	%r11b, %r11b
	je	.L307
	movzbl	%r11b, %edx
	movq	(%r15,%rbx,8), %rsi
	movl	%r14d, %ecx
	movq	8(%rsp), %rdi
	call	addmul1
.L307:
	addq	$1, %rbx
	cmpl	%ebx, %r13d
	jg	.L292
	jmp	.L287
.L336:
	movzbl	%bl, %edx
	movq	(%r15,%r12,8), %rsi
	movl	%r14d, %ecx
	movq	8(%rsp), %rdi
	call	addmul1
.L309:
	leaq	1(%r12), %rcx
	movzbl	1(%rbp,%r12), %edx
	testb	%dl, %dl
	je	.L310
	movzbl	%dl, %edx
	movq	(%r15,%rcx,8), %rsi
	movl	%r14d, %ecx
	movq	8(%rsp), %rdi
	call	addmul1
.L310:
	leaq	2(%r12), %rsi
	movzbl	2(%rbp,%r12), %edi
	testb	%dil, %dil
	je	.L311
	movzbl	%dil, %edx
	movq	(%r15,%rsi,8), %rsi
	movl	%r14d, %ecx
	movq	8(%rsp), %rdi
	call	addmul1
.L311:
	leaq	3(%r12), %r8
	movzbl	3(%rbp,%r12), %r9d
	testb	%r9b, %r9b
	je	.L312
	movzbl	%r9b, %edx
	movq	(%r15,%r8,8), %rsi
	movl	%r14d, %ecx
	movq	8(%rsp), %rdi
	call	addmul1
.L312:
	leaq	4(%r12), %r10
	movzbl	4(%rbp,%r12), %r11d
	testb	%r11b, %r11b
	je	.L313
	movzbl	%r11b, %edx
	movq	(%r15,%r10,8), %rsi
	movl	%r14d, %ecx
	movq	8(%rsp), %rdi
	call	addmul1
.L313:
	leaq	5(%r12), %rax
	movzbl	5(%rbp,%r12), %ebx
	testb	%bl, %bl
	je	.L314
	movzbl	%bl, %edx
	movq	(%r15,%rax,8), %rsi
	movl	%r14d, %ecx
	movq	8(%rsp), %rdi
	call	addmul1
.L314:
	leaq	6(%r12), %rcx
	movzbl	6(%rbp,%r12), %edx
	testb	%dl, %dl
	je	.L315
	movzbl	%dl, %edx
	movq	(%r15,%rcx,8), %rsi
	movl	%r14d, %ecx
	movq	8(%rsp), %rdi
	call	addmul1
.L315:
	leaq	7(%r12), %rbx
	cmpl	%ebx, %r13d
	jg	.L292
	jmp	.L287
	.cfi_endproc
.LFE71:
	.size	fec_encode, .-fec_encode
	.section	.rodata.str1.8
	.align 8
.LC6:
	.string	"decode: invalid index %d (max %d)\n"
	.section	.rodata.str1.1
.LC7:
	.string	"singular matrix\n"
.LC8:
	.string	"XXX pivot not found!\n"
.LC9:
	.string	"singular matrix 2\n"
.LC10:
	.string	"AARGH, indxr[col] %d\n"
.LC11:
	.string	"AARGH, indxc[col] %d\n"
.LC12:
	.string	"new pkt pointers"
.LC13:
	.string	"new pkt buffer"
.LC14:
	.string	"indxc"
.LC15:
	.string	"indxr"
.LC16:
	.string	"ipiv"
	.text
	.globl	fec_decode
	.type	fec_decode, @function
fec_decode:
.LFB74:
	.cfi_startproc
	pushq	%r15
	.cfi_def_cfa_offset 16
	.cfi_offset 15, -16
	pushq	%r14
	.cfi_def_cfa_offset 24
	.cfi_offset 14, -24
	pushq	%r13
	.cfi_def_cfa_offset 32
	.cfi_offset 13, -32
	pushq	%r12
	.cfi_def_cfa_offset 40
	.cfi_offset 12, -40
	pushq	%rbp
	.cfi_def_cfa_offset 48
	.cfi_offset 6, -48
	pushq	%rbx
	.cfi_def_cfa_offset 56
	.cfi_offset 3, -56
	subq	$168, %rsp
	.cfi_def_cfa_offset 224
	movq	%rdi, %rbp
	movq	%rsi, 48(%rsp)
	movq	%rdx, %r14
	movl	%ecx, 20(%rsp)
	movl	8(%rdi), %r15d
	testl	%r15d, %r15d
	jle	.L339
	movl	$0, %ebx
	movq	%rsi, %r10
.L345:
	movslq	%ebx, %rcx
	leaq	(%r14,%rcx,4), %rsi
	movl	(%rsi), %edx
	cmpl	%edx, %r15d
	jle	.L340
	cmpl	%edx, %ebx
	jne	.L341
.L340:
	addl	$1, %ebx
	jmp	.L342
.L341:
	movslq	%edx, %rdi
	leaq	(%r14,%rdi,4), %r8
	movl	(%r8), %r9d
	cmpl	%r9d, %edx
	je	.L394
	movl	%r9d, (%rsi)
	movl	%edx, (%r8)
	leaq	(%r10,%rcx,8), %r11
	movq	(%r11), %r12
	leaq	(%r10,%rdi,8), %r13
	movq	0(%r13), %rax
	movq	%rax, (%r11)
	movq	%r12, 0(%r13)
.L342:
	cmpl	%ebx, %r15d
	jg	.L345
	jmp	.L716
.L711:
	movl	%r15d, 32(%rsp)
	movq	%r13, 24(%rsp)
	movl	8(%rsp), %r13d
	jmp	.L391
.L712:
	movl	%r15d, 32(%rsp)
	movq	%r13, 24(%rsp)
	movl	8(%rsp), %r13d
.L391:
	movl	(%r14,%r12,4), %ecx
	cmpl	%ecx, %r13d
	jle	.L346
	movq	%rbx, %rdx
	movl	$0, %esi
	movq	24(%rsp), %r15
	movq	%r15, %rdi
	call	memset@PLT
	movb	$1, (%r15,%r12)
	jmp	.L347
.L346:
	movl	12(%rbp), %eax
	cmpl	%eax, %ecx
	jge	.L348
	imull	%r13d, %ecx
	movslq	%ecx, %rsi
	addq	16(%rbp), %rsi
	movq	%rbx, %rdx
	movq	24(%rsp), %rdi
	call	memmove@PLT
	jmp	.L347
.L348:
	leal	-1(%rax), %r8d
	leaq	.LC6(%rip), %rdx
	movl	$1, %esi
	movq	stderr@GOTPCREL(%rip), %r14
	movq	(%r14), %rdi
	movl	$0, %eax
	call	__fprintf_chk@PLT
	movq	56(%rsp), %rdi
	call	free@PLT
	jmp	.L349
.L347:
	movq	24(%rsp), %rdx
	addq	%rbx, %rdx
	movq	%rdx, 24(%rsp)
	leaq	1(%r12), %r15
	movl	(%r14,%r15,4), %ecx
	cmpl	%ecx, %r13d
	jle	.L594
	jmp	.L636
.L392:
	movl	$0, 0(%r13,%r8,4)
	addq	$1, %r8
	movl	$0, 0(%r13,%r8,4)
	leaq	1(%r8), %r9
	movl	$0, 0(%r13,%r9,4)
	leaq	2(%r8), %r11
	movl	$0, 0(%r13,%r11,4)
	leaq	3(%r8), %rax
	movl	$0, 0(%r13,%rax,4)
	leaq	4(%r8), %r10
	movl	$0, 0(%r13,%r10,4)
	leaq	5(%r8), %rsi
	movl	$0, 0(%r13,%rsi,4)
	leaq	6(%r8), %rcx
	movl	$0, 0(%r13,%rcx,4)
	addq	$7, %r8
	cmpl	%r8d, 8(%rsp)
	jg	.L392
.L650:
	movl	8(%rsp), %ebx
	addl	$1, %ebx
	movl	%ebx, 144(%rsp)
	movl	$0, 68(%rsp)
	movq	$0, 24(%rsp)
	movl	72(%rsp), %r12d
	subl	$1, %r12d
	addq	$1, %r12
	movq	%r12, 120(%rsp)
	leaq	gf_mul_table(%rip), %rbp
	movl	%r15d, 148(%rsp)
	movq	56(%rsp), %r15
	movq	%r14, 152(%rsp)
.L373:
	movq	24(%rsp), %r14
	movl	%r14d, %ebx
	movl	0(%r13,%r14,4), %r8d
	cmpl	$1, %r8d
	jne	.L353
.L356:
	movl	72(%rsp), %r11d
	movl	$0, %r12d
	movl	$0, %ecx
	movl	8(%rsp), %r9d
	jmp	.L354
.L353:
	movslq	68(%rsp), %rdx
	cmpb	$0, (%r15,%rdx)
	jne	.L355
	jmp	.L356
.L354:
	movl	%ecx, %edi
	cmpl	$1, 0(%r13,%rcx,4)
	je	.L357
	movl	$0, %eax
	leal	-1(%r9), %esi
	andl	$7, %esi
	movl	%eax, %ebx
	movl	0(%r13), %r10d
	testl	%r10d, %r10d
	jne	.L511
	jmp	.L637
.L362:
	movl	%eax, %ebx
	movl	0(%r13,%rax,4), %edx
	testl	%edx, %edx
	jne	.L358
	leal	(%r12,%rax), %esi
	movslq	%esi, %r10
	cmpb	$0, (%r15,%r10)
	je	.L360
	jmp	.L359
.L358:
	cmpl	$1, %edx
	jle	.L360
.L652:
	movq	stderr@GOTPCREL(%rip), %r15
	movq	(%r15), %rcx
	movl	$16, %edx
	movl	$1, %esi
	leaq	.LC7(%rip), %rdi
	call	fwrite@PLT
	jmp	.L361
.L360:
	addq	$1, %rax
	movq	%rax, %r14
	movl	%eax, %ebx
	movl	0(%r13,%rax,4), %r8d
	testl	%r8d, %r8d
	jne	.L534
	jmp	.L638
.L357:
	addq	$1, %rcx
	addl	%r11d, %r12d
	cmpl	%ecx, %r9d
	jg	.L354
	movq	stderr@GOTPCREL(%rip), %rbp
	movq	0(%rbp), %rcx
	movl	$21, %edx
	movl	$1, %esi
	leaq	.LC8(%rip), %rdi
	call	fwrite@PLT
	jmp	.L361
.L359:
	movslq	%ebx, %r14
	addl	$1, 0(%r13,%r14,4)
	cmpl	%ebx, %edi
	je	.L364
	movl	8(%rsp), %r12d
	movl	%r12d, %edx
	imull	%ebx, %edx
	imull	%edi, %r12d
	movl	72(%rsp), %r11d
	leal	(%r11,%r12), %r8d
	movl	%r12d, %eax
	subl	%r12d, %edx
	notl	%r12d
	addl	%r8d, %r12d
	andl	$3, %r12d
	movslq	%eax, %rcx
	addq	%r15, %rcx
	movzbl	(%rcx), %r9d
	leal	(%rdx,%rax), %r10d
	movslq	%r10d, %rsi
	addq	%r15, %rsi
	movzbl	(%rsi), %r11d
	movb	%r11b, (%rcx)
	movb	%r9b, (%rsi)
	addl	$1, %eax
	cmpl	%r8d, %eax
	jne	.L639
	jmp	.L364
.L365:
	movslq	%eax, %rsi
	addq	%r15, %rsi
	movzbl	(%rsi), %r11d
	leal	(%rdx,%rax), %r12d
	movslq	%r12d, %r9
	addq	%r15, %r9
	movzbl	(%r9), %r10d
	movb	%r10b, (%rsi)
	movb	%r11b, (%r9)
	addl	$1, %eax
	movl	%eax, %ecx
	movslq	%eax, %rsi
	addq	%r15, %rsi
	movzbl	(%rsi), %r11d
	addl	%edx, %ecx
	movslq	%ecx, %r12
	addq	%r15, %r12
	movzbl	(%r12), %r9d
	movb	%r9b, (%rsi)
	movb	%r11b, (%r12)
	leal	1(%rax), %r10d
	movslq	%r10d, %rsi
	addq	%r15, %rsi
	movzbl	(%rsi), %r11d
	addl	%edx, %r10d
	movslq	%r10d, %rcx
	addq	%r15, %rcx
	movzbl	(%rcx), %r12d
	movb	%r12b, (%rsi)
	movb	%r11b, (%rcx)
	leal	2(%rax), %r10d
	movslq	%r10d, %rsi
	addq	%r15, %rsi
	movzbl	(%rsi), %r9d
	addl	%edx, %r10d
	movslq	%r10d, %r11
	addq	%r15, %r11
	movzbl	(%r11), %ecx
	movb	%cl, (%rsi)
	movb	%r9b, (%r11)
	addl	$3, %eax
	cmpl	%r8d, %eax
	jne	.L365
.L364:
	movq	88(%rsp), %rdx
	movq	24(%rsp), %r8
	movl	%edi, (%rdx,%r8,4)
	movq	80(%rsp), %rax
	movl	%ebx, (%rax,%r8,4)
	movl	8(%rsp), %eax
	imull	%ebx, %eax
	cltq
	addq	%r15, %rax
	movq	%rax, 32(%rsp)
	leaq	(%rax,%r14), %r12
	movzbl	(%r12), %r10d
	testb	%r10b, %r10b
	jne	.L366
	movq	stderr@GOTPCREL(%rip), %rsi
	movq	(%rsi), %rcx
	movl	$18, %edx
	movl	$1, %esi
	leaq	.LC9(%rip), %rdi
	call	fwrite@PLT
	jmp	.L361
.L366:
	cmpb	$1, %r10b
	je	.L367
	movzbl	%r10b, %esi
	leaq	inverse(%rip), %r9
	movzbl	(%r9,%rsi), %ecx
	movb	$1, (%r12)
	movq	32(%rsp), %r11
	movq	%r11, %r8
	movq	%r11, %r12
	addq	120(%rsp), %r12
	movzbl	%cl, %edx
	salq	$8, %rdx
	movq	%r11, %rax
	notq	%rax
	addq	%r12, %rax
	andl	$3, %eax
	movzbl	(%r11), %r10d
	addq	%rdx, %r10
	movzbl	0(%rbp,%r10), %esi
	movb	%sil, (%r11)
	addq	$1, %r8
	cmpq	%r12, %r8
	jne	.L640
	jmp	.L367
.L368:
	movzbl	(%r8), %r9d
	addq	%rdx, %r9
	movzbl	0(%rbp,%r9), %ecx
	movb	%cl, (%r8)
	leaq	1(%r8), %r11
	movzbl	1(%r8), %eax
	addq	%rdx, %rax
	movzbl	0(%rbp,%rax), %edi
	movb	%dil, 1(%r8)
	movzbl	2(%r8), %r8d
	addq	%rdx, %r8
	movzbl	0(%rbp,%r8), %r10d
	movb	%r10b, 1(%r11)
	movzbl	2(%r11), %esi
	addq	%rdx, %rsi
	movzbl	0(%rbp,%rsi), %r9d
	movb	%r9b, 2(%r11)
	leaq	3(%r11), %r8
	cmpq	%r12, %r8
	jne	.L368
.L367:
	movq	96(%rsp), %rsi
	movq	%rsi, %r12
	addq	%r14, %r12
	movq	%r12, 104(%rsp)
	movb	$1, (%r12)
	movq	40(%rsp), %rdx
	movq	32(%rsp), %rdi
	call	bcmp@PLT
	testl	%eax, %eax
	je	.L369
	movq	%r15, 128(%rsp)
	movl	$0, %r12d
	movl	8(%rsp), %edx
	leal	-1(%rdx), %ecx
	andl	$3, %ecx
	movl	%ecx, 136(%rsp)
	cmpl	%r12d, %ebx
	jne	.L641
	jmp	.L478
.L686:
	movq	%r15, 128(%rsp)
	movq	%r13, 136(%rsp)
	movq	40(%rsp), %r15
	movl	64(%rsp), %r13d
	jmp	.L371
.L687:
	movq	%r15, 128(%rsp)
	movq	%r13, 136(%rsp)
	movq	40(%rsp), %r15
	movl	64(%rsp), %r13d
.L371:
	cmpl	%r13d, %ebx
	je	.L370
	movzbl	(%r12,%r14), %edx
	movb	$0, (%r12,%r14)
	testb	%dl, %dl
	je	.L370
	movzbl	%dl, %edx
	movl	8(%rsp), %ecx
	movq	32(%rsp), %rsi
	movq	%r12, %rdi
	call	addmul1
.L370:
	addl	$1, %r13d
	addq	%r15, %r12
	cmpl	%r13d, %ebx
	jne	.L642
	jmp	.L485
.L369:
	movq	104(%rsp), %rbx
	movb	$0, (%rbx)
	addq	$1, 24(%rsp)
	movl	144(%rsp), %r14d
	addl	%r14d, 68(%rsp)
	movl	24(%rsp), %r12d
	cmpl	%r12d, 8(%rsp)
	jg	.L373
	movl	148(%rsp), %r15d
	movq	152(%rsp), %r14
.L372:
	movl	8(%rsp), %esi
	movl	%esi, %ebx
	subl	$1, %ebx
	js	.L374
	movl	%esi, %ebp
	movl	%r15d, 8(%rsp)
	movl	%esi, %r15d
	movq	56(%rsp), %r12
	movq	%r13, 24(%rsp)
	movq	88(%rsp), %r13
.L381:
	movslq	%ebx, %r11
	salq	$2, %r11
	leaq	0(%r13,%r11), %rdx
	movl	(%rdx), %ecx
	testl	%ecx, %ecx
	js	.L375
	cmpl	%ecx, %r15d
	jg	.L376
.L375:
	leaq	.LC10(%rip), %rdx
	movl	$1, %esi
	movq	stderr@GOTPCREL(%rip), %r10
	movq	(%r10), %rdi
	movl	$0, %eax
	call	__fprintf_chk@PLT
	jmp	.L377
.L376:
	addq	80(%rsp), %r11
	movl	(%r11), %r9d
	testl	%r9d, %r9d
	js	.L378
	cmpl	%r9d, %r15d
	jg	.L379
.L378:
	movl	%r9d, %ecx
	leaq	.LC11(%rip), %rdx
	movl	$1, %esi
	movq	stderr@GOTPCREL(%rip), %r11
	movq	(%r11), %rdi
	movl	$0, %eax
	call	__fprintf_chk@PLT
	jmp	.L377
.L379:
	cmpl	%r9d, %ecx
	je	.L377
	testl	%r15d, %r15d
	.p2align 4,,5
	jle	.L377
	leal	-1(%r15), %eax
	andl	$3, %eax
	movslq	(%rdx), %rcx
	addq	%r12, %rcx
	movzbl	(%rcx), %edi
	movslq	(%r11), %r8
	movzbl	(%r12,%r8), %r10d
	movb	%r10b, (%rcx)
	movslq	(%r11), %rsi
	movb	%dil, (%r12,%rsi)
	movl	$1, %r10d
	movl	%ebp, %ecx
	cmpl	%r15d, %r10d
	jne	.L643
	jmp	.L377
.L380:
	movl	%ecx, %r8d
	addl	(%rdx), %r8d
	movslq	%r8d, %rax
	addq	%r12, %rax
	movzbl	(%rax), %esi
	movl	%ecx, %r9d
	addl	(%r11), %r9d
	movslq	%r9d, %rdi
	movzbl	(%r12,%rdi), %r8d
	movb	%r8b, (%rax)
	movl	%ecx, %eax
	addl	(%r11), %eax
	movslq	%eax, %r9
	movb	%sil, (%r12,%r9)
	addl	$1, %r10d
	addl	%ebp, %ecx
	movl	%ecx, %esi
	addl	(%rdx), %esi
	movslq	%esi, %rdi
	addq	%r12, %rdi
	movzbl	(%rdi), %r8d
	movl	%ecx, %eax
	addl	(%r11), %eax
	movslq	%eax, %r9
	movzbl	(%r12,%r9), %esi
	movb	%sil, (%rdi)
	movl	%ecx, %edi
	addl	(%r11), %edi
	movslq	%edi, %rax
	movb	%r8b, (%r12,%rax)
	addl	%ebp, %ecx
	movl	%ecx, %r8d
	addl	(%rdx), %r8d
	movslq	%r8d, %rsi
	addq	%r12, %rsi
	movzbl	(%rsi), %eax
	movl	%ecx, %r9d
	addl	(%r11), %r9d
	movslq	%r9d, %rdi
	movzbl	(%r12,%rdi), %r8d
	movb	%r8b, (%rsi)
	movl	%ecx, %esi
	addl	(%r11), %esi
	movslq	%esi, %r9
	movb	%al, (%r12,%r9)
	addl	%ebp, %ecx
	movl	%ecx, %eax
	addl	(%rdx), %eax
	movslq	%eax, %rdi
	addq	%r12, %rdi
	movzbl	(%rdi), %r8d
	movl	%ecx, %esi
	addl	(%r11), %esi
	movslq	%esi, %r9
	movzbl	(%r12,%r9), %eax
	movb	%al, (%rdi)
	movl	%ecx, %edi
	addl	(%r11), %edi
	movslq	%edi, %rsi
	movb	%r8b, (%r12,%rsi)
	addl	$3, %r10d
	addl	%ebp, %ecx
	cmpl	%r15d, %r10d
	jne	.L380
.L377:
	subl	$1, %ebx
	cmpl	$-1, %ebx
	jne	.L381
	movl	8(%rsp), %r15d
	movq	24(%rsp), %r13
	jmp	.L374
.L651:
	movl	%r15d, 68(%rsp)
	leal	0(,%r15,8), %edi
	leaq	.LC12(%rip), %rsi
	call	my_malloc
	movq	%rax, %r13
	testl	%r15d, %r15d
	jle	.L382
	movl	%r15d, 64(%rsp)
	movl	$0, 24(%rsp)
	movl	$0, %ebp
	movslq	20(%rsp), %rdx
	movq	%rdx, 72(%rsp)
	movl	%r15d, 8(%rsp)
	movq	%r14, 32(%rsp)
	movq	56(%rsp), %r14
.L387:
	movq	32(%rsp), %rcx
	movl	8(%rsp), %ebx
	cmpl	(%rcx,%rbp,4), %ebx
	jg	.L383
	leaq	.LC13(%rip), %rsi
	movl	20(%rsp), %edi
	call	my_malloc
	movq	%rax, 0(%r13,%rbp,8)
	movq	72(%rsp), %rdx
	movl	$0, %esi
	movq	%rax, %rdi
	call	memset@PLT
	movq	48(%rsp), %r12
	movl	68(%rsp), %r8d
	movl	24(%rsp), %ebx
	addl	%ebx, %r8d
	movl	%r8d, 40(%rsp)
	movl	%ebx, %r15d
	notl	%r15d
	addl	%r8d, %r15d
	andl	$7, %r15d
	movslq	%ebx, %r9
	movzbl	(%r14,%r9), %eax
	testb	%al, %al
	je	.L433
	jmp	.L644
.L385:
	movslq	%ebx, %r8
	movzbl	(%r14,%r8), %r15d
	testb	%r15b, %r15b
	je	.L384
	movzbl	%r15b, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	20(%rsp), %ecx
	movq	(%r12), %rsi
	call	addmul1
.L384:
	addl	$1, %ebx
	movl	%ebx, %eax
	leaq	8(%r12), %r15
	cltq
	movzbl	(%r14,%rax), %r9d
	testb	%r9b, %r9b
	je	.L448
	jmp	.L645
.L383:
	addq	$1, %rbp
	movl	64(%rsp), %ebx
	addl	%ebx, 24(%rsp)
	cmpl	%ebp, 8(%rsp)
	jg	.L387
	movl	8(%rsp), %r15d
	movq	32(%rsp), %r14
	movslq	20(%rsp), %rbp
	movl	$0, %ebx
	leal	-1(%r15), %r12d
	andl	$7, %r12d
	cmpl	(%r14,%rbx,4), %r15d
	jg	.L397
	jmp	.L646
.L680:
	movq	%rbp, 8(%rsp)
	movq	48(%rsp), %rbp
	jmp	.L389
.L681:
	movq	%rbp, 8(%rsp)
	movq	48(%rsp), %rbp
.L389:
	cmpl	(%r14,%rbx,4), %r15d
	jg	.L388
	movq	0(%r13,%rbx,8), %rsi
	movq	0(%rbp,%rbx,8), %rdi
	movq	8(%rsp), %rdx
	call	memmove@PLT
	movq	0(%r13,%rbx,8), %rdi
	call	free@PLT
	movl	%ebx, (%r14,%rbx,4)
.L388:
	addq	$1, %rbx
	cmpl	(%r14,%rbx,4), %r15d
	jg	.L412
	jmp	.L647
.L382:
	movq	%r13, %rdi
	call	free@PLT
	movq	56(%rsp), %rdi
	call	free@PLT
	movl	$0, %eax
	jmp	.L648
.L394:
	movl	$1, %eax
	jmp	.L648
.L395:
	movl	$1, %eax
	jmp	.L648
.L716:
	movl	8(%rbp), %ebx
	movl	%ebx, 8(%rsp)
	movl	%ebx, %edi
	imull	%ebx, %edi
	leaq	.LC1(%rip), %rsi
	call	my_malloc
	movq	%rax, %r13
	movq	%rax, 56(%rsp)
	testl	%ebx, %ebx
	jle	.L390
	movl	%ebx, %esi
	movslq	%ebx, %rbx
	movl	$0, %r12d
	leal	-1(%rsi), %ecx
	andl	$3, %ecx
	movl	%ecx, 24(%rsp)
	movq	%r12, 32(%rsp)
	movl	(%r14,%r12,4), %ecx
	cmpl	%ecx, %esi
	jle	.L583
	jmp	.L649
.L667:
	movl	8(%rsp), %ebp
	movl	%ebp, %ebx
	movl	%ebp, 72(%rsp)
	leal	0(,%rbp,4), %r13d
	leaq	.LC14(%rip), %rsi
	movl	%r13d, %edi
	call	my_malloc
	movq	%rax, 80(%rsp)
	leaq	.LC15(%rip), %rsi
	movl	%r13d, %edi
	call	my_malloc
	movq	%rax, 88(%rsp)
	leaq	.LC16(%rip), %rsi
	movl	%r13d, %edi
	call	my_malloc
	movq	%rax, %r13
	leaq	.LC1(%rip), %rsi
	movl	%ebp, %edi
	call	my_malloc
	movq	%rax, %rbp
	movq	%rax, 96(%rsp)
	leaq	.LC1(%rip), %rsi
	movl	%ebx, %edi
	call	my_malloc
	movq	%rax, 112(%rsp)
	movq	32(%rsp), %rdx
	movq	%rdx, 40(%rsp)
	movl	$0, %esi
	movq	%rbp, %rdi
	call	memset@PLT
	movl	$0, %r8d
	leal	-1(%rbx), %edi
	andl	$7, %edi
	movl	$0, 0(%r13,%r8,4)
	addq	$1, %r8
	cmpl	%r8d, %ebx
	jg	.L600
	jmp	.L650
.L390:
	movl	8(%rsp), %ebp
	leal	0(,%rbp,4), %ebx
	leaq	.LC14(%rip), %rsi
	movl	%ebx, %edi
	call	my_malloc
	movq	%rax, 80(%rsp)
	leaq	.LC15(%rip), %rsi
	movl	%ebx, %edi
	call	my_malloc
	movq	%rax, 88(%rsp)
	leaq	.LC16(%rip), %rsi
	movl	%ebx, %edi
	call	my_malloc
	movq	%rax, %r13
	leaq	.LC1(%rip), %rsi
	movl	%ebp, %r12d
	movl	%ebp, %edi
	call	my_malloc
	movq	%rax, %rbp
	movq	%rax, 96(%rsp)
	leaq	.LC1(%rip), %rsi
	movl	%r12d, %edi
	call	my_malloc
	movq	%rax, 112(%rsp)
	movslq	%r12d, %rdx
	movl	$0, %esi
	movq	%rbp, %rdi
	call	memset@PLT
	jmp	.L372
.L355:
	movq	24(%rsp), %rdi
	movq	%rdi, %r14
	addl	$1, %r8d
	movl	%r8d, 0(%r13,%rdi,4)
	movl	%ebx, %edi
	jmp	.L364
.L361:
	movq	80(%rsp), %rdi
	call	free@PLT
	movq	88(%rsp), %rdi
	call	free@PLT
	movq	%r13, %rdi
	call	free@PLT
	movq	96(%rsp), %rdi
	call	free@PLT
	movq	112(%rsp), %rdi
	call	free@PLT
	movq	56(%rsp), %rdi
	call	free@PLT
	jmp	.L349
.L374:
	movq	80(%rsp), %rdi
	call	free@PLT
	movq	88(%rsp), %rdi
	call	free@PLT
	movq	%r13, %rdi
	call	free@PLT
	movq	96(%rsp), %rdi
	call	free@PLT
	movq	112(%rsp), %rdi
	call	free@PLT
	cmpq	$0, 56(%rsp)
	jne	.L651
	jmp	.L395
.L349:
	movl	$1, %eax
	jmp	.L648
.L339:
	movl	8(%rdi), %eax
	movl	%eax, 8(%rsp)
	movl	%eax, %edi
	imull	%eax, %edi
	leaq	.LC1(%rip), %rsi
	call	my_malloc
	movq	%rax, 56(%rsp)
	jmp	.L390
.L648:
	addq	$168, %rsp
	.cfi_remember_state
	.cfi_def_cfa_offset 56
	popq	%rbx
	.cfi_def_cfa_offset 48
	popq	%rbp
	.cfi_def_cfa_offset 40
	popq	%r12
	.cfi_def_cfa_offset 32
	popq	%r13
	.cfi_def_cfa_offset 24
	popq	%r14
	.cfi_def_cfa_offset 16
	popq	%r15
	.cfi_def_cfa_offset 8
	ret
.L646:
	.cfi_restore_state
	movq	0(%r13,%rbx,8), %rsi
	movq	48(%rsp), %rcx
	movq	(%rcx,%rbx,8), %rdi
	movq	%rbp, %rdx
	call	memmove@PLT
	movq	0(%r13,%rbx,8), %rdi
	call	free@PLT
	movl	%ebx, (%r14,%rbx,4)
.L397:
	addq	$1, %rbx
	cmpl	%ebx, %r15d
	jle	.L382
	testl	%r12d, %r12d
	je	.L680
	cmpl	$1, %r12d
	je	.L601
	cmpl	$2, %r12d
	je	.L602
	cmpl	$3, %r12d
	je	.L603
	cmpl	$4, %r12d
	je	.L604
	cmpl	$5, %r12d
	je	.L605
	cmpl	$6, %r12d
	je	.L606
	cmpl	(%r14,%rbx,4), %r15d
	jg	.L398
	movq	0(%r13,%rbx,8), %rsi
	movq	48(%rsp), %r8
	movq	(%r8,%rbx,8), %rdi
	movq	%rbp, %rdx
	call	memmove@PLT
	movq	0(%r13,%rbx,8), %rdi
	call	free@PLT
	movl	%ebx, (%r14,%rbx,4)
.L398:
	addq	$1, %rbx
.L606:
	cmpl	(%r14,%rbx,4), %r15d
	jg	.L400
	movq	0(%r13,%rbx,8), %rsi
	movq	48(%rsp), %r9
	movq	(%r9,%rbx,8), %rdi
	movq	%rbp, %rdx
	call	memmove@PLT
	movq	0(%r13,%rbx,8), %rdi
	call	free@PLT
	movl	%ebx, (%r14,%rbx,4)
.L400:
	addq	$1, %rbx
.L605:
	cmpl	(%r14,%rbx,4), %r15d
	jg	.L402
	movq	0(%r13,%rbx,8), %rsi
	movq	48(%rsp), %rdi
	movq	(%rdi,%rbx,8), %rdi
	movq	%rbp, %rdx
	call	memmove@PLT
	movq	0(%r13,%rbx,8), %rdi
	call	free@PLT
	movl	%ebx, (%r14,%rbx,4)
.L402:
	addq	$1, %rbx
.L604:
	cmpl	(%r14,%rbx,4), %r15d
	jg	.L404
	movq	0(%r13,%rbx,8), %rsi
	movq	48(%rsp), %r11
	movq	(%r11,%rbx,8), %rdi
	movq	%rbp, %rdx
	call	memmove@PLT
	movq	0(%r13,%rbx,8), %rdi
	call	free@PLT
	movl	%ebx, (%r14,%rbx,4)
.L404:
	addq	$1, %rbx
.L603:
	cmpl	(%r14,%rbx,4), %r15d
	jg	.L406
	movq	0(%r13,%rbx,8), %rsi
	movq	48(%rsp), %r10
	movq	(%r10,%rbx,8), %rdi
	movq	%rbp, %rdx
	call	memmove@PLT
	movq	0(%r13,%rbx,8), %rdi
	call	free@PLT
	movl	%ebx, (%r14,%rbx,4)
.L406:
	addq	$1, %rbx
.L602:
	cmpl	(%r14,%rbx,4), %r15d
	jg	.L408
	movq	0(%r13,%rbx,8), %rsi
	movq	48(%rsp), %rax
	movq	(%rax,%rbx,8), %rdi
	movq	%rbp, %rdx
	call	memmove@PLT
	movq	0(%r13,%rbx,8), %rdi
	call	free@PLT
	movl	%ebx, (%r14,%rbx,4)
.L408:
	addq	$1, %rbx
.L601:
	cmpl	(%r14,%rbx,4), %r15d
	jg	.L410
	movq	0(%r13,%rbx,8), %rsi
	movq	48(%rsp), %rdx
	movq	(%rdx,%rbx,8), %rdi
	movq	%rbp, %rdx
	call	memmove@PLT
	movq	0(%r13,%rbx,8), %rdi
	call	free@PLT
	movl	%ebx, (%r14,%rbx,4)
.L410:
	addq	$1, %rbx
	cmpl	%ebx, %r15d
	jg	.L681
	jmp	.L382
.L647:
	movq	0(%r13,%rbx,8), %rsi
	movq	0(%rbp,%rbx,8), %rdi
	movq	8(%rsp), %rdx
	call	memmove@PLT
	movq	0(%r13,%rbx,8), %rdi
	call	free@PLT
	movl	%ebx, (%r14,%rbx,4)
.L412:
	leaq	1(%rbx), %r12
	cmpl	(%r14,%r12,4), %r15d
	jg	.L413
	movq	0(%r13,%r12,8), %rsi
	movq	0(%rbp,%r12,8), %rdi
	movq	8(%rsp), %rdx
	call	memmove@PLT
	movq	0(%r13,%r12,8), %rdi
	call	free@PLT
	movl	%r12d, (%r14,%r12,4)
.L413:
	leaq	2(%rbx), %r12
	cmpl	(%r14,%r12,4), %r15d
	jg	.L414
	movq	0(%r13,%r12,8), %rsi
	movq	0(%rbp,%r12,8), %rdi
	movq	8(%rsp), %rdx
	call	memmove@PLT
	movq	0(%r13,%r12,8), %rdi
	call	free@PLT
	movl	%r12d, (%r14,%r12,4)
.L414:
	leaq	3(%rbx), %r12
	cmpl	(%r14,%r12,4), %r15d
	jg	.L415
	movq	0(%r13,%r12,8), %rsi
	movq	0(%rbp,%r12,8), %rdi
	movq	8(%rsp), %rdx
	call	memmove@PLT
	movq	0(%r13,%r12,8), %rdi
	call	free@PLT
	movl	%r12d, (%r14,%r12,4)
.L415:
	leaq	4(%rbx), %r12
	cmpl	(%r14,%r12,4), %r15d
	jg	.L416
	movq	0(%r13,%r12,8), %rsi
	movq	0(%rbp,%r12,8), %rdi
	movq	8(%rsp), %rdx
	call	memmove@PLT
	movq	0(%r13,%r12,8), %rdi
	call	free@PLT
	movl	%r12d, (%r14,%r12,4)
.L416:
	leaq	5(%rbx), %r12
	cmpl	(%r14,%r12,4), %r15d
	jg	.L417
	movq	0(%r13,%r12,8), %rsi
	movq	0(%rbp,%r12,8), %rdi
	movq	8(%rsp), %rdx
	call	memmove@PLT
	movq	0(%r13,%r12,8), %rdi
	call	free@PLT
	movl	%r12d, (%r14,%r12,4)
.L417:
	leaq	6(%rbx), %r12
	cmpl	(%r14,%r12,4), %r15d
	jg	.L418
	movq	0(%r13,%r12,8), %rsi
	movq	0(%rbp,%r12,8), %rdi
	movq	8(%rsp), %rdx
	call	memmove@PLT
	movq	0(%r13,%r12,8), %rdi
	call	free@PLT
	movl	%r12d, (%r14,%r12,4)
.L418:
	addq	$7, %rbx
	cmpl	%ebx, %r15d
	jg	.L389
	jmp	.L382
.L644:
	movzbl	%al, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	20(%rsp), %ecx
	movq	48(%rsp), %rsi
	movq	(%rsi), %rsi
	call	addmul1
.L433:
	addl	$1, %ebx
	addq	$8, %r12
	cmpl	40(%rsp), %ebx
	je	.L383
	testl	%r15d, %r15d
	je	.L385
	cmpl	$1, %r15d
	je	.L607
	cmpl	$2, %r15d
	je	.L608
	cmpl	$3, %r15d
	je	.L609
	cmpl	$4, %r15d
	je	.L610
	cmpl	$5, %r15d
	je	.L611
	cmpl	$6, %r15d
	je	.L612
	movslq	%ebx, %rdi
	movzbl	(%r14,%rdi), %r11d
	testb	%r11b, %r11b
	je	.L434
	movzbl	%r11b, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	20(%rsp), %ecx
	movq	48(%rsp), %r10
	movq	8(%r10), %rsi
	call	addmul1
.L434:
	addl	$1, %ebx
	addq	$8, %r12
.L612:
	movslq	%ebx, %rdx
	movzbl	(%r14,%rdx), %ecx
	testb	%cl, %cl
	je	.L436
	movzbl	%cl, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	20(%rsp), %ecx
	movq	(%r12), %rsi
	call	addmul1
.L436:
	addl	$1, %ebx
	addq	$8, %r12
.L611:
	movslq	%ebx, %r8
	movzbl	(%r14,%r8), %r15d
	testb	%r15b, %r15b
	je	.L438
	movzbl	%r15b, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	20(%rsp), %ecx
	movq	(%r12), %rsi
	call	addmul1
.L438:
	addl	$1, %ebx
	addq	$8, %r12
.L610:
	movslq	%ebx, %r9
	movzbl	(%r14,%r9), %eax
	testb	%al, %al
	je	.L440
	movzbl	%al, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	20(%rsp), %ecx
	movq	(%r12), %rsi
	call	addmul1
.L440:
	addl	$1, %ebx
	addq	$8, %r12
.L609:
	movslq	%ebx, %rsi
	movzbl	(%r14,%rsi), %edi
	testb	%dil, %dil
	je	.L442
	movzbl	%dil, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	20(%rsp), %ecx
	movq	(%r12), %rsi
	call	addmul1
.L442:
	addl	$1, %ebx
	addq	$8, %r12
.L608:
	movslq	%ebx, %r11
	movzbl	(%r14,%r11), %r10d
	testb	%r10b, %r10b
	je	.L444
	movzbl	%r10b, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	20(%rsp), %ecx
	movq	(%r12), %rsi
	call	addmul1
.L444:
	addl	$1, %ebx
	addq	$8, %r12
.L607:
	movslq	%ebx, %rdx
	movzbl	(%r14,%rdx), %ecx
	testb	%cl, %cl
	je	.L446
	movzbl	%cl, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	20(%rsp), %ecx
	movq	(%r12), %rsi
	call	addmul1
.L446:
	addl	$1, %ebx
	addq	$8, %r12
	cmpl	40(%rsp), %ebx
	jne	.L385
	jmp	.L383
.L645:
	movzbl	%r9b, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	20(%rsp), %ecx
	movq	8(%r12), %rsi
	call	addmul1
.L448:
	leal	1(%rbx), %eax
	cltq
	movzbl	(%r14,%rax), %r12d
	testb	%r12b, %r12b
	je	.L449
	movzbl	%r12b, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	20(%rsp), %ecx
	movq	8(%r15), %rsi
	call	addmul1
.L449:
	leal	2(%rbx), %eax
	cltq
	movzbl	(%r14,%rax), %esi
	testb	%sil, %sil
	je	.L450
	movzbl	%sil, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	20(%rsp), %ecx
	movq	16(%r15), %rsi
	call	addmul1
.L450:
	leal	3(%rbx), %eax
	cltq
	movzbl	(%r14,%rax), %edi
	testb	%dil, %dil
	je	.L451
	movzbl	%dil, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	20(%rsp), %ecx
	movq	24(%r15), %rsi
	call	addmul1
.L451:
	leal	4(%rbx), %eax
	cltq
	movzbl	(%r14,%rax), %r11d
	testb	%r11b, %r11b
	je	.L452
	movzbl	%r11b, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	20(%rsp), %ecx
	movq	32(%r15), %rsi
	call	addmul1
.L452:
	leal	5(%rbx), %eax
	cltq
	movzbl	(%r14,%rax), %r10d
	testb	%r10b, %r10b
	je	.L453
	movzbl	%r10b, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	20(%rsp), %ecx
	movq	40(%r15), %rsi
	call	addmul1
.L453:
	leal	6(%rbx), %eax
	cltq
	movzbl	(%r14,%rax), %edx
	testb	%dl, %dl
	je	.L454
	movzbl	%dl, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	20(%rsp), %ecx
	movq	48(%r15), %rsi
	call	addmul1
.L454:
	addl	$7, %ebx
	leaq	56(%r15), %r12
	cmpl	40(%rsp), %ebx
	jne	.L385
	jmp	.L383
.L643:
	testl	%eax, %eax
	je	.L380
	cmpl	$1, %eax
	.p2align 4,,6
	je	.L613
	cmpl	$2, %eax
	.p2align 4,,3
	je	.L614
	movl	%ebp, %r9d
	addl	(%rdx), %r9d
	movslq	%r9d, %rax
	addq	%r12, %rax
	movzbl	(%rax), %r8d
	movl	%ebp, %edi
	addl	(%r11), %edi
	movslq	%edi, %rsi
	movzbl	(%r12,%rsi), %r9d
	movb	%r9b, (%rax)
	movl	%ebp, %eax
	addl	(%r11), %eax
	movslq	%eax, %rdi
	movb	%r8b, (%r12,%rdi)
	addl	$1, %r10d
	addl	%ebp, %ecx
.L614:
	movl	%ecx, %r8d
	addl	(%rdx), %r8d
	movslq	%r8d, %rsi
	addq	%r12, %rsi
	movzbl	(%rsi), %eax
	movl	%ecx, %r9d
	addl	(%r11), %r9d
	movslq	%r9d, %rdi
	movzbl	(%r12,%rdi), %r8d
	movb	%r8b, (%rsi)
	movl	%ecx, %esi
	addl	(%r11), %esi
	movslq	%esi, %r9
	movb	%al, (%r12,%r9)
	addl	$1, %r10d
	addl	%ebp, %ecx
.L613:
	movl	%ecx, %eax
	addl	(%rdx), %eax
	movslq	%eax, %rdi
	addq	%r12, %rdi
	movzbl	(%rdi), %r8d
	movl	%ecx, %esi
	addl	(%r11), %esi
	movslq	%esi, %r9
	movzbl	(%r12,%r9), %eax
	movb	%al, (%rdi)
	movl	%ecx, %edi
	addl	(%r11), %edi
	movslq	%edi, %rsi
	movb	%r8b, (%r12,%rsi)
	addl	$1, %r10d
	addl	%ebp, %ecx
	cmpl	%r15d, %r10d
	jne	.L380
	jmp	.L377
.L641:
	movzbl	(%r15,%r14), %r11d
	movb	$0, (%r15,%r14)
	testb	%r11b, %r11b
	je	.L478
	movzbl	%r11b, %edx
	movl	8(%rsp), %ecx
	movq	32(%rsp), %rsi
	movq	128(%rsp), %rdi
	call	addmul1
.L478:
	leal	1(%r12), %eax
	movl	%eax, 64(%rsp)
	movq	128(%rsp), %r12
	addq	40(%rsp), %r12
	cmpl	8(%rsp), %eax
	je	.L369
	movl	136(%rsp), %edi
	testl	%edi, %edi
	je	.L686
	cmpl	$1, %edi
	je	.L615
	cmpl	$2, %edi
	je	.L616
	cmpl	%eax, %ebx
	.p2align 4,,3
	je	.L479
	movzbl	(%r12,%r14), %r8d
	movb	$0, (%r12,%r14)
	testb	%r8b, %r8b
	je	.L479
	movzbl	%r8b, %edx
	movl	8(%rsp), %ecx
	movq	32(%rsp), %rsi
	movq	%r12, %rdi
	call	addmul1
.L479:
	addl	$1, 64(%rsp)
	addq	40(%rsp), %r12
.L616:
	cmpl	64(%rsp), %ebx
	je	.L481
	movzbl	(%r12,%r14), %r10d
	movb	$0, (%r12,%r14)
	testb	%r10b, %r10b
	je	.L481
	movzbl	%r10b, %edx
	movl	8(%rsp), %ecx
	movq	32(%rsp), %rsi
	movq	%r12, %rdi
	call	addmul1
.L481:
	addl	$1, 64(%rsp)
	addq	40(%rsp), %r12
.L615:
	cmpl	64(%rsp), %ebx
	je	.L483
	movzbl	(%r12,%r14), %esi
	movb	$0, (%r12,%r14)
	testb	%sil, %sil
	je	.L483
	movzbl	%sil, %edx
	movl	8(%rsp), %ecx
	movq	32(%rsp), %rsi
	movq	%r12, %rdi
	call	addmul1
.L483:
	addl	$1, 64(%rsp)
	addq	40(%rsp), %r12
	movl	8(%rsp), %r9d
	cmpl	%r9d, 64(%rsp)
	jne	.L687
	jmp	.L369
.L642:
	movzbl	(%r12,%r14), %r11d
	movb	$0, (%r12,%r14)
	testb	%r11b, %r11b
	je	.L485
	movzbl	%r11b, %edx
	movl	8(%rsp), %ecx
	movq	32(%rsp), %rsi
	movq	%r12, %rdi
	call	addmul1
.L485:
	leal	1(%r13), %eax
	addq	%r15, %r12
	cmpl	%eax, %ebx
	je	.L486
	movzbl	(%r12,%r14), %edi
	movb	$0, (%r12,%r14)
	testb	%dil, %dil
	je	.L486
	movzbl	%dil, %edx
	movl	8(%rsp), %ecx
	movq	32(%rsp), %rsi
	movq	%r12, %rdi
	call	addmul1
.L486:
	leal	2(%r13), %r8d
	addq	%r15, %r12
	cmpl	%r8d, %ebx
	je	.L487
	movzbl	(%r12,%r14), %r10d
	movb	$0, (%r12,%r14)
	testb	%r10b, %r10b
	je	.L487
	movzbl	%r10b, %edx
	movl	8(%rsp), %ecx
	movq	32(%rsp), %rsi
	movq	%r12, %rdi
	call	addmul1
.L487:
	addl	$3, %r13d
	addq	%r15, %r12
	cmpl	8(%rsp), %r13d
	jne	.L371
	movq	128(%rsp), %r15
	movq	136(%rsp), %r13
	jmp	.L369
.L640:
	testq	%rax, %rax
	je	.L368
	cmpq	$1, %rax
	je	.L617
	cmpq	$2, %rax
	je	.L618
	movq	32(%rsp), %r9
	movzbl	1(%r9), %ecx
	addq	%rdx, %rcx
	movzbl	0(%rbp,%rcx), %r11d
	movb	%r11b, 1(%r9)
	addq	$1, %r8
.L618:
	movzbl	(%r8), %eax
	addq	%rdx, %rax
	movzbl	0(%rbp,%rax), %edi
	movb	%dil, (%r8)
	addq	$1, %r8
.L617:
	movzbl	(%r8), %r10d
	addq	%rdx, %r10
	movzbl	0(%rbp,%r10), %esi
	movb	%sil, (%r8)
	addq	$1, %r8
	cmpq	%r12, %r8
	jne	.L368
	jmp	.L367
.L639:
	testl	%r12d, %r12d
	je	.L365
	cmpl	$1, %r12d
	.p2align 4,,4
	je	.L619
	cmpl	$2, %r12d
	je	.L620
	movslq	%eax, %r12
	addq	%r15, %r12
	movzbl	(%r12), %r9d
	leal	(%rdx,%rax), %ecx
	movslq	%ecx, %rsi
	addq	%r15, %rsi
	movzbl	(%rsi), %r10d
	movb	%r10b, (%r12)
	movb	%r9b, (%rsi)
	addl	$1, %eax
.L620:
	movslq	%eax, %r11
	addq	%r15, %r11
	movzbl	(%r11), %r12d
	leal	(%rdx,%rax), %r9d
	movslq	%r9d, %rcx
	addq	%r15, %rcx
	movzbl	(%rcx), %esi
	movb	%sil, (%r11)
	movb	%r12b, (%rcx)
	addl	$1, %eax
.L619:
	movslq	%eax, %r11
	addq	%r15, %r11
	movzbl	(%r11), %r12d
	leal	(%rdx,%rax), %r10d
	movslq	%r10d, %r9
	addq	%r15, %r9
	movzbl	(%r9), %ecx
	movb	%cl, (%r11)
	movb	%r12b, (%r9)
	addl	$1, %eax
	cmpl	%r8d, %eax
	jne	.L365
	jmp	.L364
.L511:
	cmpl	$1, %r10d
	jle	.L512
	.p2align 4,,7
	jmp	.L652
.L637:
	movslq	%r12d, %r14
	cmpb	$0, (%r15,%r14)
	jne	.L359
.L512:
	addq	$1, %rax
	cmpl	%eax, %r9d
	jle	.L357
	testl	%esi, %esi
	je	.L362
	cmpl	$1, %esi
	je	.L621
	cmpl	$2, %esi
	.p2align 4,,3
	je	.L622
	cmpl	$3, %esi
	.p2align 4,,2
	je	.L623
	cmpl	$4, %esi
	.p2align 4,,2
	je	.L624
	cmpl	$5, %esi
	.p2align 4,,2
	je	.L625
	cmpl	$6, %esi
	.p2align 4,,2
	je	.L626
	movl	%eax, %ebx
	movl	0(%r13,%rax,4), %r8d
	testl	%r8d, %r8d
	je	.L653
	cmpl	$1, %r8d
	jle	.L514
	jmp	.L652
.L653:
	leal	(%r12,%rax), %edx
	movslq	%edx, %rsi
	cmpb	$0, (%r15,%rsi)
	jne	.L359
.L514:
	addq	$1, %rax
.L626:
	movl	%eax, %ebx
	movl	0(%r13,%rax,4), %r10d
	testl	%r10d, %r10d
	je	.L654
	cmpl	$1, %r10d
	jle	.L517
	jmp	.L652
.L654:
	leal	(%r12,%rax), %r14d
	movslq	%r14d, %r8
	cmpb	$0, (%r15,%r8)
	jne	.L359
.L517:
	addq	$1, %rax
.L625:
	movl	%eax, %ebx
	movl	0(%r13,%rax,4), %edx
	testl	%edx, %edx
	je	.L655
	cmpl	$1, %edx
	jle	.L520
	jmp	.L652
.L655:
	leal	(%r12,%rax), %esi
	movslq	%esi, %r10
	cmpb	$0, (%r15,%r10)
	jne	.L359
.L520:
	addq	$1, %rax
.L624:
	movl	%eax, %ebx
	movl	0(%r13,%rax,4), %r14d
	testl	%r14d, %r14d
	je	.L656
	cmpl	$1, %r14d
	jle	.L523
	jmp	.L652
.L656:
	leal	(%r12,%rax), %r8d
	movslq	%r8d, %rdx
	cmpb	$0, (%r15,%rdx)
	jne	.L359
.L523:
	addq	$1, %rax
.L623:
	movl	%eax, %ebx
	movl	0(%r13,%rax,4), %esi
	testl	%esi, %esi
	je	.L657
	cmpl	$1, %esi
	jle	.L526
	jmp	.L652
.L657:
	leal	(%r12,%rax), %r10d
	movslq	%r10d, %r14
	cmpb	$0, (%r15,%r14)
	jne	.L359
.L526:
	addq	$1, %rax
.L622:
	movl	%eax, %ebx
	movl	0(%r13,%rax,4), %r8d
	testl	%r8d, %r8d
	je	.L658
	cmpl	$1, %r8d
	jle	.L529
	jmp	.L652
.L658:
	leal	(%r12,%rax), %edx
	movslq	%edx, %rsi
	cmpb	$0, (%r15,%rsi)
	jne	.L359
.L529:
	addq	$1, %rax
.L621:
	movl	%eax, %ebx
	movl	0(%r13,%rax,4), %r10d
	testl	%r10d, %r10d
	je	.L659
	cmpl	$1, %r10d
	jle	.L532
	jmp	.L652
.L659:
	leal	(%r12,%rax), %r14d
	movslq	%r14d, %r8
	cmpb	$0, (%r15,%r8)
	jne	.L359
.L532:
	addq	$1, %rax
	cmpl	%eax, %r9d
	jg	.L362
	jmp	.L357
.L534:
	cmpl	$1, %r8d
	jle	.L535
	.p2align 4,,7
	jmp	.L652
.L638:
	addl	%r12d, %r14d
	movslq	%r14d, %rdx
	cmpb	$0, (%r15,%rdx)
	jne	.L359
.L535:
	leaq	1(%rax), %rsi
	movl	%esi, %ebx
	movl	0(%r13,%rsi,4), %r10d
	testl	%r10d, %r10d
	je	.L660
	cmpl	$1, %r10d
	jle	.L537
	jmp	.L652
.L660:
	addl	%r12d, %esi
	movslq	%esi, %r14
	cmpb	$0, (%r15,%r14)
	jne	.L359
.L537:
	leaq	2(%rax), %r8
	movl	%r8d, %ebx
	movl	0(%r13,%r8,4), %edx
	testl	%edx, %edx
	je	.L661
	cmpl	$1, %edx
	jle	.L539
	jmp	.L652
.L661:
	addl	%r12d, %r8d
	movslq	%r8d, %rsi
	cmpb	$0, (%r15,%rsi)
	jne	.L359
.L539:
	leaq	3(%rax), %r14
	movl	%r14d, %ebx
	movl	0(%r13,%r14,4), %r10d
	testl	%r10d, %r10d
	je	.L662
	cmpl	$1, %r10d
	jle	.L541
	jmp	.L652
.L662:
	addl	%r12d, %r14d
	movslq	%r14d, %r8
	cmpb	$0, (%r15,%r8)
	jne	.L359
.L541:
	leaq	4(%rax), %rdx
	movl	%edx, %ebx
	movl	0(%r13,%rdx,4), %esi
	testl	%esi, %esi
	je	.L663
	cmpl	$1, %esi
	jle	.L543
	jmp	.L652
.L663:
	addl	%r12d, %edx
	movslq	%edx, %r14
	cmpb	$0, (%r15,%r14)
	jne	.L359
.L543:
	leaq	5(%rax), %r8
	movl	%r8d, %ebx
	movl	0(%r13,%r8,4), %r10d
	testl	%r10d, %r10d
	je	.L664
	cmpl	$1, %r10d
	jle	.L545
	jmp	.L652
.L664:
	addl	%r12d, %r8d
	movslq	%r8d, %rdx
	cmpb	$0, (%r15,%rdx)
	jne	.L359
.L545:
	leaq	6(%rax), %rsi
	movl	%esi, %ebx
	movl	0(%r13,%rsi,4), %r14d
	testl	%r14d, %r14d
	je	.L665
	cmpl	$1, %r14d
	jle	.L547
	jmp	.L652
.L665:
	addl	%r12d, %esi
	movslq	%esi, %r8
	cmpb	$0, (%r15,%r8)
	jne	.L359
.L547:
	addq	$7, %rax
	cmpl	%eax, %r9d
	jg	.L362
	jmp	.L357
.L600:
	testl	%edi, %edi
	.p2align 4,,2
	je	.L392
	cmpl	$1, %edi
	.p2align 4,,6
	je	.L627
	cmpl	$2, %edi
	.p2align 4,,3
	je	.L628
	cmpl	$3, %edi
	.p2align 4,,2
	je	.L629
	cmpl	$4, %edi
	.p2align 4,,2
	je	.L630
	cmpl	$5, %edi
	.p2align 4,,2
	je	.L631
	cmpl	$6, %edi
	.p2align 4,,2
	je	.L632
	movl	$0, 0(%r13,%r8,4)
	addq	$1, %r8
.L632:
	movl	$0, 0(%r13,%r8,4)
	addq	$1, %r8
.L631:
	movl	$0, 0(%r13,%r8,4)
	addq	$1, %r8
.L630:
	movl	$0, 0(%r13,%r8,4)
	addq	$1, %r8
.L629:
	movl	$0, 0(%r13,%r8,4)
	addq	$1, %r8
.L628:
	movl	$0, 0(%r13,%r8,4)
	addq	$1, %r8
.L627:
	movl	$0, 0(%r13,%r8,4)
	addq	$1, %r8
	cmpl	%r8d, 8(%rsp)
	jg	.L392
	jmp	.L650
.L583:
	movl	12(%rbp), %eax
	cmpl	%eax, %ecx
	jge	.L348
	imull	8(%rsp), %ecx
	movslq	%ecx, %rsi
	addq	16(%rbp), %rsi
	movq	%rbx, %rdx
	movq	%r13, %rdi
	call	memmove@PLT
	jmp	.L666
.L649:
	movq	%rbx, %rdx
	movl	$0, %esi
	movq	%rax, %rdi
	call	memset@PLT
	movq	32(%rsp), %rdx
	movb	$1, 0(%r13,%rdx)
.L666:
	movq	%rbx, 32(%rsp)
	addq	%rbx, %r13
	addq	$1, %r12
	movl	8(%rsp), %edi
	cmpl	%r12d, %edi
	jle	.L667
	movl	24(%rsp), %r8d
	testl	%r8d, %r8d
	je	.L711
	cmpl	$1, %r8d
	je	.L634
	cmpl	$2, %r8d
	je	.L635
	movq	%r12, 24(%rsp)
	movl	(%r14,%r12,4), %ecx
	cmpl	%ecx, %edi
	jg	.L668
	movl	12(%rbp), %eax
	cmpl	%eax, %ecx
	jge	.L348
	imull	%edi, %ecx
	movslq	%ecx, %rsi
	addq	16(%rbp), %rsi
	movq	%rbx, %rdx
	movq	%r13, %rdi
	call	memmove@PLT
	jmp	.L669
.L668:
	movq	%rbx, %rdx
	movl	$0, %esi
	movq	%r13, %rdi
	call	memset@PLT
	movq	24(%rsp), %r9
	movb	$1, 0(%r13,%r9)
.L669:
	addq	%rbx, %r13
	addq	$1, %r12
.L635:
	movq	%r12, 24(%rsp)
	movl	(%r14,%r12,4), %ecx
	movl	8(%rsp), %r11d
	cmpl	%ecx, %r11d
	jg	.L670
	movl	12(%rbp), %eax
	cmpl	%eax, %ecx
	jge	.L348
	imull	%r11d, %ecx
	movslq	%ecx, %rsi
	addq	16(%rbp), %rsi
	movq	%rbx, %rdx
	movq	%r13, %rdi
	call	memmove@PLT
	jmp	.L671
.L670:
	movq	%rbx, %rdx
	movl	$0, %esi
	movq	%r13, %rdi
	call	memset@PLT
	movq	24(%rsp), %rax
	movb	$1, 0(%r13,%rax)
.L671:
	addq	%rbx, %r13
	addq	$1, %r12
.L634:
	movq	%r12, 24(%rsp)
	movl	(%r14,%r12,4), %ecx
	movl	8(%rsp), %r10d
	cmpl	%ecx, %r10d
	jg	.L672
	movl	12(%rbp), %eax
	cmpl	%eax, %ecx
	jge	.L348
	imull	%r10d, %ecx
	movslq	%ecx, %rsi
	addq	16(%rbp), %rsi
	movq	%rbx, %rdx
	movq	%r13, %rdi
	call	memmove@PLT
	jmp	.L673
.L672:
	movq	%rbx, %rdx
	movl	$0, %esi
	movq	%r13, %rdi
	call	memset@PLT
	movq	24(%rsp), %rsi
	movb	$1, 0(%r13,%rsi)
.L673:
	movq	%rbx, 32(%rsp)
	addq	%rbx, %r13
	addq	$1, %r12
	cmpl	%r12d, 8(%rsp)
	jg	.L712
	jmp	.L667
.L594:
	movl	12(%rbp), %eax
	cmpl	%eax, %ecx
	jge	.L348
	imull	%r13d, %ecx
	movslq	%ecx, %rsi
	addq	16(%rbp), %rsi
	movq	%rbx, %rdx
	movq	24(%rsp), %rdi
	call	memmove@PLT
	jmp	.L674
.L636:
	movq	%rbx, %rdx
	movl	$0, %esi
	movq	24(%rsp), %rdi
	call	memset@PLT
	movq	24(%rsp), %rdi
	movb	$1, 1(%rdi,%r12)
.L674:
	movq	24(%rsp), %r12
	addq	%rbx, %r12
	leaq	1(%r15), %r8
	movl	(%r14,%r8,4), %ecx
	cmpl	%ecx, %r13d
	jg	.L675
	movl	12(%rbp), %eax
	cmpl	%eax, %ecx
	jge	.L348
	imull	%r13d, %ecx
	movslq	%ecx, %rsi
	addq	16(%rbp), %rsi
	movq	%rbx, %rdx
	movq	%r12, %rdi
	call	memmove@PLT
	jmp	.L676
.L675:
	movq	%rbx, %rdx
	movl	$0, %esi
	movq	%r12, %rdi
	call	memset@PLT
	movb	$1, 1(%r12,%r15)
.L676:
	addq	%rbx, %r12
	leaq	2(%r15), %r11
	movl	(%r14,%r11,4), %ecx
	cmpl	%ecx, %r13d
	jg	.L677
	movl	12(%rbp), %eax
	cmpl	%eax, %ecx
	jge	.L348
	imull	%r13d, %ecx
	movslq	%ecx, %rsi
	addq	16(%rbp), %rsi
	movq	%rbx, %rdx
	movq	%r12, %rdi
	call	memmove@PLT
	jmp	.L678
.L677:
	movq	%rbx, %rdx
	movl	$0, %esi
	movq	%r12, %rdi
	call	memset@PLT
	movb	$1, 2(%r12,%r15)
.L678:
	leaq	(%r12,%rbx), %rcx
	movq	%rcx, 24(%rsp)
	leaq	3(%r15), %r12
	cmpl	%r12d, %r13d
	jg	.L391
	movl	32(%rsp), %r15d
	movq	%rbx, 32(%rsp)
	jmp	.L667
	.cfi_endproc
.LFE74:
	.size	fec_decode, .-fec_decode
	.local	fec_initialized
	.comm	fec_initialized,4,4
	.local	gf_mul_table
	.comm	gf_mul_table,65536,32
	.local	inverse
	.comm	inverse,256,32
	.local	gf_log
	.comm	gf_log,1024,32
	.local	gf_exp
	.comm	gf_exp,510,32
	.ident	"GCC: (Ubuntu/Linaro 4.8.1-10ubuntu8) 4.8.1"
	.section	.note.GNU-stack,"",@progbits
