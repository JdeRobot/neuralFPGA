#pragma once

#include <stdint.h>

#define GPIOA_BASE_ADDR 0xA0000000UL

typedef struct
{
  volatile uint32_t READ;
  volatile uint32_t WRITE;
  volatile uint32_t WRITEEN;
} GPIO_Reg;