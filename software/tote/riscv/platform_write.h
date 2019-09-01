#ifndef PLATFORM_WRITE
#define PLATFORM_WRITE

#include <stdint.h>

#define OUTPORT 0xFFFFFFF8

static inline uint8_t platform_write(int _fd, const uint8_t _c) {
    (void)_fd;
    *((volatile uint32_t*) OUTPORT) = _c;
    return _c;
}

#endif //PLATFORM_WRITE