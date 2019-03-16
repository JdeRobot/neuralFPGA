    .section .text.entry
  	.globl _start

_start:
#ifdef USE_GP
.option push
.option norelax
	la gp, __global_pointer$
.option pop
#endif
	la sp, __stack_top

	# perform the rest of initialization in C
  	j _init
