// adapted from https://raw.githubusercontent.com/RISCV-on-Microsemi-FPGA/SoftConsole/master/riscv-simple-baremetal-bootloader/riscv_hal/init.c
// with https://github.com/RISCV-on-Microsemi-FPGA/SoftConsole/pull/19
#include <stdint.h>
#include <stdlib.h>

#ifdef __cplusplus
extern "C" {
#endif

extern uint32_t     __sdata_load;
extern uint32_t     __sdata_start;
extern uint32_t     __sdata_end;

extern uint32_t     __data_load;
extern uint32_t     __data_start;
extern uint32_t     __data_end;

extern uint32_t     __sbss_start;
extern uint32_t     __sbss_end;
extern uint32_t     __bss_start;
extern uint32_t     __bss_end;


static void copy_section(uint32_t * p_load, uint32_t * p_vma, uint32_t * p_vma_end)
{
    while(p_vma < p_vma_end)
    {
        *p_vma = *p_load;
        ++p_load;
        ++p_vma;
    }
}

static void zero_section(uint32_t * start, uint32_t * end)
{
    uint32_t * p_zero = start;
    
    while(p_zero < end)
    {
        *p_zero = 0;
        ++p_zero;
    }
}

void _init(void)
{
    extern int main();

    copy_section(&__sdata_load, &__sdata_start, &__sdata_end);
    copy_section(&__data_load, &__data_start, &__data_end);
    zero_section(&__sbss_start, &__sbss_end);
    zero_section(&__bss_start, &__bss_end);
    
    exit(main());
}

#ifdef __cplusplus
}
#endif