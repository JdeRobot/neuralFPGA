#include <hal.h>
#include <stdio.h>
#include <inttypes.h>

int main() {
  printf("%" PRIu64 ": Hello World!\n", TIMER->COUNTER);

  return 0;
}