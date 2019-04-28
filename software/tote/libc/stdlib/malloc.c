#include <stddef.h>
#include <stdio.h>

void *malloc(size_t size) {
    puts("malloc called!");
    return 0;
}