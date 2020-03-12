  .section .text.entry
  .globl _start

_start:
.option push
.option norelax
  la gp, __global_pointer$
.option pop
  la sp, __stack_top

  # perform the rest of initialization in C
  j _init
