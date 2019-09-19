#ifndef HAL_H
#define HAL_H
#ifdef __cplusplus
extern "C" {
#endif

#include <gpio.h>
#include <timer.h>

#define CORE_HZ 12000000

extern GPIO_Reg *hal_gpioA (void);
extern Timer_Reg *hal_timer (void);

#define GPIOA    (hal_gpioA())
#define TIMER    (hal_timer())

#ifdef __cplusplus
}
#endif
#endif //HAL_H