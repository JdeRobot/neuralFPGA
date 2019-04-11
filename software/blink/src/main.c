#include <stdint.h>
#include <stddef.h>

#include "hal.h"

const uint8_t a[] = {
    0, 0, 0, 0, 1, 1, 1, 1, 2, 2, 2, 2, 3, 3, 3, 3,
    4, 4, 4, 4, 5, 5, 5, 5, 6, 6, 6, 6, 7, 7, 7, 7,
};
const size_t a_size = sizeof(a) / sizeof(a[0]);

void main() {
  LEDS->STATE = 0;

  while (1) {
    for (uint8_t i = 0; i < a_size; i++) {
      LEDS->STATE = a[i];
    }
  }
}