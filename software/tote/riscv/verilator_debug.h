#ifndef VERILATOR_DEBUG
#define VERILATOR_DEBUG

#include <stdint.h>

static inline uint8_t noop_write(int _fd, const uint8_t _c) {
    register uint8_t c asm("a0") = _c;

    asm volatile ("addi x0, %0, 0" :: "r"(c));
    (void)_fd;//TODO: can we read this from verilator to have stdout + stderr output??

    return c;
}

#endif //VERILATOR_DEBUG