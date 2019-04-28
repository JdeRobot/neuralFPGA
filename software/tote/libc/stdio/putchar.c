#include <stdio.h>

int putchar(int c) {
    return putc(c, 0);
}