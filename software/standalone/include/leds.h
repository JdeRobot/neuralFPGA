#ifndef LEDS_H
#define LEDS_H

#include <stdint.h>

typedef struct
{
  volatile uint32_t STATE;
} Leds_Reg;

#endif //LEDS_H