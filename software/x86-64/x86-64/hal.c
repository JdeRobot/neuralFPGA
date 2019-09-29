#include <hal/hal.h>

GPIO_Reg gpioA;
Timer_Reg timer;

GPIO_Reg *hal_gpioA (void) {
    return &gpioA;
}

Timer_Reg *hal_timer (void) {
    return &timer;
}