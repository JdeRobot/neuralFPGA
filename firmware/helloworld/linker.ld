/* adapted from https://raw.githubusercontent.com/RISCV-on-Microsemi-FPGA/SoftConsole/master/riscv-simple-baremetal-bootloader/riscv_hal/microsemi-riscv-ram.ld */

INCLUDE generated/output_format.ld
ENTRY(_start)
INCLUDE generated/regions.ld

__stack_size = DEFINED(__stack_size) ? __stack_size : 2K;

SECTIONS
{
    .text :
    {
        KEEP (*(SORT_NONE(.text.entry)))   
        . = ALIGN(4);
        *(.text .text.* .gnu.linkonce.t.*)
        *(.plt)
        . = ALIGN(4);

        KEEP (*crtbegin.o(.ctors))
        KEEP (*(EXCLUDE_FILE (*crtend.o) .ctors))
        KEEP (*(SORT(.ctors.*)))
        KEEP (*crtend.o(.ctors))
        KEEP (*crtbegin.o(.dtors))
        KEEP (*(EXCLUDE_FILE (*crtend.o) .dtors))
        KEEP (*(SORT(.dtors.*)))
        KEEP (*crtend.o(.dtors))

        *(.rodata .rodata.* .gnu.linkonce.r.*)
        *(.gcc_except_table) 
        *(.eh_frame_hdr)
        *(.eh_frame)

        KEEP (*(.init))
        KEEP (*(.fini))

        PROVIDE_HIDDEN (__preinit_array_start = .);
        KEEP (*(.preinit_array))
        PROVIDE_HIDDEN (__preinit_array_end = .);
        PROVIDE_HIDDEN (__init_array_start = .);
        KEEP (*(SORT(.init_array.*)))
        KEEP (*(.init_array))
        PROVIDE_HIDDEN (__init_array_end = .);
        PROVIDE_HIDDEN (__fini_array_start = .);
        KEEP (*(.fini_array))
        KEEP (*(SORT(.fini_array.*)))
        PROVIDE_HIDDEN (__fini_array_end = .);
        . = ALIGN(4);

    } > main_ram

    /* short/global data section */
    .sdata : ALIGN(4)
    {
        __sdata_load = LOADADDR(.sdata);
        __sdata_start = .; 
        PROVIDE( __global_pointer$ = . + 0x800);
        *(.srodata.cst16) *(.srodata.cst8) *(.srodata.cst4) *(.srodata.cst2)
        *(.srodata*)
        *(.sdata .sdata.* .gnu.linkonce.s.*)
        . = ALIGN(4);
        __sdata_end = .;
    } > main_ram

    /* data section */
    .data : ALIGN(4)
    {
        __data_load = LOADADDR(.data);
        __data_start = .; 
        *(.got.plt) *(.got)
        *(.shdata)
        *(.data .data.* .gnu.linkonce.d.*)
        . = ALIGN(4);
        __data_end = .;
    } > main_ram

    /* sbss section */
    .sbss : ALIGN(4)
    {
        __sbss_start = .;
        *(.sbss .sbss.* .gnu.linkonce.sb.*)
        *(.scommon)
        . = ALIGN(0x10);
        __sbss_end = .;
    } > sram
  
    /* sbss section */
    .bss : ALIGN(4)
    { 
        __bss_start = .;
        *(.shbss)
        *(.bss .bss.* .gnu.linkonce.b.*)
        *(COMMON)
        . = ALIGN(4);
        __bss_end = .;
    } > sram
}

PROVIDE(__stack_top = ORIGIN(sram) + LENGTH(sram));

PROVIDE(_heap_start = __data_end);
PROVIDE(_heap_end = ORIGIN(main_ram) + LENGTH(main_ram));
