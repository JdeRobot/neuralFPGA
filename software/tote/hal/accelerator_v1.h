#ifndef ACCELERATOR_V1_H
#define ACCELERATOR_V1_H

#include <stdint.h>

#define ACCELERATOR_V1_BASE_ADDR 0xB0000000UL

typedef struct
{
  volatile uint32_t STATUS;
  volatile uint32_t X;
  volatile uint32_t Z;
} Accelerator_V1_Reg;

#endif //ACCELERATOR_V1_H