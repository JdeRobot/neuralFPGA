#ifndef HAL_H
#define HAL_H

#include <gpio.h>
#include <timer.h>

#define CORE_HZ 12000000

#define GPIOA    ((GPIO_Reg*)(GPIOA_BASE_ADDR))
#define TIMER    ((Timer_Reg*)(TIMER_BASE_ADDR))

#endif //HAL_H