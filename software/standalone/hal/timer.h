#pragma once

#include <stdint.h>

typedef struct
{
  volatile uint64_t COUNTER;
  volatile uint64_t CMP;
  volatile uint32_t RESET;
} Timer_Reg;