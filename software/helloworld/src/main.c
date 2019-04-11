#include <stddef.h>
#include <stdint.h>
#include <stdio.h>

const char msg[] = "Hello World!\n";
const size_t msg_len = sizeof(msg)/sizeof(msg[0]);

void main() {
  while (1) {
    for (size_t i = 0; i < msg_len; i++) {
      putc(msg[i], 0);
    }
  }
}