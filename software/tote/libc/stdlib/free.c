#include <stddef.h>
#include <stdio.h>

void free(void *ptr) {
    puts("free called!");
    return;
}