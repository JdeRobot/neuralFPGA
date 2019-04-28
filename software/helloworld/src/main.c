#include <hal.h>
#include <stdio.h>


void main() {
  while (1) {
    printf("%llu: Hello World!\n", TIMER->COUNTER);
  }
}