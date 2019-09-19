#ifndef GPIO_H
#define GPIO_H

#include <stdint.h>

typedef struct
{
  volatile uint32_t READ;
  volatile uint32_t WRITE;
  volatile uint32_t WRITEEN;
} GPIO_Reg;

#endif //GPIO_H