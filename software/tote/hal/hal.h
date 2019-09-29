#ifndef HAL_H
#define HAL_H

#include "gpio.h"
#include "timer.h"

#ifndef CORE_HZ
#define CORE_HZ 1000000
#endif

#define GPIOA    ((GPIO_Reg*)(GPIOA_BASE_ADDR))
#define TIMER    ((Timer_Reg*)(TIMER_BASE_ADDR))
#define OUTPORT  (0xFFFFFFF8)

#endif //HAL_H