	.file	"fec.c"
	.text
	.type	addmul1, @function
addmul1:
.LFB64:
	.cfi_startproc
	movslq	%ecx, %rcx
	leaq	(%rdi,%rcx,2), %r10
	movzwl	%dx, %edx
	leaq	gf_log(%rip), %rax
	movslq	(%rax,%rdx,4), %r8
	leaq	gf_exp(%rip), %r9
	leaq	(%r9,%r8,2), %r11
	cmpq	%r10, %rdi
	jae	.L1
	movq	%rdi, %rcx
	notq	%rcx
	addq	%r10, %rcx
	shrq	%rcx
	andl	$7, %ecx
	movzwl	(%rsi), %edx
	testw	%dx, %dx
	je	.L6
	jmp	.L47
.L4:
	movzwl	(%rsi), %ecx
	testw	%cx, %cx
	je	.L3
	movzwl	%cx, %eax
	leaq	gf_log(%rip), %r8
	movslq	(%r8,%rax,4), %r9
	movzwl	(%r11,%r9,2), %edx
	xorw	%dx, (%rdi)
.L3:
	addq	$2, %rdi
	leaq	2(%rsi), %rax
	movzwl	2(%rsi), %esi
	testw	%si, %si
	je	.L21
	jmp	.L48
.L1:
	rep ret
.L47:
	movzwl	%dx, %eax
	leaq	gf_log(%rip), %r8
	movslq	(%r8,%rax,4), %r9
	movzwl	(%r11,%r9,2), %edx
	xorw	%dx, (%rdi)
.L6:
	addq	$2, %rdi
	addq	$2, %rsi
	cmpq	%rdi, %r10
	jbe	.L1
	testq	%rcx, %rcx
	je	.L4
	cmpq	$1, %rcx
	je	.L41
	cmpq	$2, %rcx
	je	.L42
	cmpq	$3, %rcx
	je	.L43
	cmpq	$4, %rcx
	je	.L44
	cmpq	$5, %rcx
	je	.L45
	cmpq	$6, %rcx
	je	.L46
	movzwl	(%rsi), %ecx
	testw	%cx, %cx
	je	.L7
	movzwl	%cx, %eax
	leaq	gf_log(%rip), %r8
	movslq	(%r8,%rax,4), %r9
	movzwl	(%r11,%r9,2), %edx
	xorw	%dx, (%rdi)
.L7:
	addq	$2, %rdi
	addq	$2, %rsi
.L46:
	movzwl	(%rsi), %ecx
	testw	%cx, %cx
	je	.L9
	movzwl	%cx, %eax
	leaq	gf_log(%rip), %r8
	movslq	(%r8,%rax,4), %r9
	movzwl	(%r11,%r9,2), %edx
	xorw	%dx, (%rdi)
.L9:
	addq	$2, %rdi
	addq	$2, %rsi
.L45:
	movzwl	(%rsi), %ecx
	testw	%cx, %cx
	je	.L11
	movzwl	%cx, %eax
	leaq	gf_log(%rip), %r8
	movslq	(%r8,%rax,4), %r9
	movzwl	(%r11,%r9,2), %edx
	xorw	%dx, (%rdi)
.L11:
	addq	$2, %rdi
	addq	$2, %rsi
.L44:
	movzwl	(%rsi), %ecx
	testw	%cx, %cx
	je	.L13
	movzwl	%cx, %eax
	leaq	gf_log(%rip), %r8
	movslq	(%r8,%rax,4), %r9
	movzwl	(%r11,%r9,2), %edx
	xorw	%dx, (%rdi)
.L13:
	addq	$2, %rdi
	addq	$2, %rsi
.L43:
	movzwl	(%rsi), %ecx
	testw	%cx, %cx
	je	.L15
	movzwl	%cx, %eax
	leaq	gf_log(%rip), %r8
	movslq	(%r8,%rax,4), %r9
	movzwl	(%r11,%r9,2), %edx
	xorw	%dx, (%rdi)
.L15:
	addq	$2, %rdi
	addq	$2, %rsi
.L42:
	movzwl	(%rsi), %ecx
	testw	%cx, %cx
	je	.L17
	movzwl	%cx, %eax
	leaq	gf_log(%rip), %r8
	movslq	(%r8,%rax,4), %r9
	movzwl	(%r11,%r9,2), %edx
	xorw	%dx, (%rdi)
.L17:
	addq	$2, %rdi
	addq	$2, %rsi
.L41:
	movzwl	(%rsi), %ecx
	testw	%cx, %cx
	je	.L19
	movzwl	%cx, %eax
	leaq	gf_log(%rip), %r8
	movslq	(%r8,%rax,4), %r9
	movzwl	(%r11,%r9,2), %edx
	xorw	%dx, (%rdi)
.L19:
	addq	$2, %rdi
	addq	$2, %rsi
	cmpq	%rdi, %r10
	ja	.L4
	rep ret
.L48:
	movzwl	%si, %r8d
	leaq	gf_log(%rip), %r9
	movslq	(%r9,%r8,4), %rdx
	movzwl	(%r11,%rdx,2), %esi
	xorw	%si, (%rdi)
.L21:
	leaq	2(%rdi), %rcx
	movzwl	2(%rax), %r8d
	testw	%r8w, %r8w
	je	.L22
	movzwl	%r8w, %edx
	leaq	gf_log(%rip), %r9
	movslq	(%r9,%rdx,4), %rsi
	movzwl	(%r11,%rsi,2), %r8d
	xorw	%r8w, (%rcx)
.L22:
	leaq	4(%rdi), %rcx
	movzwl	4(%rax), %edx
	testw	%dx, %dx
	je	.L23
	movzwl	%dx, %esi
	leaq	gf_log(%rip), %r9
	movslq	(%r9,%rsi,4), %r8
	movzwl	(%r11,%r8,2), %edx
	xorw	%dx, (%rcx)
.L23:
	leaq	6(%rdi), %rcx
	movzwl	6(%rax), %esi
	testw	%si, %si
	je	.L24
	movzwl	%si, %r8d
	leaq	gf_log(%rip), %r9
	movslq	(%r9,%r8,4), %rdx
	movzwl	(%r11,%rdx,2), %esi
	xorw	%si, (%rcx)
.L24:
	leaq	8(%rdi), %rcx
	movzwl	8(%rax), %r8d
	testw	%r8w, %r8w
	je	.L25
	movzwl	%r8w, %edx
	leaq	gf_log(%rip), %r9
	movslq	(%r9,%rdx,4), %rsi
	movzwl	(%r11,%rsi,2), %r8d
	xorw	%r8w, (%rcx)
.L25:
	leaq	10(%rdi), %rcx
	movzwl	10(%rax), %edx
	testw	%dx, %dx
	je	.L26
	movzwl	%dx, %esi
	leaq	gf_log(%rip), %r9
	movslq	(%r9,%rsi,4), %r8
	movzwl	(%r11,%r8,2), %edx
	xorw	%dx, (%rcx)
.L26:
	leaq	12(%rdi), %rcx
	movzwl	12(%rax), %esi
	testw	%si, %si
	je	.L27
	movzwl	%si, %r8d
	leaq	gf_log(%rip), %r9
	movslq	(%r9,%r8,4), %rdx
	movzwl	(%r11,%rdx,2), %esi
	xorw	%si, (%rcx)
.L27:
	addq	$14, %rdi
	leaq	14(%rax), %rsi
	cmpq	%rdi, %r10
	ja	.L4
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
	jne	.L50
	movq	%rbx, %rcx
	leaq	.LC0(%rip), %rdx
	movl	$1, %esi
	movq	stderr@GOTPCREL(%rip), %rax
	movq	(%rax), %rdi
	movl	$0, %eax
	call	__fprintf_chk@PLT
	movl	$1, %edi
	call	exit@PLT
.L50:
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
	subq	$56, %rsp
	.cfi_def_cfa_offset 112
	movl	%esi, %r13d
	cmpl	$1, %esi
	je	.L77
	movq	%rdi, %r12
	movl	%esi, 36(%rsp)
	leal	(%rsi,%rsi), %r14d
	leaq	.LC1(%rip), %rsi
	movl	%r14d, %edi
	call	my_malloc
	movq	%rax, %rbp
	leaq	.LC1(%rip), %rsi
	movl	%r14d, %edi
	call	my_malloc
	movq	%rax, %rbx
	leaq	.LC1(%rip), %rsi
	movl	%r14d, %edi
	call	my_malloc
	movq	%rax, %rsi
	movq	%rax, (%rsp)
	testl	%r13d, %r13d
	jle	.L54
	movl	$0, %eax
	leal	-1(%r13), %ecx
	andl	$7, %ecx
	movw	$0, 0(%rbp,%rax,2)
	movzwl	2(%r12), %edx
	movw	%dx, (%rsi,%rax,2)
	leal	1(%r13), %r8d
	addq	$1, %rax
	cmpl	%eax, %r13d
	jg	.L140
	jmp	.L54
.L180:
	movq	(%rsp), %r11
	jmp	.L55
.L55:
	movw	$0, 0(%rbp,%rax,2)
	movslq	%r8d, %r15
	movzwl	(%r12,%r15,2), %r14d
	movw	%r14w, (%r11,%rax,2)
	addl	%r13d, %r8d
	addq	$1, %rax
	movw	$0, 0(%rbp,%rax,2)
	movslq	%r8d, %rsi
	movzwl	(%r12,%rsi,2), %ecx
	movw	%cx, (%r11,%rax,2)
	addl	%r13d, %r8d
	leaq	1(%rax), %rdx
	movw	$0, 0(%rbp,%rdx,2)
	movslq	%r8d, %rdi
	movzwl	(%r12,%rdi,2), %r9d
	movw	%r9w, (%r11,%rdx,2)
	addl	%r13d, %r8d
	leaq	2(%rax), %r10
	movw	$0, 0(%rbp,%r10,2)
	movslq	%r8d, %r15
	movzwl	(%r12,%r15,2), %r14d
	movw	%r14w, (%r11,%r10,2)
	addl	%r13d, %r8d
	leaq	3(%rax), %rsi
	movw	$0, 0(%rbp,%rsi,2)
	movslq	%r8d, %rcx
	movzwl	(%r12,%rcx,2), %edx
	movw	%dx, (%r11,%rsi,2)
	addl	%r13d, %r8d
	leaq	4(%rax), %r9
	movw	$0, 0(%rbp,%r9,2)
	movslq	%r8d, %rdi
	movzwl	(%r12,%rdi,2), %r10d
	movw	%r10w, (%r11,%r9,2)
	addl	%r13d, %r8d
	leaq	5(%rax), %r15
	movw	$0, 0(%rbp,%r15,2)
	movslq	%r8d, %r14
	movzwl	(%r12,%r14,2), %esi
	movw	%si, (%r11,%r15,2)
	addl	%r13d, %r8d
	leaq	6(%rax), %rcx
	movw	$0, 0(%rbp,%rcx,2)
	movslq	%r8d, %rdx
	movzwl	(%r12,%rdx,2), %r9d
	movw	%r9w, (%r11,%rcx,2)
	addl	%r13d, %r8d
	addq	$7, %rax
	cmpl	%eax, %r13d
	jg	.L55
.L54:
	movslq	%r13d, %rax
	leaq	-2(%rax,%rax), %r8
	movq	%r8, 16(%rsp)
	addq	%rbp, %r8
	movq	%r8, %r11
	movq	(%rsp), %rdi
	movzwl	(%rdi), %r10d
	movw	%r10w, (%r8)
	cmpl	$1, %r13d
	jg	.L56
	jmp	.L57
.L61:
	movzwl	(%r9), %r8d
	movl	%esi, %edi
	cmpl	%esi, %eax
	jle	.L58
	movzwl	%r8w, %edx
	movslq	%edx, %rcx
	movl	%esi, %r14d
	subl	%eax, %r14d
	notl	%r14d
	andl	$1, %r14d
	movslq	%esi, %r15
	addq	%r15, %r15
	leaq	0(%rbp,%r15), %r10
	movzwl	(%r10), %r12d
	movzwl	2(%rbp,%r15), %r11d
	testl	%r11d, %r11d
	je	.L105
	jmp	.L141
.L178:
	movl	8(%rsp), %r13d
	jmp	.L60
.L60:
	movslq	%edi, %r14
	addq	%r14, %r14
	leaq	0(%rbp,%r14), %r10
	movzwl	(%r10), %r12d
	movzwl	2(%rbp,%r14), %r11d
	testl	%r11d, %r11d
	je	.L72
	testl	%edx, %edx
	je	.L72
	leaq	gf_log(%rip), %r14
	movslq	%r11d, %r11
	movl	(%r14,%rcx,4), %r15d
	addl	(%r14,%r11,4), %r15d
	movslq	%r15d, %r15
	leaq	gf_exp(%rip), %r11
	movzwl	(%r11,%r15,2), %r14d
	jmp	.L59
.L72:
	movl	$0, %r14d
.L59:
	xorl	%r12d, %r14d
	movw	%r14w, (%r10)
	addl	$1, %edi
	movslq	%edi, %r15
	addq	%r15, %r15
	leaq	0(%rbp,%r15), %r10
	movzwl	(%r10), %r12d
	movzwl	2(%rbp,%r15), %r11d
	testl	%r11d, %r11d
	je	.L108
	jmp	.L142
.L58:
	addq	$2, %r9
	subl	$1, %esi
.L71:
	xorw	%r8w, (%rbx)
	testl	%esi, %esi
	jne	.L61
	movq	24(%rsp), %rbx
	movq	40(%rsp), %r12
	movl	8(%rsp), %r13d
.L57:
	testl	%r13d, %r13d
	jle	.L62
	movl	$0, %r10d
	leal	-2(%r13), %r14d
	movl	%r14d, 24(%rsp)
	movl	36(%rsp), %esi
.L69:
	movq	(%rsp), %rcx
	movzwl	(%rcx,%r10,2), %edx
	movq	16(%rsp), %r11
	movw	$1, (%rbx,%r11)
	movl	24(%rsp), %edi
	movl	%edi, %eax
	testl	%edi, %edi
	js	.L73
	movzwl	%dx, %ecx
	movslq	%ecx, %rdi
	movl	%eax, %r15d
	andl	$1, %r15d
	movl	%r15d, 8(%rsp)
	movslq	%eax, %r8
	addq	%r8, %r8
	leaq	(%rbx,%r8), %r11
	movzwl	2(%rbp,%r8), %edx
	movzwl	2(%rbx,%r8), %r9d
	testl	%ecx, %ecx
	sete	%r8b
	testl	%r9d, %r9d
	je	.L96
	jmp	.L143
.L176:
	movl	%esi, 8(%rsp)
	jmp	.L66
.L177:
	movl	%esi, 8(%rsp)
.L66:
	movslq	%eax, %rsi
	addq	%rsi, %rsi
	leaq	(%rbx,%rsi), %r9
	movzwl	2(%rbp,%rsi), %r11d
	movzwl	2(%rbx,%rsi), %r15d
	testl	%ecx, %ecx
	sete	%r8b
	testl	%r15d, %r15d
	je	.L74
	testb	%r8b, %r8b
	jne	.L74
	leaq	gf_log(%rip), %r14
	movslq	%r15d, %r15
	movl	(%r14,%rdi,4), %esi
	addl	(%r14,%r15,4), %esi
	movslq	%esi, %r15
	leaq	gf_exp(%rip), %rsi
	movzwl	(%rsi,%r15,2), %r14d
	jmp	.L64
.L74:
	movl	$0, %r14d
.L64:
	xorl	%r14d, %r11d
	movw	%r11w, (%r9)
	movzwl	%dx, %edx
	testb	%r8b, %r8b
	jne	.L75
	testl	%edx, %edx
	je	.L75
	leaq	gf_log(%rip), %r8
	movslq	%edx, %r15
	movl	(%r8,%rdi,4), %r9d
	addl	(%r8,%r15,4), %r9d
	movslq	%r9d, %r14
	leaq	gf_exp(%rip), %rdx
	movzwl	(%rdx,%r14,2), %r8d
	jmp	.L65
.L75:
	movl	$0, %r8d
.L65:
	xorl	%r8d, %r11d
	subl	$1, %eax
	movslq	%eax, %rsi
	addq	%rsi, %rsi
	leaq	(%rbx,%rsi), %r9
	movzwl	2(%rbp,%rsi), %edx
	movzwl	2(%rbx,%rsi), %r15d
	testl	%ecx, %ecx
	sete	%r8b
	testl	%r15d, %r15d
	je	.L101
	jmp	.L144
.L70:
	movslq	%edx, %r9
	leaq	(%r12,%r9,2), %rax
	movzwl	(%rbx,%rdi,2), %r8d
	testl	%r8d, %r8d
	je	.L76
	testb	%r15b, %r15b
	jne	.L76
	leaq	gf_log(%rip), %r11
	movslq	%r8d, %r9
	movl	(%r11,%rcx,4), %r14d
	addl	(%r11,%r9,4), %r14d
	movslq	%r14d, %r11
	leaq	gf_exp(%rip), %r9
	movzwl	(%r9,%r11,2), %r14d
	jmp	.L67
.L76:
	movl	$0, %r14d
.L67:
	movw	%r14w, (%rax)
	addq	$1, %rdi
	addl	%esi, %edx
	movslq	%edx, %rax
	leaq	(%r12,%rax,2), %r9
	movzwl	(%rbx,%rdi,2), %r8d
	testl	%r8d, %r8d
	je	.L89
	jmp	.L145
.L148:
	addq	$1, %r10
	cmpl	%r10d, %r13d
	jg	.L69
.L62:
	movq	%rbp, %rdi
	call	free@PLT
	movq	%rbx, %rdi
	call	free@PLT
	movq	(%rsp), %rdi
	call	free@PLT
	jmp	.L77
.L73:
	movl	$1, %edx
.L63:
	movzwl	%dx, %ecx
	leaq	inverse(%rip), %rax
	movzwl	(%rax,%rcx,2), %edi
	testl	%edi, %edi
	sete	%r15b
	movl	%r10d, %edx
	movslq	%edi, %rcx
	leal	-1(%r13), %r8d
	andl	$3, %r8d
	movslq	%r10d, %r9
	leaq	(%r12,%r9,2), %rdi
	movzwl	(%rbx), %eax
	testl	%eax, %eax
	je	.L78
	jmp	.L146
.L56:
	movq	(%rsp), %r15
	movzwl	2(%r15), %r8d
	addq	$4, %r15
	movq	%r15, %r9
	leal	-2(%r13), %esi
	leal	-1(%r13), %eax
	movq	%rbx, 24(%rsp)
	movq	%r11, %rbx
	movq	%r12, 40(%rsp)
	movl	%r13d, 8(%rsp)
	jmp	.L71
.L77:
	movl	$0, %eax
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
.L78:
	.cfi_restore_state
	movl	$0, %r14d
	jmp	.L147
.L146:
	testb	%r15b, %r15b
	jne	.L78
	leaq	gf_log(%rip), %r11
	cltq
	movl	(%r11,%rcx,4), %r14d
	addl	(%r11,%rax,4), %r14d
	movslq	%r14d, %r11
	leaq	gf_exp(%rip), %rax
	movzwl	(%rax,%r11,2), %r14d
.L147:
	movw	%r14w, (%rdi)
	movl	$0, %edi
	addq	$1, %rdi
	addl	%esi, %edx
	cmpl	%edi, %r13d
	jle	.L148
	testl	%r8d, %r8d
	je	.L70
	cmpl	$1, %r8d
	je	.L132
	cmpl	$2, %r8d
	je	.L133
	movslq	%edx, %r8
	leaq	(%r12,%r8,2), %r9
	movzwl	(%rbx,%rdi,2), %eax
	testl	%eax, %eax
	jne	.L149
.L80:
	movl	$0, %r14d
	jmp	.L150
.L149:
	testb	%r15b, %r15b
	jne	.L80
	leaq	gf_log(%rip), %r11
	movslq	%eax, %r8
	movl	(%r11,%rcx,4), %r14d
	addl	(%r11,%r8,4), %r14d
	movslq	%r14d, %r11
	leaq	gf_exp(%rip), %r8
	movzwl	(%r8,%r11,2), %r14d
.L150:
	movw	%r14w, (%r9)
	addq	$1, %rdi
	addl	%esi, %edx
.L133:
	movslq	%edx, %r9
	leaq	(%r12,%r9,2), %rax
	movzwl	(%rbx,%rdi,2), %r8d
	testl	%r8d, %r8d
	jne	.L151
.L83:
	movl	$0, %r14d
	jmp	.L152
.L151:
	testb	%r15b, %r15b
	jne	.L83
	leaq	gf_log(%rip), %r11
	movslq	%r8d, %r9
	movl	(%r11,%rcx,4), %r14d
	addl	(%r11,%r9,4), %r14d
	movslq	%r14d, %r11
	leaq	gf_exp(%rip), %r9
	movzwl	(%r9,%r11,2), %r14d
.L152:
	movw	%r14w, (%rax)
	addq	$1, %rdi
	addl	%esi, %edx
.L132:
	movslq	%edx, %rax
	leaq	(%r12,%rax,2), %r9
	movzwl	(%rbx,%rdi,2), %r8d
	testl	%r8d, %r8d
	jne	.L153
.L86:
	movl	$0, %r14d
	jmp	.L154
.L153:
	testb	%r15b, %r15b
	jne	.L86
	leaq	gf_log(%rip), %r11
	movslq	%r8d, %rax
	movl	(%r11,%rcx,4), %r14d
	addl	(%r11,%rax,4), %r14d
	movslq	%r14d, %r11
	leaq	gf_exp(%rip), %rax
	movzwl	(%rax,%r11,2), %r14d
.L154:
	movw	%r14w, (%r9)
	addq	$1, %rdi
	addl	%esi, %edx
	cmpl	%edi, %r13d
	jg	.L70
	jmp	.L148
.L89:
	movl	$0, %r14d
	jmp	.L155
.L145:
	testb	%r15b, %r15b
	.p2align 4,,2
	jne	.L89
	leaq	gf_log(%rip), %r11
	movslq	%r8d, %rax
	movl	(%r11,%rcx,4), %r14d
	addl	(%r11,%rax,4), %r14d
	movslq	%r14d, %r11
	leaq	gf_exp(%rip), %rax
	movzwl	(%rax,%r11,2), %r14d
.L155:
	movw	%r14w, (%r9)
	leaq	1(%rdi), %r8
	addl	%esi, %edx
	movslq	%edx, %r9
	leaq	(%r12,%r9,2), %rax
	movzwl	(%rbx,%r8,2), %r14d
	testl	%r14d, %r14d
	jne	.L156
.L91:
	movl	$0, %r9d
	jmp	.L157
.L156:
	testb	%r15b, %r15b
	jne	.L91
	leaq	gf_log(%rip), %r11
	movslq	%r14d, %r8
	movl	(%r11,%rcx,4), %r9d
	addl	(%r11,%r8,4), %r9d
	movslq	%r9d, %r11
	leaq	gf_exp(%rip), %r8
	movzwl	(%r8,%r11,2), %r9d
.L157:
	movw	%r9w, (%rax)
	leaq	2(%rdi), %rax
	addl	%esi, %edx
	movslq	%edx, %r14
	leaq	(%r12,%r14,2), %r9
	movzwl	(%rbx,%rax,2), %r8d
	testl	%r8d, %r8d
	jne	.L158
.L93:
	movl	$0, %r14d
	jmp	.L159
.L158:
	testb	%r15b, %r15b
	jne	.L93
	leaq	gf_log(%rip), %r11
	movslq	%r8d, %rax
	movl	(%r11,%rcx,4), %r14d
	addl	(%r11,%rax,4), %r14d
	movslq	%r14d, %r11
	leaq	gf_exp(%rip), %rax
	movzwl	(%rax,%r11,2), %r14d
.L159:
	movw	%r14w, (%r9)
	addq	$3, %rdi
	addl	%esi, %edx
	cmpl	%edi, %r13d
	jg	.L70
	jmp	.L148
.L96:
	movl	$0, %r15d
	jmp	.L160
.L143:
	testb	%r8b, %r8b
	.p2align 4,,2
	jne	.L96
	leaq	gf_log(%rip), %r15
	movslq	%r9d, %r9
	movl	(%r15,%rdi,4), %r14d
	addl	(%r15,%r9,4), %r14d
	movslq	%r14d, %r14
	leaq	gf_exp(%rip), %r9
	movzwl	(%r9,%r14,2), %r15d
.L160:
	xorl	%r15d, %edx
	movw	%dx, (%r11)
	movl	$1, %r11d
	testb	%r8b, %r8b
	je	.L161
.L97:
	movl	$0, %r14d
	jmp	.L162
.L161:
	testl	%r11d, %r11d
	je	.L97
	leaq	gf_log(%rip), %r8
	movslq	%r11d, %r14
	movl	(%r8,%rdi,4), %r9d
	addl	(%r8,%r14,4), %r9d
	movslq	%r9d, %r11
	leaq	gf_exp(%rip), %r8
	movzwl	(%r8,%r11,2), %r14d
.L162:
	xorl	%r14d, %edx
	subl	$1, %eax
	cmpl	$-1, %eax
	je	.L63
	cmpl	$0, 8(%rsp)
	je	.L176
	movslq	%eax, %r9
	addq	%r9, %r9
	leaq	(%rbx,%r9), %r15
	movq	%r15, 8(%rsp)
	movzwl	2(%rbp,%r9), %r14d
	movzwl	2(%rbx,%r9), %r11d
	testl	%ecx, %ecx
	sete	%r8b
	testl	%r11d, %r11d
	jne	.L163
.L98:
	movl	$0, %r15d
	jmp	.L164
.L163:
	testb	%r8b, %r8b
	jne	.L98
	leaq	gf_log(%rip), %r15
	movslq	%r11d, %r9
	movl	(%r15,%rdi,4), %r11d
	addl	(%r15,%r9,4), %r11d
	movslq	%r11d, %r11
	leaq	gf_exp(%rip), %r9
	movzwl	(%r9,%r11,2), %r15d
.L164:
	xorl	%r15d, %r14d
	movq	8(%rsp), %r11
	movw	%r14w, (%r11)
	movzwl	%dx, %edx
	testb	%r8b, %r8b
	je	.L165
.L99:
	movl	$0, %edx
	jmp	.L166
.L165:
	testl	%edx, %edx
	je	.L99
	leaq	gf_log(%rip), %r8
	movslq	%edx, %r15
	movl	(%r8,%rdi,4), %r9d
	addl	(%r8,%r15,4), %r9d
	movslq	%r9d, %r11
	leaq	gf_exp(%rip), %rdx
	movzwl	(%rdx,%r11,2), %edx
.L166:
	xorl	%r14d, %edx
	subl	$1, %eax
	cmpl	$-1, %eax
	jne	.L177
	jmp	.L63
.L101:
	movl	$0, %r14d
	jmp	.L167
.L144:
	testb	%r8b, %r8b
	.p2align 4,,2
	jne	.L101
	leaq	gf_log(%rip), %r14
	movslq	%r15d, %r15
	movl	(%r14,%rdi,4), %esi
	addl	(%r14,%r15,4), %esi
	movslq	%esi, %r15
	leaq	gf_exp(%rip), %rsi
	movzwl	(%rsi,%r15,2), %r14d
.L167:
	xorl	%r14d, %edx
	movw	%dx, (%r9)
	movzwl	%r11w, %r11d
	testb	%r8b, %r8b
	je	.L168
.L102:
	movl	$0, %r11d
	jmp	.L169
.L168:
	testl	%r11d, %r11d
	je	.L102
	leaq	gf_log(%rip), %r8
	movslq	%r11d, %r15
	movl	(%r8,%rdi,4), %r9d
	addl	(%r8,%r15,4), %r9d
	movslq	%r9d, %rsi
	leaq	gf_exp(%rip), %r14
	movzwl	(%r14,%rsi,2), %r11d
.L169:
	xorl	%r11d, %edx
	subl	$1, %eax
	cmpl	$-1, %eax
	jne	.L66
	movl	8(%rsp), %esi
	jmp	.L63
.L105:
	movl	$0, %r15d
	jmp	.L170
.L141:
	testl	%edx, %edx
	je	.L105
	leaq	gf_log(%rip), %r15
	movslq	%r11d, %r11
	movl	(%r15,%rcx,4), %r13d
	addl	(%r15,%r11,4), %r13d
	movslq	%r13d, %r13
	leaq	gf_exp(%rip), %r11
	movzwl	(%r11,%r13,2), %r15d
.L170:
	xorl	%r15d, %r12d
	movw	%r12w, (%r10)
	addl	$1, %edi
	cmpl	%edi, %eax
	je	.L58
	testl	%r14d, %r14d
	je	.L178
	movslq	%edi, %r14
	addq	%r14, %r14
	leaq	0(%rbp,%r14), %r10
	movzwl	(%r10), %r12d
	movzwl	2(%rbp,%r14), %r13d
	testl	%r13d, %r13d
	jne	.L171
.L106:
	movl	$0, %r15d
	jmp	.L172
.L171:
	testl	%edx, %edx
	je	.L106
	leaq	gf_log(%rip), %r14
	movslq	%r13d, %r11
	movl	(%r14,%rcx,4), %r15d
	addl	(%r14,%r11,4), %r15d
	movslq	%r15d, %r14
	leaq	gf_exp(%rip), %r11
	movzwl	(%r11,%r14,2), %r15d
.L172:
	xorl	%r15d, %r12d
	movw	%r12w, (%r10)
	addl	$1, %edi
	movl	8(%rsp), %r13d
	cmpl	%edi, %eax
	jne	.L60
	jmp	.L58
.L108:
	movl	$0, %r14d
	jmp	.L173
.L142:
	testl	%edx, %edx
	.p2align 4,,3
	je	.L108
	leaq	gf_log(%rip), %r14
	movslq	%r11d, %r11
	movl	(%r14,%rcx,4), %r15d
	addl	(%r14,%r11,4), %r15d
	movslq	%r15d, %r15
	leaq	gf_exp(%rip), %r11
	movzwl	(%r11,%r15,2), %r14d
.L173:
	xorl	%r12d, %r14d
	movw	%r14w, (%r10)
	addl	$1, %edi
	cmpl	%edi, %eax
	jne	.L60
	movl	%r13d, 8(%rsp)
	jmp	.L58
.L140:
	testl	%ecx, %ecx
	je	.L180
	cmpl	$1, %ecx
	je	.L134
	cmpl	$2, %ecx
	.p2align 4,,3
	je	.L135
	cmpl	$3, %ecx
	.p2align 4,,2
	je	.L136
	cmpl	$4, %ecx
	.p2align 4,,2
	je	.L137
	cmpl	$5, %ecx
	.p2align 4,,2
	je	.L138
	cmpl	$6, %ecx
	.p2align 4,,2
	je	.L139
	movw	$0, 0(%rbp,%rax,2)
	movslq	%r8d, %rdi
	movzwl	(%r12,%rdi,2), %r9d
	movq	(%rsp), %r10
	movw	%r9w, (%r10,%rax,2)
	addl	%r13d, %r8d
	addq	$1, %rax
.L139:
	movw	$0, 0(%rbp,%rax,2)
	movslq	%r8d, %r11
	movzwl	(%r12,%r11,2), %r15d
	movq	(%rsp), %r14
	movw	%r15w, (%r14,%rax,2)
	addl	%r13d, %r8d
	addq	$1, %rax
.L138:
	movw	$0, 0(%rbp,%rax,2)
	movslq	%r8d, %rsi
	movzwl	(%r12,%rsi,2), %ecx
	movq	(%rsp), %rdx
	movw	%cx, (%rdx,%rax,2)
	addl	%r13d, %r8d
	addq	$1, %rax
.L137:
	movw	$0, 0(%rbp,%rax,2)
	movslq	%r8d, %rdi
	movzwl	(%r12,%rdi,2), %r9d
	movq	(%rsp), %r10
	movw	%r9w, (%r10,%rax,2)
	addl	%r13d, %r8d
	addq	$1, %rax
.L136:
	movw	$0, 0(%rbp,%rax,2)
	movslq	%r8d, %r11
	movzwl	(%r12,%r11,2), %r15d
	movq	(%rsp), %r14
	movw	%r15w, (%r14,%rax,2)
	addl	%r13d, %r8d
	addq	$1, %rax
.L135:
	movw	$0, 0(%rbp,%rax,2)
	movslq	%r8d, %rsi
	movzwl	(%r12,%rsi,2), %ecx
	movq	(%rsp), %rdx
	movw	%cx, (%rdx,%rax,2)
	addl	%r13d, %r8d
	addq	$1, %rax
.L134:
	movw	$0, 0(%rbp,%rax,2)
	movslq	%r8d, %rdi
	movzwl	(%r12,%rdi,2), %r9d
	movq	(%rsp), %r10
	movw	%r9w, (%r10,%rax,2)
	addl	%r13d, %r8d
	addq	$1, %rax
	movq	(%rsp), %r11
	cmpl	%eax, %r13d
	jg	.L55
	jmp	.L54
	.cfi_endproc
.LFE67:
	.size	invert_vdm, .-invert_vdm
	.globl	init_fec
	.type	init_fec, @function
init_fec:
.LFB68:
	.cfi_startproc
	movw	$1, gf_exp(%rip)
	movl	$0, 4+gf_log(%rip)
	movw	$2, 2+gf_exp(%rip)
	movl	$1, 8+gf_log(%rip)
	movw	$4, 4+gf_exp(%rip)
	movl	$2, 16+gf_log(%rip)
	movw	$8, 6+gf_exp(%rip)
	movl	$3, 32+gf_log(%rip)
	movw	$16, 8+gf_exp(%rip)
	movl	$4, 64+gf_log(%rip)
	movw	$32, 10+gf_exp(%rip)
	movl	$5, 128+gf_log(%rip)
	movw	$64, 12+gf_exp(%rip)
	movl	$6, 256+gf_log(%rip)
	movw	$128, 14+gf_exp(%rip)
	movl	$7, 512+gf_log(%rip)
	movw	$256, 16+gf_exp(%rip)
	movl	$8, 1024+gf_log(%rip)
	movw	$512, 18+gf_exp(%rip)
	movl	$9, 2048+gf_log(%rip)
	movw	$1024, 20+gf_exp(%rip)
	movl	$10, 4096+gf_log(%rip)
	movw	$2048, 22+gf_exp(%rip)
	movl	$11, 8192+gf_log(%rip)
	movw	$4096, 24+gf_exp(%rip)
	movl	$12, 16384+gf_log(%rip)
	movw	$4107, 32+gf_exp(%rip)
	movw	$8192, 26+gf_exp(%rip)
	movl	$13, 32768+gf_log(%rip)
	movw	$16384, 28+gf_exp(%rip)
	movl	$14, 65536+gf_log(%rip)
	movw	$-32768, 30+gf_exp(%rip)
	movl	$15, 131072+gf_log(%rip)
	movl	$16, 16428+gf_log(%rip)
	leaq	34+gf_exp(%rip), %rdx
	movl	$17, %ecx
	movl	$4107, %esi
	addl	%esi, %esi
	movw	%si, (%rdx)
	movzwl	%si, %edi
	leaq	gf_log(%rip), %r8
	movl	%ecx, (%r8,%rdi,4)
	addl	$1, %ecx
	movl	%ecx, %r9d
	addq	$2, %rdx
	movq	%rdx, %r11
	movzwl	-2(%rdx), %eax
	cmpw	$-32768, %ax
	jb	.L216
	jmp	.L229
.L187:
	movq	%rdx, %rdi
	movzwl	-2(%rdx), %r8d
	cmpw	$-32768, %r8w
	jb	.L184
	addl	%r8d, %r8d
	xorw	32+gf_exp(%rip), %r8w
	movw	%r8w, (%rdx)
	jmp	.L185
.L184:
	addl	%r8d, %r8d
	movw	%r8w, (%rdx)
.L185:
	movzwl	(%rdi), %r9d
	leaq	gf_log(%rip), %r10
	movl	%ecx, (%r10,%r9,4)
	addl	$1, %ecx
	movl	%ecx, %r11d
	leaq	2(%rdx), %rax
	movzwl	-2(%rax), %esi
	cmpw	$-32768, %si
	jb	.L218
	jmp	.L230
.L214:
	movl	$65535, gf_log(%rip)
	leaq	gf_exp(%rip), %r8
	leaq	131070+gf_exp(%rip), %rcx
.L189:
	movzwl	(%r8), %eax
	movw	%ax, 131070(%r8)
	leaq	2(%r8), %rsi
	movzwl	2(%r8), %r8d
	movw	%r8w, 131070(%rsi)
	movzwl	2(%rsi), %r9d
	movw	%r9w, 131072(%rsi)
	movzwl	4(%rsi), %r10d
	movw	%r10w, 131074(%rsi)
	movzwl	6(%rsi), %r11d
	movw	%r11w, 131076(%rsi)
	movzwl	8(%rsi), %edi
	movw	%di, 131078(%rsi)
	movzwl	10(%rsi), %edx
	movw	%dx, 131080(%rsi)
	movzwl	12(%rsi), %eax
	movw	%ax, 131082(%rsi)
	movzwl	14(%rsi), %r8d
	movw	%r8w, 131084(%rsi)
	movzwl	16(%rsi), %r9d
	movw	%r9w, 131086(%rsi)
	movzwl	18(%rsi), %r10d
	movw	%r10w, 131088(%rsi)
	movzwl	20(%rsi), %r11d
	movw	%r11w, 131090(%rsi)
	movzwl	22(%rsi), %edi
	movw	%di, 131092(%rsi)
	movzwl	24(%rsi), %edx
	movw	%dx, 131094(%rsi)
	movzwl	26(%rsi), %eax
	movw	%ax, 131096(%rsi)
	leaq	28(%rsi), %r8
	cmpq	%rcx, %r8
	jne	.L189
	movw	$0, inverse(%rip)
	movw	$1, 2+inverse(%rip)
	movl	$0, %ecx
	movl	$65535, %r9d
.L191:
	leaq	8+gf_log(%rip), %r10
	movl	%r9d, %esi
	subl	(%r10,%rcx,2), %esi
	movslq	%esi, %r11
	leaq	gf_exp(%rip), %rdi
	movzwl	(%rdi,%r11,2), %edx
	leaq	4+inverse(%rip), %rax
	movw	%dx, (%rax,%rcx)
	leaq	2(%rcx), %r8
	leaq	8+gf_log(%rip), %r10
	movl	%r9d, %esi
	subl	(%r10,%r8,2), %esi
	movslq	%esi, %r11
	leaq	gf_exp(%rip), %rdi
	movzwl	(%rdi,%r11,2), %edx
	leaq	4+inverse(%rip), %rax
	movw	%dx, 2(%rax,%rcx)
	leaq	8+gf_log(%rip), %rcx
	movl	%r9d, %r10d
	subl	4(%rcx,%r8,2), %r10d
	movslq	%r10d, %rsi
	leaq	gf_exp(%rip), %r11
	movzwl	(%r11,%rsi,2), %edi
	leaq	4+inverse(%rip), %rdx
	movw	%di, 2(%rdx,%r8)
	leaq	8+gf_log(%rip), %rax
	movl	%r9d, %ecx
	subl	8(%rax,%r8,2), %ecx
	movslq	%ecx, %r10
	leaq	gf_exp(%rip), %rsi
	movzwl	(%rsi,%r10,2), %r11d
	leaq	4+inverse(%rip), %rdi
	movw	%r11w, 4(%rdi,%r8)
	leaq	8+gf_log(%rip), %rdx
	movl	%r9d, %eax
	subl	12(%rdx,%r8,2), %eax
	movslq	%eax, %r10
	leaq	gf_exp(%rip), %rcx
	movzwl	(%rcx,%r10,2), %esi
	leaq	4+inverse(%rip), %r11
	movw	%si, 6(%r11,%r8)
	leaq	8+gf_log(%rip), %rdi
	movl	%r9d, %edx
	subl	16(%rdi,%r8,2), %edx
	movslq	%edx, %rax
	leaq	gf_exp(%rip), %r10
	movzwl	(%r10,%rax,2), %esi
	leaq	4+inverse(%rip), %rcx
	movw	%si, 8(%rcx,%r8)
	leaq	8+gf_log(%rip), %r11
	movl	%r9d, %edi
	subl	20(%r11,%r8,2), %edi
	movslq	%edi, %rdx
	leaq	gf_exp(%rip), %rax
	movzwl	(%rax,%rdx,2), %r10d
	leaq	4+inverse(%rip), %rsi
	movw	%r10w, 10(%rsi,%r8)
	leaq	12(%r8), %rcx
	cmpq	$131068, %rcx
	jne	.L191
	movl	$1, fec_initialized(%rip)
	ret
.L216:
	addl	%eax, %eax
	movw	%ax, (%rdx)
	jmp	.L231
.L229:
	addl	%eax, %eax
	xorw	32+gf_exp(%rip), %ax
	movw	%ax, (%rdx)
.L231:
	movzwl	(%r11), %esi
	leaq	gf_log(%rip), %rdi
	movl	%r9d, (%rdi,%rsi,4)
	leal	1(%rcx), %r8d
	leaq	2(%rdx), %r9
	movq	%r9, %r10
	movzwl	(%rdx), %r11d
	cmpw	$-32768, %r11w
	jae	.L232
	addl	%r11d, %r11d
	movw	%r11w, (%r9)
	jmp	.L233
.L232:
	addl	%r11d, %r11d
	xorw	32+gf_exp(%rip), %r11w
	movw	%r11w, (%r9)
.L233:
	movzwl	(%r10), %esi
	leaq	gf_log(%rip), %rax
	movl	%r8d, (%rax,%rsi,4)
	addl	$2, %ecx
	addq	$4, %rdx
	jmp	.L187
.L218:
	addl	%esi, %esi
	movw	%si, 2(%rdx)
	jmp	.L234
.L230:
	addl	%esi, %esi
	xorw	32+gf_exp(%rip), %si
	movw	%si, 2(%rdx)
.L234:
	movzwl	(%rax), %edi
	leaq	gf_log(%rip), %rdx
	movl	%r11d, (%rdx,%rdi,4)
	leal	1(%rcx), %r8d
	movzwl	(%rax), %r9d
	cmpw	$-32768, %r9w
	jae	.L235
	addl	%r9d, %r9d
	movw	%r9w, 2(%rax)
	jmp	.L236
.L235:
	addl	%r9d, %r9d
	xorw	32+gf_exp(%rip), %r9w
	movw	%r9w, 2(%rax)
.L236:
	movzwl	2(%rax), %r10d
	leaq	gf_log(%rip), %r11
	movl	%r8d, (%r11,%r10,4)
	leal	2(%rcx), %edi
	movzwl	2(%rax), %esi
	cmpw	$-32768, %si
	jae	.L237
	addl	%esi, %esi
	movw	%si, 4(%rax)
	jmp	.L238
.L237:
	addl	%esi, %esi
	xorw	32+gf_exp(%rip), %si
	movw	%si, 4(%rax)
.L238:
	movzwl	4(%rax), %r8d
	leaq	gf_log(%rip), %rdx
	movl	%edi, (%rdx,%r8,4)
	leal	3(%rcx), %r9d
	movzwl	4(%rax), %r10d
	cmpw	$-32768, %r10w
	jae	.L239
	addl	%r10d, %r10d
	movw	%r10w, 6(%rax)
	jmp	.L240
.L239:
	addl	%r10d, %r10d
	xorw	32+gf_exp(%rip), %r10w
	movw	%r10w, 6(%rax)
.L240:
	movzwl	6(%rax), %r11d
	leaq	gf_log(%rip), %rdi
	movl	%r9d, (%rdi,%r11,4)
	addl	$4, %ecx
	leaq	8(%rax), %rdx
	cmpl	$65535, %ecx
	jne	.L187
	jmp	.L214
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
	je	.L242
	movq	16(%rdi), %rdi
	movl	8(%rbx), %eax
	xorl	12(%rbx), %eax
	xorl	$-20181524, %eax
	xorq	%rdi, %rax
	cmpq	%rax, (%rbx)
	je	.L243
.L242:
	movq	stderr@GOTPCREL(%rip), %rdx
	movq	(%rdx), %rcx
	movl	$27, %edx
	movl	$1, %esi
	leaq	.LC2(%rip), %rdi
	call	fwrite@PLT
	jmp	.L241
.L243:
	call	free@PLT
	movq	%rbx, %rdi
	.p2align 4,,5
	call	free@PLT
.L241:
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
	subq	$40, %rsp
	.cfi_def_cfa_offset 96
	movl	%edi, %ebp
	movl	%esi, %r13d
	cmpl	$0, fec_initialized(%rip)
	jne	.L247
	movl	$0, %eax
	call	init_fec@PLT
.L247:
	cmpl	$65536, %ebp
	jg	.L248
	cmpl	$65536, %r13d
	jg	.L248
	cmpl	%r13d, %ebp
	jle	.L249
.L248:
	movl	$65535, %r9d
	movl	%r13d, %r8d
	movl	%ebp, %ecx
	leaq	.LC3(%rip), %rdx
	movl	$1, %esi
	movq	stderr@GOTPCREL(%rip), %rbx
	movq	(%rbx), %rdi
	movl	$0, %eax
	call	__fprintf_chk@PLT
	movl	$0, %eax
	jmp	.L250
.L249:
	leaq	.LC4(%rip), %rsi
	movl	$24, %edi
	call	my_malloc
	movq	%rax, %r15
	movq	%rax, 16(%rsp)
	movl	%ebp, 8(%rax)
	movl	%r13d, 12(%rax)
	movl	%r13d, %ebx
	imull	%ebp, %ebx
	addl	%ebx, %ebx
	leaq	.LC1(%rip), %rsi
	movl	%ebx, %edi
	call	my_malloc
	movq	%rax, 16(%r15)
	movl	%r13d, %edx
	xorl	$-20181524, %edx
	xorl	%ebp, %edx
	xorq	%rdx, %rax
	movq	%rax, (%r15)
	leaq	.LC1(%rip), %rsi
	movl	%ebx, %edi
	call	my_malloc
	movq	%rax, %r12
	movw	$1, (%rax)
	cmpl	$1, %ebp
	jle	.L251
	leaq	2(%rax), %rsi
	leal	-2(%rbp), %ecx
	leaq	4(%rax,%rcx,2), %r8
	movq	%r8, %r9
	subq	%rsi, %r9
	subq	$2, %r9
	shrq	%r9
	andl	$7, %r9d
	movw	$0, 2(%rax)
	leaq	4(%rax), %r10
	cmpq	%r8, %r10
	jne	.L343
	jmp	.L251
.L252:
	movw	$0, (%r10)
	movw	$0, 2(%r10)
	movw	$0, 4(%r10)
	movw	$0, 6(%r10)
	movw	$0, 8(%r10)
	movw	$0, 10(%r10)
	movw	$0, 12(%r10)
	movw	$0, 14(%r10)
	addq	$16, %r10
	cmpq	%r8, %r10
	jne	.L252
.L251:
	movslq	%ebp, %rbx
	addq	%rbx, %rbx
	leaq	(%r12,%rbx), %r11
	leal	-1(%r13), %r14d
	testl	%r14d, %r14d
	jle	.L253
	leal	-1(%rbp), %r15d
	movl	$0, %r10d
	leaq	2(%r15,%r15), %r15
	jmp	.L254
.L257:
	movq	%rdx, %rsi
	movl	%ecx, %eax
	cmpl	$65534, %ecx
	jle	.L255
.L256:
	subl	$65535, %eax
	movl	%eax, %r9d
	sarl	$16, %r9d
	movzwl	%ax, %eax
	addl	%r9d, %eax
	cmpl	$65534, %eax
	jg	.L256
.L255:
	addq	$2, %rdx
	addl	%r8d, %ecx
.L259:
	movzwl	%ax, %eax
	leaq	gf_exp(%rip), %r9
	movzwl	(%r9,%rax,2), %eax
	movw	%ax, (%rsi)
	cmpq	%rdi, %rdx
	jne	.L257
.L258:
	addl	$1, %r10d
	addq	%rbx, %r11
	cmpl	%r14d, %r10d
	je	.L253
.L254:
	testl	%ebp, %ebp
	jle	.L258
	movl	%r10d, %r8d
	leaq	(%r11,%r15), %rdi
	movl	%r10d, %ecx
	leaq	2(%r11), %rdx
	movq	%r11, %rsi
	movl	$0, %eax
	jmp	.L259
.L253:
	movl	%ebp, %esi
	movq	%r12, %rdi
	call	invert_vdm@PLT
	subl	%ebp, %r13d
	movl	%r13d, 8(%rsp)
	movl	%ebp, %eax
	imull	%ebp, %eax
	cltq
	addq	%rax, %rax
	movq	%rax, 24(%rsp)
	movq	16(%rsp), %r11
	movq	%rax, %r10
	addq	16(%r11), %r10
	addq	%r12, %rax
	movq	%rax, (%rsp)
	testl	%r13d, %r13d
	jle	.L260
	movl	%ebp, 12(%rsp)
	movl	$0, %r9d
	movl	$0, %r11d
	jmp	.L261
.L267:
	movq	%rdi, %rdx
	leal	-1(%rbp), %r15d
	andl	$3, %r15d
	movzwl	(%rdi), %eax
	movzwl	(%rcx), %esi
	testl	%eax, %eax
	je	.L293
	jmp	.L344
.L264:
	movzwl	(%rdx), %r13d
	movzwl	(%rcx,%rax,2), %r15d
	testl	%r13d, %r13d
	je	.L270
	testl	%r15d, %r15d
	je	.L270
	movslq	%r15d, %r14
	leaq	gf_log(%rip), %r15
	movslq	%r13d, %r13
	movl	(%r15,%r14,4), %r14d
	addl	(%r15,%r13,4), %r14d
	movslq	%r14d, %r15
	leaq	gf_exp(%rip), %r13
	movzwl	0(%r13,%r15,2), %r14d
	jmp	.L262
.L270:
	movl	$0, %r14d
.L262:
	xorl	%r14d, %esi
	addq	%rbx, %rdx
	addq	$1, %rax
	movzwl	(%rdx), %r13d
	movzwl	(%rcx,%rax,2), %r15d
	testl	%r13d, %r13d
	je	.L300
	jmp	.L345
.L292:
	leal	(%r9,%r8), %eax
	cltq
	movw	%si, (%r10,%rax,2)
	addl	$1, %r8d
	addq	$2, %rdi
	cmpl	%ebp, %r8d
	jne	.L267
.L266:
	addl	$1, %r11d
	addl	12(%rsp), %r9d
	cmpl	8(%rsp), %r11d
	je	.L260
.L261:
	testl	%ebp, %ebp
	jle	.L266
	movslq	%r9d, %r13
	movq	(%rsp), %r14
	leaq	(%r14,%r13,2), %rcx
	movq	%r12, %rdi
	movl	$0, %r8d
	jmp	.L267
.L260:
	movq	16(%rsp), %r15
	movq	16(%r15), %rdi
	movq	24(%rsp), %rdx
	movl	$0, %esi
	call	memset@PLT
	movq	16(%r15), %r8
	testl	%ebp, %ebp
	jle	.L268
	addq	$2, %rbx
	leal	-1(%rbp), %esi
	andl	$7, %esi
	movw	$1, (%r8)
	movl	$1, %edx
	addq	%rbx, %r8
	cmpl	%ebp, %edx
	jne	.L346
	jmp	.L268
.L269:
	movw	$1, (%r8)
	addl	$1, %edx
	addq	%rbx, %r8
	movw	$1, (%r8)
	addq	%rbx, %r8
	movw	$1, (%r8)
	addq	%rbx, %r8
	movw	$1, (%r8)
	addq	%rbx, %r8
	movw	$1, (%r8)
	addq	%rbx, %r8
	movw	$1, (%r8)
	addq	%rbx, %r8
	movw	$1, (%r8)
	addq	%rbx, %r8
	movw	$1, (%r8)
	addl	$7, %edx
	addq	%rbx, %r8
	cmpl	%ebp, %edx
	jne	.L269
.L268:
	movq	%r12, %rdi
	call	free@PLT
	movq	16(%rsp), %rax
.L250:
	addq	$40, %rsp
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
.L346:
	.cfi_restore_state
	testl	%esi, %esi
	je	.L269
	cmpl	$1, %esi
	je	.L329
	cmpl	$2, %esi
	.p2align 4,,3
	je	.L330
	cmpl	$3, %esi
	.p2align 4,,2
	je	.L331
	cmpl	$4, %esi
	.p2align 4,,2
	je	.L332
	cmpl	$5, %esi
	.p2align 4,,2
	je	.L333
	cmpl	$6, %esi
	.p2align 4,,2
	je	.L334
	movw	$1, (%r8)
	addl	$1, %edx
	addq	%rbx, %r8
.L334:
	movw	$1, (%r8)
	addl	$1, %edx
	addq	%rbx, %r8
.L333:
	movw	$1, (%r8)
	addl	$1, %edx
	addq	%rbx, %r8
.L332:
	movw	$1, (%r8)
	addl	$1, %edx
	addq	%rbx, %r8
.L331:
	movw	$1, (%r8)
	addl	$1, %edx
	addq	%rbx, %r8
.L330:
	movw	$1, (%r8)
	addl	$1, %edx
	addq	%rbx, %r8
.L329:
	movw	$1, (%r8)
	addl	$1, %edx
	addq	%rbx, %r8
	cmpl	%ebp, %edx
	jne	.L269
	jmp	.L268
.L293:
	movl	$0, %esi
	jmp	.L347
.L344:
	testl	%esi, %esi
	.p2align 4,,4
	je	.L293
	movslq	%esi, %r13
	leaq	gf_log(%rip), %r14
	cltq
	movl	(%r14,%r13,4), %esi
	addl	(%r14,%rax,4), %esi
	movslq	%esi, %r13
	leaq	gf_exp(%rip), %rax
	movzwl	(%rax,%r13,2), %esi
.L347:
	xorl	$0, %esi
	addq	%rbx, %rdx
	movl	$0, %eax
	addq	$1, %rax
	cmpl	%eax, %ebp
	jle	.L292
	testl	%r15d, %r15d
	je	.L264
	cmpl	$1, %r15d
	je	.L335
	cmpl	$2, %r15d
	je	.L336
	movzwl	(%rdx), %r13d
	movzwl	(%rcx,%rax,2), %r15d
	testl	%r13d, %r13d
	jne	.L348
.L294:
	movl	$0, %r14d
	jmp	.L349
.L348:
	testl	%r15d, %r15d
	je	.L294
	movslq	%r15d, %r14
	leaq	gf_log(%rip), %r15
	movslq	%r13d, %r13
	movl	(%r15,%r14,4), %r14d
	addl	(%r15,%r13,4), %r14d
	movslq	%r14d, %r15
	leaq	gf_exp(%rip), %r13
	movzwl	0(%r13,%r15,2), %r14d
.L349:
	xorl	%r14d, %esi
	addq	%rbx, %rdx
	addq	$1, %rax
.L336:
	movzwl	(%rdx), %r13d
	movzwl	(%rcx,%rax,2), %r15d
	testl	%r13d, %r13d
	jne	.L350
.L296:
	movl	$0, %r14d
	jmp	.L351
.L350:
	testl	%r15d, %r15d
	je	.L296
	movslq	%r15d, %r14
	leaq	gf_log(%rip), %r15
	movslq	%r13d, %r13
	movl	(%r15,%r14,4), %r14d
	addl	(%r15,%r13,4), %r14d
	movslq	%r14d, %r15
	leaq	gf_exp(%rip), %r13
	movzwl	0(%r13,%r15,2), %r14d
.L351:
	xorl	%r14d, %esi
	addq	%rbx, %rdx
	addq	$1, %rax
.L335:
	movzwl	(%rdx), %r13d
	movzwl	(%rcx,%rax,2), %r15d
	testl	%r13d, %r13d
	jne	.L352
.L298:
	movl	$0, %r14d
	jmp	.L353
.L352:
	testl	%r15d, %r15d
	je	.L298
	movslq	%r15d, %r14
	leaq	gf_log(%rip), %r15
	movslq	%r13d, %r13
	movl	(%r15,%r14,4), %r14d
	addl	(%r15,%r13,4), %r14d
	movslq	%r14d, %r15
	leaq	gf_exp(%rip), %r13
	movzwl	0(%r13,%r15,2), %r14d
.L353:
	xorl	%r14d, %esi
	addq	%rbx, %rdx
	addq	$1, %rax
	cmpl	%eax, %ebp
	jg	.L264
	jmp	.L292
.L300:
	movl	$0, %r14d
	jmp	.L354
.L345:
	testl	%r15d, %r15d
	.p2align 4,,2
	je	.L300
	movslq	%r15d, %r14
	leaq	gf_log(%rip), %r15
	movslq	%r13d, %r13
	movl	(%r15,%r14,4), %r14d
	addl	(%r15,%r13,4), %r14d
	movslq	%r14d, %r15
	leaq	gf_exp(%rip), %r13
	movzwl	0(%r13,%r15,2), %r14d
.L354:
	xorl	%r14d, %esi
	addq	%rbx, %rdx
	leaq	1(%rax), %r15
	movzwl	(%rdx), %r13d
	movzwl	(%rcx,%r15,2), %r14d
	testl	%r13d, %r13d
	jne	.L355
.L301:
	movl	$0, %r14d
	jmp	.L356
.L355:
	testl	%r14d, %r14d
	je	.L301
	movslq	%r14d, %r14
	leaq	gf_log(%rip), %r15
	movslq	%r13d, %r13
	movl	(%r15,%r14,4), %r14d
	addl	(%r15,%r13,4), %r14d
	movslq	%r14d, %r15
	leaq	gf_exp(%rip), %r13
	movzwl	0(%r13,%r15,2), %r14d
.L356:
	xorl	%r14d, %esi
	addq	%rbx, %rdx
	leaq	2(%rax), %r15
	movzwl	(%rdx), %r13d
	movzwl	(%rcx,%r15,2), %r14d
	testl	%r13d, %r13d
	jne	.L357
.L302:
	movl	$0, %r14d
	jmp	.L358
.L357:
	testl	%r14d, %r14d
	je	.L302
	movslq	%r14d, %r14
	leaq	gf_log(%rip), %r15
	movslq	%r13d, %r13
	movl	(%r15,%r14,4), %r14d
	addl	(%r15,%r13,4), %r14d
	movslq	%r14d, %r15
	leaq	gf_exp(%rip), %r13
	movzwl	0(%r13,%r15,2), %r14d
.L358:
	xorl	%r14d, %esi
	addq	%rbx, %rdx
	addq	$3, %rax
	cmpl	%eax, %ebp
	jg	.L264
	jmp	.L292
.L343:
	testq	%r9, %r9
	je	.L252
	cmpq	$1, %r9
	.p2align 4,,4
	je	.L337
	cmpq	$2, %r9
	je	.L338
	cmpq	$3, %r9
	je	.L339
	cmpq	$4, %r9
	je	.L340
	cmpq	$5, %r9
	je	.L341
	cmpq	$6, %r9
	je	.L342
	movw	$0, (%r10)
	addq	$2, %r10
.L342:
	movw	$0, (%r10)
	addq	$2, %r10
.L341:
	movw	$0, (%r10)
	addq	$2, %r10
.L340:
	movw	$0, (%r10)
	addq	$2, %r10
.L339:
	movw	$0, (%r10)
	addq	$2, %r10
.L338:
	movw	$0, (%r10)
	addq	$2, %r10
.L337:
	movw	$0, (%r10)
	addq	$2, %r10
	cmpq	%r8, %r10
	jne	.L252
	jmp	.L251
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
	movq	%rsi, %r14
	movq	%rdx, %r13
	movl	8(%rdi), %r12d
	movl	%r8d, %eax
	shrl	$31, %eax
	addl	%eax, %r8d
	sarl	%r8d
	cmpl	%ecx, %r12d
	jle	.L369
	movslq	%r8d, %rdx
	addq	%rdx, %rdx
	movslq	%ecx, %r12
	movq	(%rsi,%r12,8), %rsi
	movq	%r13, %rdi
	call	memmove@PLT
	jmp	.L368
.L369:
	movl	%r8d, %r15d
	movl	12(%rdi), %edx
	cmpl	%edx, %ecx
	jge	.L371
	imull	%r12d, %ecx
	movslq	%ecx, %rcx
	movq	16(%rdi), %rsi
	leaq	(%rsi,%rcx,2), %rbp
	movslq	%r8d, %r8
	leaq	(%r8,%r8), %rdx
	movl	$0, %esi
	movq	%r13, %rdi
	call	memset@PLT
	testl	%r12d, %r12d
	jle	.L368
	movl	$0, %ebx
	leal	-1(%r12), %edi
	andl	$7, %edi
	movl	%edi, 12(%rsp)
	movzwl	0(%rbp,%rbx,2), %r9d
	testw	%r9w, %r9w
	je	.L375
	jmp	.L416
.L373:
	movzwl	0(%rbp,%rbx,2), %r9d
	testw	%r9w, %r9w
	je	.L372
	movzwl	%r9w, %edx
	movq	(%r14,%rbx,8), %rsi
	movl	%r15d, %ecx
	movq	%r13, %rdi
	call	addmul1
.L372:
	addq	$1, %rbx
	movzwl	0(%rbp,%rbx,2), %r10d
	testw	%r10w, %r10w
	je	.L390
	jmp	.L417
.L371:
	leal	-1(%rdx), %r8d
	leaq	.LC5(%rip), %rdx
	movl	$1, %esi
	movq	stderr@GOTPCREL(%rip), %rbx
	movq	(%rbx), %rdi
	movl	$0, %eax
	call	__fprintf_chk@PLT
.L368:
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
.L416:
	.cfi_restore_state
	movzwl	%r9w, %edx
	movq	(%r14,%rbx,8), %rsi
	movl	%r15d, %ecx
	movq	%r13, %rdi
	call	addmul1
.L375:
	addq	$1, %rbx
	cmpl	%ebx, %r12d
	jle	.L368
	movl	12(%rsp), %r10d
	testl	%r10d, %r10d
	je	.L373
	cmpl	$1, %r10d
	je	.L410
	cmpl	$2, %r10d
	je	.L411
	cmpl	$3, %r10d
	je	.L412
	cmpl	$4, %r10d
	je	.L413
	cmpl	$5, %r10d
	je	.L414
	cmpl	$6, %r10d
	je	.L415
	movzwl	0(%rbp,%rbx,2), %r11d
	testw	%r11w, %r11w
	je	.L376
	movzwl	%r11w, %edx
	movq	(%r14,%rbx,8), %rsi
	movl	%r15d, %ecx
	movq	%r13, %rdi
	call	addmul1
.L376:
	addq	$1, %rbx
.L415:
	movzwl	0(%rbp,%rbx,2), %eax
	testw	%ax, %ax
	je	.L378
	movzwl	%ax, %edx
	movq	(%r14,%rbx,8), %rsi
	movl	%r15d, %ecx
	movq	%r13, %rdi
	call	addmul1
.L378:
	addq	$1, %rbx
.L414:
	movzwl	0(%rbp,%rbx,2), %edx
	testw	%dx, %dx
	je	.L380
	movzwl	%dx, %edx
	movq	(%r14,%rbx,8), %rsi
	movl	%r15d, %ecx
	movq	%r13, %rdi
	call	addmul1
.L380:
	addq	$1, %rbx
.L413:
	movzwl	0(%rbp,%rbx,2), %ecx
	testw	%cx, %cx
	je	.L382
	movzwl	%cx, %edx
	movq	(%r14,%rbx,8), %rsi
	movl	%r15d, %ecx
	movq	%r13, %rdi
	call	addmul1
.L382:
	addq	$1, %rbx
.L412:
	movzwl	0(%rbp,%rbx,2), %esi
	testw	%si, %si
	je	.L384
	movzwl	%si, %edx
	movq	(%r14,%rbx,8), %rsi
	movl	%r15d, %ecx
	movq	%r13, %rdi
	call	addmul1
.L384:
	addq	$1, %rbx
.L411:
	movzwl	0(%rbp,%rbx,2), %r8d
	testw	%r8w, %r8w
	je	.L386
	movzwl	%r8w, %edx
	movq	(%r14,%rbx,8), %rsi
	movl	%r15d, %ecx
	movq	%r13, %rdi
	call	addmul1
.L386:
	addq	$1, %rbx
.L410:
	movzwl	0(%rbp,%rbx,2), %edi
	testw	%di, %di
	je	.L388
	movzwl	%di, %edx
	movq	(%r14,%rbx,8), %rsi
	movl	%r15d, %ecx
	movq	%r13, %rdi
	call	addmul1
.L388:
	addq	$1, %rbx
	cmpl	%ebx, %r12d
	jg	.L373
	jmp	.L368
.L417:
	movzwl	%r10w, %edx
	movq	(%r14,%rbx,8), %rsi
	movl	%r15d, %ecx
	movq	%r13, %rdi
	call	addmul1
.L390:
	leaq	1(%rbx), %r11
	movzwl	0(%rbp,%r11,2), %eax
	testw	%ax, %ax
	je	.L391
	movzwl	%ax, %edx
	movq	(%r14,%r11,8), %rsi
	movl	%r15d, %ecx
	movq	%r13, %rdi
	call	addmul1
.L391:
	leaq	2(%rbx), %rcx
	movzwl	0(%rbp,%rcx,2), %edx
	testw	%dx, %dx
	je	.L392
	movzwl	%dx, %edx
	movq	(%r14,%rcx,8), %rsi
	movl	%r15d, %ecx
	movq	%r13, %rdi
	call	addmul1
.L392:
	leaq	3(%rbx), %rsi
	movzwl	0(%rbp,%rsi,2), %r8d
	testw	%r8w, %r8w
	je	.L393
	movzwl	%r8w, %edx
	movq	(%r14,%rsi,8), %rsi
	movl	%r15d, %ecx
	movq	%r13, %rdi
	call	addmul1
.L393:
	leaq	4(%rbx), %rdi
	movzwl	0(%rbp,%rdi,2), %r9d
	testw	%r9w, %r9w
	je	.L394
	movzwl	%r9w, %edx
	movq	(%r14,%rdi,8), %rsi
	movl	%r15d, %ecx
	movq	%r13, %rdi
	call	addmul1
.L394:
	leaq	5(%rbx), %r10
	movzwl	0(%rbp,%r10,2), %r11d
	testw	%r11w, %r11w
	je	.L395
	movzwl	%r11w, %edx
	movq	(%r14,%r10,8), %rsi
	movl	%r15d, %ecx
	movq	%r13, %rdi
	call	addmul1
.L395:
	leaq	6(%rbx), %rax
	movzwl	0(%rbp,%rax,2), %ecx
	testw	%cx, %cx
	je	.L396
	movzwl	%cx, %edx
	movq	(%r14,%rax,8), %rsi
	movl	%r15d, %ecx
	movq	%r13, %rdi
	call	addmul1
.L396:
	addq	$7, %rbx
	cmpl	%ebx, %r12d
	jg	.L373
	jmp	.L368
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
	subq	$152, %rsp
	.cfi_def_cfa_offset 208
	movq	%rdi, %rbx
	movq	%rsi, 40(%rsp)
	movq	%rdx, %r14
	movl	%ecx, 24(%rsp)
	movl	8(%rdi), %r15d
	testl	%r15d, %r15d
	jle	.L420
	movl	$0, %eax
	movq	%rsi, %r10
.L426:
	movslq	%eax, %rcx
	leaq	(%r14,%rcx,4), %rsi
	movl	(%rsi), %edx
	cmpl	%edx, %r15d
	jle	.L421
	cmpl	%edx, %eax
	jne	.L422
.L421:
	addl	$1, %eax
	jmp	.L423
.L422:
	movslq	%edx, %rdi
	leaq	(%r14,%rdi,4), %r8
	movl	(%r8), %r9d
	cmpl	%r9d, %edx
	je	.L476
	movl	%r9d, (%rsi)
	movl	%edx, (%r8)
	leaq	(%r10,%rcx,8), %rbp
	movq	0(%rbp), %r11
	leaq	(%r10,%rdi,8), %r12
	movq	(%r12), %r13
	movq	%r13, 0(%rbp)
	movq	%r11, (%r12)
.L423:
	cmpl	%eax, %r15d
	jg	.L426
	jmp	.L819
.L814:
	movl	%r15d, 32(%rsp)
	movq	8(%rsp), %r15
	jmp	.L473
.L815:
	movl	%r15d, 32(%rsp)
	movq	8(%rsp), %r15
.L473:
	movl	(%r14,%rbp,4), %ecx
	cmpl	%ecx, %r13d
	jle	.L427
	movq	%r15, %rdx
	movl	$0, %esi
	movq	%r12, %rdi
	call	memset@PLT
	movq	16(%rsp), %rax
	movw	$1, (%rax)
	jmp	.L428
.L427:
	movl	12(%rbx), %edx
	cmpl	%edx, %ecx
	jge	.L429
	imull	%r13d, %ecx
	movslq	%ecx, %r11
	movq	16(%rbx), %r10
	leaq	(%r10,%r11,2), %rsi
	movq	%r15, %rdx
	movq	%r12, %rdi
	call	memmove@PLT
	jmp	.L428
.L429:
	leal	-1(%rdx), %r8d
	leaq	.LC6(%rip), %rdx
	movl	$1, %esi
	movq	stderr@GOTPCREL(%rip), %r14
	movq	(%r14), %rdi
	movl	$0, %eax
	call	__fprintf_chk@PLT
	movq	56(%rsp), %rdi
	call	free@PLT
	jmp	.L430
.L428:
	addq	%r15, %r12
	addq	$1, %rbp
	movq	16(%rsp), %rcx
	addq	(%rsp), %rcx
	movq	%rcx, 16(%rsp)
	movl	(%r14,%rbp,4), %ecx
	cmpl	%ecx, %r13d
	jle	.L684
	jmp	.L726
.L474:
	movl	$0, 0(%rbp,%r10,4)
	addq	$1, %r10
	movl	$0, 0(%rbp,%r10,4)
	leaq	1(%r10), %rcx
	movl	$0, 0(%rbp,%rcx,4)
	leaq	2(%r10), %rsi
	movl	$0, 0(%rbp,%rsi,4)
	leaq	3(%r10), %rdx
	movl	$0, 0(%rbp,%rdx,4)
	leaq	4(%r10), %rdi
	movl	$0, 0(%rbp,%rdi,4)
	leaq	5(%r10), %r8
	movl	$0, 0(%rbp,%r8,4)
	leaq	6(%r10), %r9
	movl	$0, 0(%rbp,%r9,4)
	addq	$7, %r10
	cmpl	%r10d, %r13d
	jg	.L474
.L741:
	leal	1(%r13), %r11d
	movl	%r11d, 128(%rsp)
	movl	48(%rsp), %ebx
	subl	$1, %ebx
	movl	$0, 36(%rsp)
	movq	$0, (%rsp)
	leaq	2(%rbx,%rbx), %r12
	movq	%r12, 104(%rsp)
	movl	%r15d, 132(%rsp)
	movq	56(%rsp), %r12
	movq	%r14, 136(%rsp)
	movl	%r13d, %r14d
.L455:
	movq	(%rsp), %r13
	movl	%r13d, %ebx
	movl	0(%rbp,%r13,4), %r10d
	cmpl	$1, %r10d
	jne	.L434
.L437:
	movl	48(%rsp), %r8d
	movl	$0, %ecx
	movl	$0, %esi
	jmp	.L435
.L434:
	movslq	36(%rsp), %r15
	cmpw	$0, (%r12,%r15,2)
	jne	.L436
	jmp	.L437
.L435:
	movl	%esi, %edx
	cmpl	$1, 0(%rbp,%rsi,4)
	je	.L438
	movl	$0, %eax
	leal	-1(%r14), %edi
	andl	$7, %edi
	movl	%eax, %ebx
	movl	0(%rbp), %r9d
	testl	%r9d, %r9d
	jne	.L601
	jmp	.L727
.L443:
	movl	%eax, %ebx
	movl	0(%rbp,%rax,4), %edi
	testl	%edi, %edi
	jne	.L439
	leal	(%rcx,%rax), %r9d
	movslq	%r9d, %r11
	cmpw	$0, (%r12,%r11,2)
	je	.L441
	jmp	.L440
.L439:
	cmpl	$1, %edi
	jle	.L441
.L755:
	movq	stderr@GOTPCREL(%rip), %r14
	movq	(%r14), %rcx
	movl	$16, %edx
	movl	$1, %esi
	leaq	.LC7(%rip), %rdi
	call	fwrite@PLT
	jmp	.L442
.L441:
	addq	$1, %rax
	movq	%rax, %r13
	movl	%eax, %ebx
	movl	0(%rbp,%rax,4), %r10d
	testl	%r10d, %r10d
	jne	.L624
	jmp	.L728
.L438:
	addq	$1, %rsi
	addl	%r8d, %ecx
	cmpl	%esi, %r14d
	jg	.L435
	movq	stderr@GOTPCREL(%rip), %r12
	movq	(%r12), %rcx
	movl	$21, %edx
	movl	$1, %esi
	leaq	.LC8(%rip), %rdi
	call	fwrite@PLT
	jmp	.L442
.L440:
	movslq	%ebx, %r15
	addl	$1, 0(%rbp,%r15,4)
	cmpl	%ebx, %edx
	je	.L445
	movl	%r14d, %eax
	imull	%ebx, %eax
	movl	%r14d, %edi
	imull	%edx, %edi
	movl	48(%rsp), %r8d
	leal	(%r8,%rdi), %ecx
	movl	%edi, %esi
	subl	%edi, %eax
	notl	%edi
	addl	%ecx, %edi
	andl	$3, %edi
	movslq	%esi, %r9
	leaq	(%r12,%r9,2), %r13
	movzwl	0(%r13), %r10d
	leal	(%rax,%rsi), %r11d
	movslq	%r11d, %r8
	leaq	(%r12,%r8,2), %r9
	movzwl	(%r9), %r11d
	movw	%r11w, 0(%r13)
	movw	%r10w, (%r9)
	addl	$1, %esi
	cmpl	%ecx, %esi
	jne	.L729
	jmp	.L445
.L446:
	movslq	%esi, %r11
	leaq	(%r12,%r11,2), %r13
	movzwl	0(%r13), %r9d
	leal	(%rax,%rsi), %r10d
	movslq	%r10d, %rdi
	leaq	(%r12,%rdi,2), %r8
	movzwl	(%r8), %r11d
	movw	%r11w, 0(%r13)
	movw	%r9w, (%r8)
	addl	$1, %esi
	movl	%esi, %r13d
	movslq	%esi, %r9
	leaq	(%r12,%r9,2), %r8
	movzwl	(%r8), %r11d
	addl	%eax, %r13d
	movslq	%r13d, %r10
	leaq	(%r12,%r10,2), %rdi
	movzwl	(%rdi), %r13d
	movw	%r13w, (%r8)
	movw	%r11w, (%rdi)
	leal	1(%rsi), %r11d
	movslq	%r11d, %r9
	leaq	(%r12,%r9,2), %r8
	movzwl	(%r8), %r13d
	addl	%eax, %r11d
	movslq	%r11d, %r10
	leaq	(%r12,%r10,2), %rdi
	movzwl	(%rdi), %r11d
	movw	%r11w, (%r8)
	movw	%r13w, (%rdi)
	leal	2(%rsi), %r13d
	movslq	%r13d, %r9
	leaq	(%r12,%r9,2), %r8
	movzwl	(%r8), %r11d
	addl	%eax, %r13d
	movslq	%r13d, %r10
	leaq	(%r12,%r10,2), %rdi
	movzwl	(%rdi), %r13d
	movw	%r13w, (%r8)
	movw	%r11w, (%rdi)
	addl	$3, %esi
	cmpl	%ecx, %esi
	jne	.L446
.L445:
	movq	72(%rsp), %rcx
	movq	(%rsp), %rax
	movl	%edx, (%rcx,%rax,4)
	movq	64(%rsp), %rsi
	movl	%ebx, (%rsi,%rax,4)
	movl	%r14d, %eax
	imull	%ebx, %eax
	cltq
	leaq	(%r12,%rax,2), %r9
	movq	%r9, 16(%rsp)
	addq	%r15, %r15
	leaq	(%r9,%r15), %r8
	movzwl	(%r8), %r11d
	testw	%r11w, %r11w
	jne	.L447
	movq	stderr@GOTPCREL(%rip), %r13
	movq	0(%r13), %rcx
	movl	$18, %edx
	movl	$1, %esi
	leaq	.LC9(%rip), %rdi
	call	fwrite@PLT
	jmp	.L442
.L447:
	cmpw	$1, %r11w
	je	.L448
	movzwl	%r11w, %r10d
	leaq	inverse(%rip), %rdi
	movzwl	(%rdi,%r10,2), %r13d
	movw	$1, (%r8)
	movq	16(%rsp), %r9
	movq	%r9, %rax
	movq	%r9, %rdi
	addq	104(%rsp), %rdi
	movzwl	%r13w, %ecx
	movslq	%ecx, %rsi
	movq	%rdi, %rdx
	subq	%r9, %rdx
	subq	$2, %rdx
	shrq	%rdx
	andl	$3, %edx
	movzwl	(%r9), %r8d
	testl	%r8d, %r8d
	je	.L577
	jmp	.L730
.L450:
	movq	%rax, %r11
	movzwl	(%rax), %edx
	testl	%edx, %edx
	je	.L477
	testl	%ecx, %ecx
	je	.L477
	leaq	gf_log(%rip), %r9
	movslq	%edx, %r8
	movl	(%r9,%rsi,4), %r10d
	addl	(%r9,%r8,4), %r10d
	movslq	%r10d, %r9
	leaq	gf_exp(%rip), %rdx
	movzwl	(%rdx,%r9,2), %r8d
	jmp	.L449
.L477:
	movl	$0, %r8d
.L449:
	movw	%r8w, (%r11)
	leaq	2(%rax), %r11
	movzwl	2(%rax), %eax
	testl	%eax, %eax
	je	.L584
	jmp	.L731
.L448:
	movq	80(%rsp), %rsi
	movq	%rsi, %rdi
	addq	%r15, %rdi
	movq	%rdi, 88(%rsp)
	movw	$1, (%rdi)
	movq	8(%rsp), %rdx
	movq	16(%rsp), %rdi
	call	bcmp@PLT
	testl	%eax, %eax
	je	.L451
	movq	%r12, 112(%rsp)
	movl	$0, %r13d
	leal	-1(%r14), %ecx
	andl	$3, %ecx
	movl	%ecx, 120(%rsp)
	cmpl	%r13d, %ebx
	jne	.L732
	jmp	.L561
.L789:
	movq	%r12, 112(%rsp)
	movq	8(%rsp), %r12
	movq	%rbp, 120(%rsp)
	movl	32(%rsp), %ebp
	jmp	.L453
.L790:
	movq	%r12, 112(%rsp)
	movq	8(%rsp), %r12
	movq	%rbp, 120(%rsp)
	movl	32(%rsp), %ebp
.L453:
	cmpl	%ebp, %ebx
	je	.L452
	movzwl	0(%r13,%r15), %eax
	movw	$0, 0(%r13,%r15)
	testw	%ax, %ax
	je	.L452
	movzwl	%ax, %edx
	movl	%r14d, %ecx
	movq	16(%rsp), %rsi
	movq	%r13, %rdi
	call	addmul1
.L452:
	addl	$1, %ebp
	addq	%r12, %r13
	cmpl	%ebp, %ebx
	jne	.L733
	jmp	.L568
.L451:
	movq	88(%rsp), %rbx
	movw	$0, (%rbx)
	addq	$1, (%rsp)
	movl	128(%rsp), %r15d
	addl	%r15d, 36(%rsp)
	cmpl	(%rsp), %r14d
	jg	.L455
	movl	132(%rsp), %r15d
	movl	%r14d, %r13d
	movq	136(%rsp), %r14
.L454:
	movl	%r13d, %ebx
	subl	$1, %ebx
	js	.L456
	movl	%r13d, %r12d
	movl	%r15d, (%rsp)
	movq	56(%rsp), %r15
	movq	%rbp, 8(%rsp)
	movq	72(%rsp), %rbp
.L463:
	movslq	%ebx, %rax
	salq	$2, %rax
	leaq	0(%rbp,%rax), %rdx
	movl	(%rdx), %ecx
	testl	%ecx, %ecx
	js	.L457
	cmpl	%ecx, %r13d
	jg	.L458
.L457:
	leaq	.LC10(%rip), %rdx
	movl	$1, %esi
	movq	stderr@GOTPCREL(%rip), %r11
	movq	(%r11), %rdi
	movl	$0, %eax
	call	__fprintf_chk@PLT
	jmp	.L459
.L458:
	addq	64(%rsp), %rax
	movl	(%rax), %r8d
	testl	%r8d, %r8d
	js	.L460
	cmpl	%r8d, %r13d
	jg	.L461
.L460:
	movl	%r8d, %ecx
	leaq	.LC11(%rip), %rdx
	movl	$1, %esi
	movq	stderr@GOTPCREL(%rip), %rax
	movq	(%rax), %rdi
	movl	$0, %eax
	call	__fprintf_chk@PLT
	jmp	.L459
.L461:
	cmpl	%r8d, %ecx
	je	.L459
	testl	%r13d, %r13d
	.p2align 4,,5
	jle	.L459
	leal	-1(%r13), %edi
	andl	$3, %edi
	movslq	(%rdx), %rcx
	leaq	(%r15,%rcx,2), %r11
	movzwl	(%r11), %esi
	movslq	(%rax), %r10
	movzwl	(%r15,%r10,2), %r9d
	movw	%r9w, (%r11)
	movslq	(%rax), %r8
	movw	%si, (%r15,%r8,2)
	movl	$1, %r11d
	movl	%r12d, %ecx
	cmpl	%r13d, %r11d
	jne	.L734
	jmp	.L459
.L462:
	movl	%ecx, %esi
	addl	(%rdx), %esi
	movslq	%esi, %r10
	leaq	(%r15,%r10,2), %rdi
	movzwl	(%rdi), %r8d
	movl	%ecx, %r9d
	addl	(%rax), %r9d
	movslq	%r9d, %rsi
	movzwl	(%r15,%rsi,2), %r10d
	movw	%r10w, (%rdi)
	movl	%ecx, %edi
	addl	(%rax), %edi
	movslq	%edi, %r9
	movw	%r8w, (%r15,%r9,2)
	addl	$1, %r11d
	addl	%r12d, %ecx
	movl	%ecx, %r8d
	addl	(%rdx), %r8d
	movslq	%r8d, %rsi
	leaq	(%r15,%rsi,2), %r10
	movzwl	(%r10), %r8d
	movl	%ecx, %edi
	addl	(%rax), %edi
	movslq	%edi, %r9
	movzwl	(%r15,%r9,2), %esi
	movw	%si, (%r10)
	movl	%ecx, %r10d
	addl	(%rax), %r10d
	movslq	%r10d, %rdi
	movw	%r8w, (%r15,%rdi,2)
	addl	%r12d, %ecx
	movl	%ecx, %r8d
	addl	(%rdx), %r8d
	movslq	%r8d, %r9
	leaq	(%r15,%r9,2), %rsi
	movzwl	(%rsi), %r10d
	movl	%ecx, %edi
	addl	(%rax), %edi
	movslq	%edi, %r8
	movzwl	(%r15,%r8,2), %r9d
	movw	%r9w, (%rsi)
	movl	%ecx, %esi
	addl	(%rax), %esi
	movslq	%esi, %rdi
	movw	%r10w, (%r15,%rdi,2)
	addl	%r12d, %ecx
	movl	%ecx, %r10d
	addl	(%rdx), %r10d
	movslq	%r10d, %r8
	leaq	(%r15,%r8,2), %rsi
	movzwl	(%rsi), %r10d
	movl	%ecx, %r9d
	addl	(%rax), %r9d
	movslq	%r9d, %rdi
	movzwl	(%r15,%rdi,2), %r8d
	movw	%r8w, (%rsi)
	movl	%ecx, %esi
	addl	(%rax), %esi
	movslq	%esi, %r9
	movw	%r10w, (%r15,%r9,2)
	addl	$3, %r11d
	addl	%r12d, %ecx
	cmpl	%r13d, %r11d
	jne	.L462
.L459:
	subl	$1, %ebx
	cmpl	$-1, %ebx
	jne	.L463
	movl	(%rsp), %r15d
	movq	8(%rsp), %rbp
	jmp	.L456
.L742:
	movl	%r15d, 64(%rsp)
	leal	0(,%r15,8), %edi
	leaq	.LC12(%rip), %rsi
	call	my_malloc
	movq	%rax, %r13
	testl	%r15d, %r15d
	jle	.L464
	movl	24(%rsp), %edx
	movl	%edx, %eax
	shrl	$31, %eax
	addl	%edx, %eax
	sarl	%eax
	movl	%eax, 16(%rsp)
	leal	(%rax,%rax), %ecx
	movl	%ecx, 72(%rsp)
	cltq
	addq	%rax, %rax
	movq	%rax, 48(%rsp)
	movl	%r15d, 36(%rsp)
	movl	$0, 8(%rsp)
	movl	$0, %ebp
	movl	%r15d, (%rsp)
	movq	%r14, 24(%rsp)
	movq	56(%rsp), %r14
.L469:
	movq	24(%rsp), %r12
	movl	(%rsp), %ebx
	cmpl	(%r12,%rbp,4), %ebx
	jg	.L465
	leaq	.LC13(%rip), %rsi
	movl	72(%rsp), %edi
	call	my_malloc
	movq	%rax, 0(%r13,%rbp,8)
	movq	48(%rsp), %rdx
	movl	$0, %esi
	movq	%rax, %rdi
	call	memset@PLT
	movq	40(%rsp), %r12
	movl	64(%rsp), %r10d
	movl	8(%rsp), %ebx
	addl	%ebx, %r10d
	movl	%r10d, 32(%rsp)
	movl	%ebx, %r15d
	notl	%r15d
	addl	%r10d, %r15d
	andl	$7, %r15d
	movslq	%ebx, %rdi
	movzwl	(%r14,%rdi,2), %r8d
	testw	%r8w, %r8w
	je	.L516
	jmp	.L735
.L467:
	movslq	%ebx, %r10
	movzwl	(%r14,%r10,2), %r15d
	testw	%r15w, %r15w
	je	.L466
	movzwl	%r15w, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	16(%rsp), %ecx
	movq	(%r12), %rsi
	call	addmul1
.L466:
	addl	$1, %ebx
	movl	%ebx, %eax
	leaq	8(%r12), %r15
	cltq
	movzwl	(%r14,%rax,2), %edi
	testw	%di, %di
	je	.L531
	jmp	.L736
.L465:
	addq	$1, %rbp
	movl	36(%rsp), %ebx
	addl	%ebx, 8(%rsp)
	cmpl	%ebp, (%rsp)
	jg	.L469
	movl	(%rsp), %r15d
	movq	24(%rsp), %r14
	movl	$0, %ebx
	leal	-1(%r15), %ebp
	andl	$7, %ebp
	cmpl	(%r14,%rbx,4), %r15d
	jg	.L480
	jmp	.L737
.L783:
	movq	40(%rsp), %r12
	jmp	.L471
.L471:
	cmpl	(%r14,%rbx,4), %r15d
	.p2align 4,,2
	jg	.L470
	movq	0(%r13,%rbx,8), %rsi
	movq	(%r12,%rbx,8), %rdi
	movq	48(%rsp), %rdx
	call	memmove@PLT
	movq	0(%r13,%rbx,8), %rdi
	call	free@PLT
	movl	%ebx, (%r14,%rbx,4)
.L470:
	addq	$1, %rbx
	cmpl	(%r14,%rbx,4), %r15d
	jg	.L495
	jmp	.L738
.L464:
	movq	%r13, %rdi
	call	free@PLT
	movq	56(%rsp), %rdi
	call	free@PLT
	movl	$0, %eax
	jmp	.L739
.L476:
	movl	$1, %eax
	jmp	.L739
.L478:
	movl	$1, %eax
	jmp	.L739
.L819:
	movl	8(%rbx), %r13d
	movl	%r13d, %edi
	imull	%r13d, %edi
	addl	%edi, %edi
	leaq	.LC1(%rip), %rsi
	call	my_malloc
	movq	%rax, %r10
	movq	%rax, 56(%rsp)
	testl	%r13d, %r13d
	jle	.L472
	movslq	%r13d, %rax
	addq	%rax, %rax
	movq	%rax, 8(%rsp)
	addq	$2, %rax
	movq	%rax, (%rsp)
	movq	%r10, 16(%rsp)
	movq	%r10, %r12
	movl	$0, %ebp
	leal	-1(%r13), %esi
	andl	$3, %esi
	movl	%esi, 32(%rsp)
	movl	(%r14,%rbp,4), %ecx
	cmpl	%ecx, %r13d
	jle	.L673
	jmp	.L740
.L770:
	movl	%r13d, 48(%rsp)
	leal	0(,%r13,4), %ebx
	leaq	.LC14(%rip), %rsi
	movl	%ebx, %edi
	call	my_malloc
	movq	%rax, 64(%rsp)
	leaq	.LC15(%rip), %rsi
	movl	%ebx, %edi
	call	my_malloc
	movq	%rax, 72(%rsp)
	leaq	.LC16(%rip), %rsi
	movl	%ebx, %edi
	call	my_malloc
	movq	%rax, %rbp
	leal	(%r13,%r13), %ebx
	leaq	.LC1(%rip), %rsi
	movl	%ebx, %edi
	call	my_malloc
	movq	%rax, %r12
	movq	%rax, 80(%rsp)
	leaq	.LC1(%rip), %rsi
	movl	%ebx, %edi
	call	my_malloc
	movq	%rax, 96(%rsp)
	movq	8(%rsp), %rdx
	movl	$0, %esi
	movq	%r12, %rdi
	call	memset@PLT
	movl	$0, %r10d
	leal	-1(%r13), %eax
	andl	$7, %eax
	movl	$0, 0(%rbp,%r10,4)
	addq	$1, %r10
	cmpl	%r10d, %r13d
	jg	.L690
	jmp	.L741
.L472:
	leal	0(,%r13,4), %ebx
	leaq	.LC14(%rip), %rsi
	movl	%ebx, %edi
	call	my_malloc
	movq	%rax, 64(%rsp)
	leaq	.LC15(%rip), %rsi
	movl	%ebx, %edi
	call	my_malloc
	movq	%rax, 72(%rsp)
	leaq	.LC16(%rip), %rsi
	movl	%ebx, %edi
	call	my_malloc
	movq	%rax, %rbp
	leal	(%r13,%r13), %ebx
	leaq	.LC1(%rip), %rsi
	movl	%ebx, %edi
	call	my_malloc
	movq	%rax, %r12
	movq	%rax, 80(%rsp)
	leaq	.LC1(%rip), %rsi
	movl	%ebx, %edi
	call	my_malloc
	movq	%rax, 96(%rsp)
	movslq	%r13d, %rdx
	addq	%rdx, %rdx
	movl	$0, %esi
	movq	%r12, %rdi
	call	memset@PLT
	jmp	.L454
.L436:
	movq	(%rsp), %rdx
	movq	%rdx, %r15
	addl	$1, %r10d
	movl	%r10d, 0(%rbp,%rdx,4)
	movl	%r13d, %edx
	jmp	.L445
.L442:
	movq	64(%rsp), %rdi
	call	free@PLT
	movq	72(%rsp), %rdi
	call	free@PLT
	movq	%rbp, %rdi
	call	free@PLT
	movq	80(%rsp), %rdi
	call	free@PLT
	movq	96(%rsp), %rdi
	call	free@PLT
	movq	56(%rsp), %rdi
	call	free@PLT
	jmp	.L430
.L456:
	movq	64(%rsp), %rdi
	call	free@PLT
	movq	72(%rsp), %rdi
	call	free@PLT
	movq	%rbp, %rdi
	call	free@PLT
	movq	80(%rsp), %rdi
	call	free@PLT
	movq	96(%rsp), %rdi
	call	free@PLT
	cmpq	$0, 56(%rsp)
	jne	.L742
	jmp	.L478
.L430:
	movl	$1, %eax
	jmp	.L739
.L420:
	movl	8(%rdi), %r13d
	movl	%r13d, %edi
	imull	%r13d, %edi
	addl	%edi, %edi
	leaq	.LC1(%rip), %rsi
	call	my_malloc
	movq	%rax, 56(%rsp)
	jmp	.L472
.L739:
	addq	$152, %rsp
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
.L737:
	.cfi_restore_state
	movq	0(%r13,%rbx,8), %rsi
	movq	40(%rsp), %rcx
	movq	(%rcx,%rbx,8), %rdi
	movq	48(%rsp), %rdx
	call	memmove@PLT
	movq	0(%r13,%rbx,8), %rdi
	call	free@PLT
	movl	%ebx, (%r14,%rbx,4)
.L480:
	addq	$1, %rbx
	cmpl	%ebx, %r15d
	jle	.L464
	testl	%ebp, %ebp
	je	.L783
	cmpl	$1, %ebp
	je	.L691
	cmpl	$2, %ebp
	.p2align 4,,3
	je	.L692
	cmpl	$3, %ebp
	.p2align 4,,2
	je	.L693
	cmpl	$4, %ebp
	.p2align 4,,2
	je	.L694
	cmpl	$5, %ebp
	.p2align 4,,2
	je	.L695
	cmpl	$6, %ebp
	.p2align 4,,2
	je	.L696
	cmpl	(%r14,%rbx,4), %r15d
	jg	.L481
	movq	0(%r13,%rbx,8), %rsi
	movq	40(%rsp), %r10
	movq	(%r10,%rbx,8), %rdi
	movq	48(%rsp), %rdx
	call	memmove@PLT
	movq	0(%r13,%rbx,8), %rdi
	call	free@PLT
	movl	%ebx, (%r14,%rbx,4)
.L481:
	addq	$1, %rbx
.L696:
	cmpl	(%r14,%rbx,4), %r15d
	jg	.L483
	movq	0(%r13,%rbx,8), %rsi
	movq	40(%rsp), %rdi
	movq	(%rdi,%rbx,8), %rdi
	movq	48(%rsp), %rdx
	call	memmove@PLT
	movq	0(%r13,%rbx,8), %rdi
	call	free@PLT
	movl	%ebx, (%r14,%rbx,4)
.L483:
	addq	$1, %rbx
.L695:
	cmpl	(%r14,%rbx,4), %r15d
	jg	.L485
	movq	0(%r13,%rbx,8), %rsi
	movq	40(%rsp), %r8
	movq	(%r8,%rbx,8), %rdi
	movq	48(%rsp), %rdx
	call	memmove@PLT
	movq	0(%r13,%rbx,8), %rdi
	call	free@PLT
	movl	%ebx, (%r14,%rbx,4)
.L485:
	addq	$1, %rbx
.L694:
	cmpl	(%r14,%rbx,4), %r15d
	jg	.L487
	movq	0(%r13,%rbx,8), %rsi
	movq	40(%rsp), %r9
	movq	(%r9,%rbx,8), %rdi
	movq	48(%rsp), %rdx
	call	memmove@PLT
	movq	0(%r13,%rbx,8), %rdi
	call	free@PLT
	movl	%ebx, (%r14,%rbx,4)
.L487:
	addq	$1, %rbx
.L693:
	cmpl	(%r14,%rbx,4), %r15d
	jg	.L489
	movq	0(%r13,%rbx,8), %rsi
	movq	40(%rsp), %r11
	movq	(%r11,%rbx,8), %rdi
	movq	48(%rsp), %rdx
	call	memmove@PLT
	movq	0(%r13,%rbx,8), %rdi
	call	free@PLT
	movl	%ebx, (%r14,%rbx,4)
.L489:
	addq	$1, %rbx
.L692:
	cmpl	(%r14,%rbx,4), %r15d
	jg	.L491
	movq	0(%r13,%rbx,8), %rsi
	movq	40(%rsp), %rax
	movq	(%rax,%rbx,8), %rdi
	movq	48(%rsp), %rdx
	call	memmove@PLT
	movq	0(%r13,%rbx,8), %rdi
	call	free@PLT
	movl	%ebx, (%r14,%rbx,4)
.L491:
	addq	$1, %rbx
.L691:
	cmpl	(%r14,%rbx,4), %r15d
	jg	.L493
	movq	0(%r13,%rbx,8), %rsi
	movq	40(%rsp), %rdx
	movq	(%rdx,%rbx,8), %rdi
	movq	48(%rsp), %rdx
	call	memmove@PLT
	movq	0(%r13,%rbx,8), %rdi
	call	free@PLT
	movl	%ebx, (%r14,%rbx,4)
.L493:
	addq	$1, %rbx
	movq	40(%rsp), %r12
	cmpl	%ebx, %r15d
	jg	.L471
	jmp	.L464
.L738:
	movq	0(%r13,%rbx,8), %rsi
	movq	(%r12,%rbx,8), %rdi
	movq	48(%rsp), %rdx
	call	memmove@PLT
	movq	0(%r13,%rbx,8), %rdi
	call	free@PLT
	movl	%ebx, (%r14,%rbx,4)
.L495:
	leaq	1(%rbx), %rbp
	cmpl	(%r14,%rbp,4), %r15d
	jg	.L496
	movq	0(%r13,%rbp,8), %rsi
	movq	(%r12,%rbp,8), %rdi
	movq	48(%rsp), %rdx
	call	memmove@PLT
	movq	0(%r13,%rbp,8), %rdi
	call	free@PLT
	movl	%ebp, (%r14,%rbp,4)
.L496:
	leaq	2(%rbx), %rbp
	cmpl	(%r14,%rbp,4), %r15d
	jg	.L497
	movq	0(%r13,%rbp,8), %rsi
	movq	(%r12,%rbp,8), %rdi
	movq	48(%rsp), %rdx
	call	memmove@PLT
	movq	0(%r13,%rbp,8), %rdi
	call	free@PLT
	movl	%ebp, (%r14,%rbp,4)
.L497:
	leaq	3(%rbx), %rbp
	cmpl	(%r14,%rbp,4), %r15d
	jg	.L498
	movq	0(%r13,%rbp,8), %rsi
	movq	(%r12,%rbp,8), %rdi
	movq	48(%rsp), %rdx
	call	memmove@PLT
	movq	0(%r13,%rbp,8), %rdi
	call	free@PLT
	movl	%ebp, (%r14,%rbp,4)
.L498:
	leaq	4(%rbx), %rbp
	cmpl	(%r14,%rbp,4), %r15d
	jg	.L499
	movq	0(%r13,%rbp,8), %rsi
	movq	(%r12,%rbp,8), %rdi
	movq	48(%rsp), %rdx
	call	memmove@PLT
	movq	0(%r13,%rbp,8), %rdi
	call	free@PLT
	movl	%ebp, (%r14,%rbp,4)
.L499:
	leaq	5(%rbx), %rbp
	cmpl	(%r14,%rbp,4), %r15d
	jg	.L500
	movq	0(%r13,%rbp,8), %rsi
	movq	(%r12,%rbp,8), %rdi
	movq	48(%rsp), %rdx
	call	memmove@PLT
	movq	0(%r13,%rbp,8), %rdi
	call	free@PLT
	movl	%ebp, (%r14,%rbp,4)
.L500:
	leaq	6(%rbx), %rbp
	cmpl	(%r14,%rbp,4), %r15d
	jg	.L501
	movq	0(%r13,%rbp,8), %rsi
	movq	(%r12,%rbp,8), %rdi
	movq	48(%rsp), %rdx
	call	memmove@PLT
	movq	0(%r13,%rbp,8), %rdi
	call	free@PLT
	movl	%ebp, (%r14,%rbp,4)
.L501:
	addq	$7, %rbx
	cmpl	%ebx, %r15d
	jg	.L471
	jmp	.L464
.L735:
	movzwl	%r8w, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	16(%rsp), %ecx
	movq	40(%rsp), %rsi
	movq	(%rsi), %rsi
	call	addmul1
.L516:
	addl	$1, %ebx
	addq	$8, %r12
	cmpl	32(%rsp), %ebx
	je	.L465
	testl	%r15d, %r15d
	je	.L467
	cmpl	$1, %r15d
	je	.L697
	cmpl	$2, %r15d
	je	.L698
	cmpl	$3, %r15d
	je	.L699
	cmpl	$4, %r15d
	je	.L700
	cmpl	$5, %r15d
	je	.L701
	cmpl	$6, %r15d
	je	.L702
	movslq	%ebx, %r9
	movzwl	(%r14,%r9,2), %r11d
	testw	%r11w, %r11w
	je	.L517
	movzwl	%r11w, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	16(%rsp), %ecx
	movq	40(%rsp), %rax
	movq	8(%rax), %rsi
	call	addmul1
.L517:
	addl	$1, %ebx
	addq	$8, %r12
.L702:
	movslq	%ebx, %rdx
	movzwl	(%r14,%rdx,2), %ecx
	testw	%cx, %cx
	je	.L519
	movzwl	%cx, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	16(%rsp), %ecx
	movq	(%r12), %rsi
	call	addmul1
.L519:
	addl	$1, %ebx
	addq	$8, %r12
.L701:
	movslq	%ebx, %r10
	movzwl	(%r14,%r10,2), %r15d
	testw	%r15w, %r15w
	je	.L521
	movzwl	%r15w, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	16(%rsp), %ecx
	movq	(%r12), %rsi
	call	addmul1
.L521:
	addl	$1, %ebx
	addq	$8, %r12
.L700:
	movslq	%ebx, %rdi
	movzwl	(%r14,%rdi,2), %r8d
	testw	%r8w, %r8w
	je	.L523
	movzwl	%r8w, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	16(%rsp), %ecx
	movq	(%r12), %rsi
	call	addmul1
.L523:
	addl	$1, %ebx
	addq	$8, %r12
.L699:
	movslq	%ebx, %rsi
	movzwl	(%r14,%rsi,2), %r9d
	testw	%r9w, %r9w
	je	.L525
	movzwl	%r9w, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	16(%rsp), %ecx
	movq	(%r12), %rsi
	call	addmul1
.L525:
	addl	$1, %ebx
	addq	$8, %r12
.L698:
	movslq	%ebx, %r11
	movzwl	(%r14,%r11,2), %eax
	testw	%ax, %ax
	je	.L527
	movzwl	%ax, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	16(%rsp), %ecx
	movq	(%r12), %rsi
	call	addmul1
.L527:
	addl	$1, %ebx
	addq	$8, %r12
.L697:
	movslq	%ebx, %rdx
	movzwl	(%r14,%rdx,2), %ecx
	testw	%cx, %cx
	je	.L529
	movzwl	%cx, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	16(%rsp), %ecx
	movq	(%r12), %rsi
	call	addmul1
.L529:
	addl	$1, %ebx
	addq	$8, %r12
	cmpl	32(%rsp), %ebx
	jne	.L467
	jmp	.L465
.L736:
	movzwl	%di, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	16(%rsp), %ecx
	movq	8(%r12), %rsi
	call	addmul1
.L531:
	leal	1(%rbx), %eax
	cltq
	movzwl	(%r14,%rax,2), %r12d
	testw	%r12w, %r12w
	je	.L532
	movzwl	%r12w, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	16(%rsp), %ecx
	movq	8(%r15), %rsi
	call	addmul1
.L532:
	leal	2(%rbx), %eax
	cltq
	movzwl	(%r14,%rax,2), %r8d
	testw	%r8w, %r8w
	je	.L533
	movzwl	%r8w, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	16(%rsp), %ecx
	movq	16(%r15), %rsi
	call	addmul1
.L533:
	leal	3(%rbx), %eax
	cltq
	movzwl	(%r14,%rax,2), %esi
	testw	%si, %si
	je	.L534
	movzwl	%si, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	16(%rsp), %ecx
	movq	24(%r15), %rsi
	call	addmul1
.L534:
	leal	4(%rbx), %eax
	cltq
	movzwl	(%r14,%rax,2), %r9d
	testw	%r9w, %r9w
	je	.L535
	movzwl	%r9w, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	16(%rsp), %ecx
	movq	32(%r15), %rsi
	call	addmul1
.L535:
	leal	5(%rbx), %eax
	cltq
	movzwl	(%r14,%rax,2), %r11d
	testw	%r11w, %r11w
	je	.L536
	movzwl	%r11w, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	16(%rsp), %ecx
	movq	40(%r15), %rsi
	call	addmul1
.L536:
	leal	6(%rbx), %eax
	cltq
	movzwl	(%r14,%rax,2), %edx
	testw	%dx, %dx
	je	.L537
	movzwl	%dx, %edx
	movq	0(%r13,%rbp,8), %rdi
	movl	16(%rsp), %ecx
	movq	48(%r15), %rsi
	call	addmul1
.L537:
	addl	$7, %ebx
	leaq	56(%r15), %r12
	cmpl	32(%rsp), %ebx
	jne	.L467
	jmp	.L465
.L734:
	testl	%edi, %edi
	je	.L462
	cmpl	$1, %edi
	.p2align 4,,6
	je	.L703
	cmpl	$2, %edi
	.p2align 4,,3
	je	.L704
	movl	%r12d, %edi
	addl	(%rdx), %edi
	movslq	%edi, %rsi
	leaq	(%r15,%rsi,2), %r10
	movzwl	(%r10), %r8d
	movl	%r12d, %r9d
	addl	(%rax), %r9d
	movslq	%r9d, %rdi
	movzwl	(%r15,%rdi,2), %esi
	movw	%si, (%r10)
	movl	%r12d, %r10d
	addl	(%rax), %r10d
	movslq	%r10d, %r9
	movw	%r8w, (%r15,%r9,2)
	addl	$1, %r11d
	addl	%r12d, %ecx
.L704:
	movl	%ecx, %r8d
	addl	(%rdx), %r8d
	movslq	%r8d, %rdi
	leaq	(%r15,%rdi,2), %rsi
	movzwl	(%rsi), %r10d
	movl	%ecx, %r9d
	addl	(%rax), %r9d
	movslq	%r9d, %r8
	movzwl	(%r15,%r8,2), %edi
	movw	%di, (%rsi)
	movl	%ecx, %esi
	addl	(%rax), %esi
	movslq	%esi, %r9
	movw	%r10w, (%r15,%r9,2)
	addl	$1, %r11d
	addl	%r12d, %ecx
.L703:
	movl	%ecx, %r10d
	addl	(%rdx), %r10d
	movslq	%r10d, %r8
	leaq	(%r15,%r8,2), %rdi
	movzwl	(%rdi), %esi
	movl	%ecx, %r9d
	addl	(%rax), %r9d
	movslq	%r9d, %r10
	movzwl	(%r15,%r10,2), %r8d
	movw	%r8w, (%rdi)
	movl	%ecx, %edi
	addl	(%rax), %edi
	movslq	%edi, %r9
	movw	%si, (%r15,%r9,2)
	addl	$1, %r11d
	addl	%r12d, %ecx
	cmpl	%r13d, %r11d
	jne	.L462
	jmp	.L459
.L732:
	movzwl	(%r12,%r15), %esi
	movw	$0, (%r12,%r15)
	testw	%si, %si
	je	.L561
	movzwl	%si, %edx
	movl	%r14d, %ecx
	movq	16(%rsp), %rsi
	movq	112(%rsp), %rdi
	call	addmul1
.L561:
	leal	1(%r13), %r11d
	movl	%r11d, 32(%rsp)
	movq	112(%rsp), %r13
	addq	8(%rsp), %r13
	cmpl	%r14d, %r11d
	je	.L451
	movl	120(%rsp), %r10d
	testl	%r10d, %r10d
	je	.L789
	cmpl	$1, %r10d
	je	.L705
	cmpl	$2, %r10d
	je	.L706
	cmpl	%r11d, %ebx
	je	.L562
	movzwl	0(%r13,%r15), %r9d
	movw	$0, 0(%r13,%r15)
	testw	%r9w, %r9w
	je	.L562
	movzwl	%r9w, %edx
	movl	%r14d, %ecx
	movq	16(%rsp), %rsi
	movq	%r13, %rdi
	call	addmul1
.L562:
	addl	$1, 32(%rsp)
	addq	8(%rsp), %r13
.L706:
	cmpl	32(%rsp), %ebx
	je	.L564
	movzwl	0(%r13,%r15), %edx
	movw	$0, 0(%r13,%r15)
	testw	%dx, %dx
	je	.L564
	movzwl	%dx, %edx
	movl	%r14d, %ecx
	movq	16(%rsp), %rsi
	movq	%r13, %rdi
	call	addmul1
.L564:
	addl	$1, 32(%rsp)
	addq	8(%rsp), %r13
.L705:
	cmpl	32(%rsp), %ebx
	je	.L566
	movzwl	0(%r13,%r15), %r8d
	movw	$0, 0(%r13,%r15)
	testw	%r8w, %r8w
	je	.L566
	movzwl	%r8w, %edx
	movl	%r14d, %ecx
	movq	16(%rsp), %rsi
	movq	%r13, %rdi
	call	addmul1
.L566:
	addl	$1, 32(%rsp)
	addq	8(%rsp), %r13
	cmpl	%r14d, 32(%rsp)
	jne	.L790
	jmp	.L451
.L733:
	movzwl	0(%r13,%r15), %ecx
	movw	$0, 0(%r13,%r15)
	testw	%cx, %cx
	je	.L568
	movzwl	%cx, %edx
	movl	%r14d, %ecx
	movq	16(%rsp), %rsi
	movq	%r13, %rdi
	call	addmul1
.L568:
	leal	1(%rbp), %esi
	addq	%r12, %r13
	cmpl	%esi, %ebx
	je	.L569
	movzwl	0(%r13,%r15), %r11d
	movw	$0, 0(%r13,%r15)
	testw	%r11w, %r11w
	je	.L569
	movzwl	%r11w, %edx
	movl	%r14d, %ecx
	movq	16(%rsp), %rsi
	movq	%r13, %rdi
	call	addmul1
.L569:
	leal	2(%rbp), %r10d
	addq	%r12, %r13
	cmpl	%r10d, %ebx
	je	.L570
	movzwl	0(%r13,%r15), %r9d
	movw	$0, 0(%r13,%r15)
	testw	%r9w, %r9w
	je	.L570
	movzwl	%r9w, %edx
	movl	%r14d, %ecx
	movq	16(%rsp), %rsi
	movq	%r13, %rdi
	call	addmul1
.L570:
	addl	$3, %ebp
	addq	%r12, %r13
	cmpl	%r14d, %ebp
	jne	.L453
	movq	112(%rsp), %r12
	movq	120(%rsp), %rbp
	jmp	.L451
.L577:
	movl	$0, %r13d
	jmp	.L743
.L730:
	testl	%ecx, %ecx
	je	.L577
	leaq	gf_log(%rip), %r11
	movslq	%r8d, %r13
	movl	(%r11,%rsi,4), %r10d
	addl	(%r11,%r13,4), %r10d
	movslq	%r10d, %r11
	leaq	gf_exp(%rip), %r8
	movzwl	(%r8,%r11,2), %r13d
.L743:
	movq	16(%rsp), %r10
	movw	%r13w, (%r10)
	addq	$2, %rax
	cmpq	%rdi, %rax
	je	.L448
	testq	%rdx, %rdx
	je	.L450
	cmpq	$1, %rdx
	je	.L707
	cmpq	$2, %rdx
	je	.L708
	movq	%rax, %r11
	movzwl	2(%r10), %edx
	testl	%edx, %edx
	jne	.L744
.L578:
	movl	$0, %r8d
	jmp	.L745
.L744:
	testl	%ecx, %ecx
	je	.L578
	leaq	gf_log(%rip), %r9
	movslq	%edx, %r8
	movl	(%r9,%rsi,4), %r13d
	addl	(%r9,%r8,4), %r13d
	movslq	%r13d, %r9
	leaq	gf_exp(%rip), %rdx
	movzwl	(%rdx,%r9,2), %r8d
.L745:
	movw	%r8w, (%r11)
	addq	$2, %rax
.L708:
	movq	%rax, %r11
	movzwl	(%rax), %r13d
	testl	%r13d, %r13d
	jne	.L746
.L580:
	movl	$0, %edx
	jmp	.L747
.L746:
	testl	%ecx, %ecx
	je	.L580
	leaq	gf_log(%rip), %r9
	movslq	%r13d, %rdx
	movl	(%r9,%rsi,4), %r10d
	addl	(%r9,%rdx,4), %r10d
	movslq	%r10d, %r13
	leaq	gf_exp(%rip), %r9
	movzwl	(%r9,%r13,2), %edx
.L747:
	movw	%dx, (%r11)
	addq	$2, %rax
.L707:
	movq	%rax, %r11
	movzwl	(%rax), %r10d
	testl	%r10d, %r10d
	jne	.L748
.L582:
	movl	$0, %r13d
	jmp	.L749
.L748:
	testl	%ecx, %ecx
	je	.L582
	leaq	gf_log(%rip), %r8
	movslq	%r10d, %r13
	movl	(%r8,%rsi,4), %r9d
	addl	(%r8,%r13,4), %r9d
	movslq	%r9d, %r10
	leaq	gf_exp(%rip), %r8
	movzwl	(%r8,%r10,2), %r13d
.L749:
	movw	%r13w, (%r11)
	addq	$2, %rax
	cmpq	%rdi, %rax
	jne	.L450
	jmp	.L448
.L584:
	movl	$0, %eax
	jmp	.L750
.L731:
	testl	%ecx, %ecx
	.p2align 4,,4
	je	.L584
	leaq	gf_log(%rip), %r10
	cltq
	movl	(%r10,%rsi,4), %r13d
	addl	(%r10,%rax,4), %r13d
	movslq	%r13d, %rdx
	leaq	gf_exp(%rip), %r8
	movzwl	(%r8,%rdx,2), %eax
.L750:
	movw	%ax, (%r11)
	movzwl	2(%r11), %eax
	testl	%eax, %eax
	jne	.L751
.L585:
	movl	$0, %eax
	jmp	.L752
.L751:
	testl	%ecx, %ecx
	je	.L585
	leaq	gf_log(%rip), %r10
	cltq
	movl	(%r10,%rsi,4), %r13d
	addl	(%r10,%rax,4), %r13d
	movslq	%r13d, %rdx
	leaq	gf_exp(%rip), %r8
	movzwl	(%r8,%rdx,2), %eax
.L752:
	movw	%ax, 2(%r11)
	movzwl	4(%r11), %eax
	testl	%eax, %eax
	jne	.L753
.L586:
	movl	$0, %eax
	jmp	.L754
.L753:
	testl	%ecx, %ecx
	je	.L586
	leaq	gf_log(%rip), %r10
	cltq
	movl	(%r10,%rsi,4), %r13d
	addl	(%r10,%rax,4), %r13d
	movslq	%r13d, %rdx
	leaq	gf_exp(%rip), %r8
	movzwl	(%r8,%rdx,2), %eax
.L754:
	movw	%ax, 4(%r11)
	leaq	6(%r11), %rax
	cmpq	%rdi, %rax
	jne	.L450
	jmp	.L448
.L729:
	testl	%edi, %edi
	je	.L446
	cmpl	$1, %edi
	.p2align 4,,6
	je	.L709
	cmpl	$2, %edi
	.p2align 4,,3
	je	.L710
	movslq	%esi, %rdi
	leaq	(%r12,%rdi,2), %r13
	movzwl	0(%r13), %r8d
	leal	(%rax,%rsi), %r10d
	movslq	%r10d, %r9
	leaq	(%r12,%r9,2), %r11
	movzwl	(%r11), %edi
	movw	%di, 0(%r13)
	movw	%r8w, (%r11)
	addl	$1, %esi
.L710:
	movslq	%esi, %r13
	leaq	(%r12,%r13,2), %r8
	movzwl	(%r8), %r9d
	leal	(%rax,%rsi), %r10d
	movslq	%r10d, %r11
	leaq	(%r12,%r11,2), %rdi
	movzwl	(%rdi), %r13d
	movw	%r13w, (%r8)
	movw	%r9w, (%rdi)
	addl	$1, %esi
.L709:
	movslq	%esi, %r8
	leaq	(%r12,%r8,2), %r11
	movzwl	(%r11), %r9d
	leal	(%rax,%rsi), %r10d
	movslq	%r10d, %rdi
	leaq	(%r12,%rdi,2), %r13
	movzwl	0(%r13), %r8d
	movw	%r8w, (%r11)
	movw	%r9w, 0(%r13)
	addl	$1, %esi
	cmpl	%ecx, %esi
	jne	.L446
	jmp	.L445
.L601:
	cmpl	$1, %r9d
	jle	.L602
	.p2align 4,,7
	jmp	.L755
.L727:
	movslq	%ecx, %r11
	cmpw	$0, (%r12,%r11,2)
	jne	.L440
.L602:
	addq	$1, %rax
	cmpl	%eax, %r14d
	jle	.L438
	testl	%edi, %edi
	je	.L443
	cmpl	$1, %edi
	je	.L711
	cmpl	$2, %edi
	.p2align 4,,3
	je	.L712
	cmpl	$3, %edi
	.p2align 4,,2
	je	.L713
	cmpl	$4, %edi
	.p2align 4,,2
	je	.L714
	cmpl	$5, %edi
	.p2align 4,,2
	je	.L715
	cmpl	$6, %edi
	.p2align 4,,2
	je	.L716
	movl	%eax, %ebx
	movl	0(%rbp,%rax,4), %r13d
	testl	%r13d, %r13d
	je	.L756
	cmpl	$1, %r13d
	jle	.L604
	jmp	.L755
.L756:
	leal	(%rcx,%rax), %r10d
	movslq	%r10d, %r15
	cmpw	$0, (%r12,%r15,2)
	jne	.L440
.L604:
	addq	$1, %rax
.L716:
	movl	%eax, %ebx
	movl	0(%rbp,%rax,4), %edi
	testl	%edi, %edi
	je	.L757
	cmpl	$1, %edi
	jle	.L607
	jmp	.L755
.L757:
	leal	(%rcx,%rax), %r9d
	movslq	%r9d, %r11
	cmpw	$0, (%r12,%r11,2)
	jne	.L440
.L607:
	addq	$1, %rax
.L715:
	movl	%eax, %ebx
	movl	0(%rbp,%rax,4), %r13d
	testl	%r13d, %r13d
	je	.L758
	cmpl	$1, %r13d
	jle	.L610
	jmp	.L755
.L758:
	leal	(%rcx,%rax), %r10d
	movslq	%r10d, %r15
	cmpw	$0, (%r12,%r15,2)
	jne	.L440
.L610:
	addq	$1, %rax
.L714:
	movl	%eax, %ebx
	movl	0(%rbp,%rax,4), %edi
	testl	%edi, %edi
	je	.L759
	cmpl	$1, %edi
	jle	.L613
	jmp	.L755
.L759:
	leal	(%rcx,%rax), %r9d
	movslq	%r9d, %r11
	cmpw	$0, (%r12,%r11,2)
	jne	.L440
.L613:
	addq	$1, %rax
.L713:
	movl	%eax, %ebx
	movl	0(%rbp,%rax,4), %r13d
	testl	%r13d, %r13d
	je	.L760
	cmpl	$1, %r13d
	jle	.L616
	jmp	.L755
.L760:
	leal	(%rcx,%rax), %r10d
	movslq	%r10d, %r15
	cmpw	$0, (%r12,%r15,2)
	jne	.L440
.L616:
	addq	$1, %rax
.L712:
	movl	%eax, %ebx
	movl	0(%rbp,%rax,4), %edi
	testl	%edi, %edi
	je	.L761
	cmpl	$1, %edi
	jle	.L619
	jmp	.L755
.L761:
	leal	(%rcx,%rax), %r9d
	movslq	%r9d, %r11
	cmpw	$0, (%r12,%r11,2)
	jne	.L440
.L619:
	addq	$1, %rax
.L711:
	movl	%eax, %ebx
	movl	0(%rbp,%rax,4), %r13d
	testl	%r13d, %r13d
	je	.L762
	cmpl	$1, %r13d
	jle	.L622
	jmp	.L755
.L762:
	leal	(%rcx,%rax), %r10d
	movslq	%r10d, %r15
	cmpw	$0, (%r12,%r15,2)
	jne	.L440
.L622:
	addq	$1, %rax
	cmpl	%eax, %r14d
	jg	.L443
	jmp	.L438
.L624:
	cmpl	$1, %r10d
	jle	.L625
	.p2align 4,,7
	jmp	.L755
.L728:
	addl	%ecx, %r13d
	movslq	%r13d, %r15
	cmpw	$0, (%r12,%r15,2)
	jne	.L440
.L625:
	leaq	1(%rax), %rdi
	movl	%edi, %ebx
	movl	0(%rbp,%rdi,4), %r9d
	testl	%r9d, %r9d
	je	.L763
	cmpl	$1, %r9d
	jle	.L627
	jmp	.L755
.L763:
	addl	%ecx, %edi
	movslq	%edi, %r11
	cmpw	$0, (%r12,%r11,2)
	jne	.L440
.L627:
	leaq	2(%rax), %r13
	movl	%r13d, %ebx
	movl	0(%rbp,%r13,4), %r10d
	testl	%r10d, %r10d
	je	.L764
	cmpl	$1, %r10d
	jle	.L629
	jmp	.L755
.L764:
	addl	%ecx, %r13d
	movslq	%r13d, %r15
	cmpw	$0, (%r12,%r15,2)
	jne	.L440
.L629:
	leaq	3(%rax), %rdi
	movl	%edi, %ebx
	movl	0(%rbp,%rdi,4), %r9d
	testl	%r9d, %r9d
	je	.L765
	cmpl	$1, %r9d
	jle	.L631
	jmp	.L755
.L765:
	addl	%ecx, %edi
	movslq	%edi, %r11
	cmpw	$0, (%r12,%r11,2)
	jne	.L440
.L631:
	leaq	4(%rax), %r13
	movl	%r13d, %ebx
	movl	0(%rbp,%r13,4), %r10d
	testl	%r10d, %r10d
	je	.L766
	cmpl	$1, %r10d
	jle	.L633
	jmp	.L755
.L766:
	addl	%ecx, %r13d
	movslq	%r13d, %r15
	cmpw	$0, (%r12,%r15,2)
	jne	.L440
.L633:
	leaq	5(%rax), %rdi
	movl	%edi, %ebx
	movl	0(%rbp,%rdi,4), %r9d
	testl	%r9d, %r9d
	je	.L767
	cmpl	$1, %r9d
	jle	.L635
	jmp	.L755
.L767:
	addl	%ecx, %edi
	movslq	%edi, %r11
	cmpw	$0, (%r12,%r11,2)
	jne	.L440
.L635:
	leaq	6(%rax), %r13
	movl	%r13d, %ebx
	movl	0(%rbp,%r13,4), %r10d
	testl	%r10d, %r10d
	je	.L768
	cmpl	$1, %r10d
	jle	.L637
	jmp	.L755
.L768:
	addl	%ecx, %r13d
	movslq	%r13d, %r15
	cmpw	$0, (%r12,%r15,2)
	jne	.L440
.L637:
	addq	$7, %rax
	cmpl	%eax, %r14d
	jg	.L443
	jmp	.L438
.L690:
	testl	%eax, %eax
	.p2align 4,,2
	je	.L474
	cmpl	$1, %eax
	.p2align 4,,6
	je	.L717
	cmpl	$2, %eax
	.p2align 4,,3
	je	.L718
	cmpl	$3, %eax
	.p2align 4,,2
	je	.L719
	cmpl	$4, %eax
	.p2align 4,,2
	je	.L720
	cmpl	$5, %eax
	.p2align 4,,2
	je	.L721
	cmpl	$6, %eax
	.p2align 4,,2
	je	.L722
	movl	$0, 0(%rbp,%r10,4)
	addq	$1, %r10
.L722:
	movl	$0, 0(%rbp,%r10,4)
	addq	$1, %r10
.L721:
	movl	$0, 0(%rbp,%r10,4)
	addq	$1, %r10
.L720:
	movl	$0, 0(%rbp,%r10,4)
	addq	$1, %r10
.L719:
	movl	$0, 0(%rbp,%r10,4)
	addq	$1, %r10
.L718:
	movl	$0, 0(%rbp,%r10,4)
	addq	$1, %r10
.L717:
	movl	$0, 0(%rbp,%r10,4)
	addq	$1, %r10
	cmpl	%r10d, %r13d
	jg	.L474
	jmp	.L741
.L673:
	movl	12(%rbx), %edx
	cmpl	%edx, %ecx
	jge	.L429
	imull	%r13d, %ecx
	movslq	%ecx, %rdi
	movq	16(%rbx), %r8
	leaq	(%r8,%rdi,2), %rsi
	movq	8(%rsp), %rdx
	movq	%r10, %rdi
	call	memmove@PLT
	jmp	.L769
.L740:
	movq	8(%rsp), %rdx
	movl	$0, %esi
	movq	%r10, %rdi
	call	memset@PLT
	movq	16(%rsp), %r9
	movw	$1, (%r9)
.L769:
	movq	8(%rsp), %r11
	addq	%r11, %r12
	addq	$1, %rbp
	movq	16(%rsp), %r10
	addq	(%rsp), %r10
	movq	%r10, 16(%rsp)
	cmpl	%ebp, %r13d
	jle	.L770
	movl	32(%rsp), %eax
	testl	%eax, %eax
	je	.L814
	cmpl	$1, %eax
	je	.L724
	cmpl	$2, %eax
	je	.L725
	movl	(%r14,%rbp,4), %ecx
	cmpl	%ecx, %r13d
	jg	.L771
	movl	12(%rbx), %edx
	cmpl	%edx, %ecx
	jge	.L429
	imull	%r13d, %ecx
	movslq	%ecx, %rsi
	movq	16(%rbx), %rdx
	leaq	(%rdx,%rsi,2), %rsi
	movq	%r11, %rdx
	movq	%r12, %rdi
	call	memmove@PLT
	jmp	.L772
.L771:
	movq	8(%rsp), %rdx
	movl	$0, %esi
	movq	%r12, %rdi
	call	memset@PLT
	movq	16(%rsp), %rdi
	movw	$1, (%rdi)
.L772:
	addq	8(%rsp), %r12
	addq	$1, %rbp
	movq	(%rsp), %r8
	addq	%r8, 16(%rsp)
.L725:
	movl	(%r14,%rbp,4), %ecx
	cmpl	%ecx, %r13d
	jg	.L773
	movl	12(%rbx), %edx
	cmpl	%edx, %ecx
	jge	.L429
	imull	%r13d, %ecx
	movslq	%ecx, %r11
	movq	16(%rbx), %r10
	leaq	(%r10,%r11,2), %rsi
	movq	8(%rsp), %rdx
	movq	%r12, %rdi
	call	memmove@PLT
	jmp	.L774
.L773:
	movq	8(%rsp), %rdx
	movl	$0, %esi
	movq	%r12, %rdi
	call	memset@PLT
	movq	16(%rsp), %rax
	movw	$1, (%rax)
.L774:
	addq	8(%rsp), %r12
	addq	$1, %rbp
	movq	(%rsp), %rcx
	addq	%rcx, 16(%rsp)
.L724:
	movl	(%r14,%rbp,4), %ecx
	cmpl	%ecx, %r13d
	jg	.L775
	movl	12(%rbx), %edx
	cmpl	%edx, %ecx
	jge	.L429
	imull	%r13d, %ecx
	movslq	%ecx, %rdx
	movq	16(%rbx), %rdi
	leaq	(%rdi,%rdx,2), %rsi
	movq	8(%rsp), %rdx
	movq	%r12, %rdi
	call	memmove@PLT
	jmp	.L776
.L775:
	movq	8(%rsp), %rdx
	movl	$0, %esi
	movq	%r12, %rdi
	call	memset@PLT
	movq	16(%rsp), %r8
	movw	$1, (%r8)
.L776:
	addq	8(%rsp), %r12
	addq	$1, %rbp
	movq	(%rsp), %r9
	addq	%r9, 16(%rsp)
	cmpl	%ebp, %r13d
	jg	.L815
	jmp	.L770
.L684:
	movl	12(%rbx), %edx
	cmpl	%edx, %ecx
	jge	.L429
	imull	%r13d, %ecx
	movslq	%ecx, %rsi
	movq	16(%rbx), %rdx
	leaq	(%rdx,%rsi,2), %rsi
	movq	%r15, %rdx
	movq	%r12, %rdi
	call	memmove@PLT
	jmp	.L777
.L726:
	movq	%r15, %rdx
	movl	$0, %esi
	movq	%r12, %rdi
	call	memset@PLT
	movq	16(%rsp), %rdi
	movw	$1, (%rdi)
.L777:
	addq	%r15, %r12
	leaq	1(%rbp), %r8
	movq	16(%rsp), %r9
	addq	(%rsp), %r9
	movq	%r9, 16(%rsp)
	movl	(%r14,%r8,4), %ecx
	cmpl	%ecx, %r13d
	jg	.L778
	movl	12(%rbx), %edx
	cmpl	%edx, %ecx
	jge	.L429
	imull	%r13d, %ecx
	movslq	%ecx, %r11
	movq	16(%rbx), %r10
	leaq	(%r10,%r11,2), %rsi
	movq	%r15, %rdx
	movq	%r12, %rdi
	call	memmove@PLT
	jmp	.L779
.L778:
	movq	%r15, %rdx
	movl	$0, %esi
	movq	%r12, %rdi
	call	memset@PLT
	movq	16(%rsp), %rax
	movw	$1, (%rax)
.L779:
	addq	%r15, %r12
	leaq	2(%rbp), %rcx
	movq	16(%rsp), %rsi
	addq	(%rsp), %rsi
	movq	%rsi, 16(%rsp)
	movl	(%r14,%rcx,4), %ecx
	cmpl	%ecx, %r13d
	jg	.L780
	movl	12(%rbx), %edx
	cmpl	%edx, %ecx
	jge	.L429
	imull	%r13d, %ecx
	movslq	%ecx, %rdi
	movq	16(%rbx), %r8
	leaq	(%r8,%rdi,2), %rsi
	movq	%r15, %rdx
	movq	%r12, %rdi
	call	memmove@PLT
	jmp	.L781
.L780:
	movq	%r15, %rdx
	movl	$0, %esi
	movq	%r12, %rdi
	call	memset@PLT
	movq	16(%rsp), %r9
	movw	$1, (%r9)
.L781:
	addq	%r15, %r12
	addq	$3, %rbp
	movq	16(%rsp), %r11
	addq	(%rsp), %r11
	movq	%r11, 16(%rsp)
	cmpl	%ebp, %r13d
	jg	.L473
	movl	32(%rsp), %r15d
	jmp	.L770
	.cfi_endproc
.LFE74:
	.size	fec_decode, .-fec_decode
	.local	fec_initialized
	.comm	fec_initialized,4,4
	.local	inverse
	.comm	inverse,131072,32
	.local	gf_log
	.comm	gf_log,262144,32
	.local	gf_exp
	.comm	gf_exp,262140,32
	.ident	"GCC: (Ubuntu/Linaro 4.8.1-10ubuntu8) 4.8.1"
	.section	.note.GNU-stack,"",@progbits
