#pragma once

#include "gpio.h"
#include "timer.h"
#include "accelerator_v1.h"

#ifndef CORE_HZ
#define CORE_HZ 1000000
#endif

#define GPIOA    ((GPIO_Reg*)(GPIOA_BASE_ADDR))
#define TIMER    ((Timer_Reg*)(TIMER_BASE_ADDR))
#define OUTPORT  (0xFFFFFFF8)
#define ACCELERATOR_V1 ((struct accelerator_v1_reg*)(ACCELERATOR_V1_BASE_ADDR))