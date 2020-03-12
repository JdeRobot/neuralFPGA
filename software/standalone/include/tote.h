#pragma once

#include <soc.h>
#include "encoding.h"
#include "gpio.h"

#ifndef CORE_HZ
#define CORE_HZ 1000000
#endif

#define GPIOA    ((GPIO_Reg*)(SYSTEM_GPIO_A_BASE_ADDR))

#ifndef HAS_SYSTEM_MACHINE_TIMER
#define HAS_SYSTEM_MACHINE_TIMER 0
#endif

#if HAS_SYSTEM_MACHINE_TIMER
#define MACHINE_TIMER   SYSTEM_MACHINE_TIMER_BASE_ADDR
#define MACHINE_TIMER_HZ   SYSTEM_MACHINE_TIMER_HZ
#define TIMER    ((Timer_Reg*)(MACHINE_TIMER))
#endif

#if HAS_SYSTEM_ACCELERATOR_V1
#define ACCELERATOR_V1 ((struct accelerator_v1_reg*)(SYSTEM_ACCELERATOR_V1_BASE_ADDR))
#endif