#pragma once

#include <stdint.h>

#define X_FIFO_AVAILABILITY(STATUS) ((uint16_t)((STATUS) & 0xFFFF))
#define Z_FIFO_OCUPANCY(STATUS) ((uint16_t)(((STATUS) >> 16) & 0xFFFF))

#define CONFIG_ROW_WIDTH(CONFIG) ((uint16_t)((CONFIG) & 0xFFFF))
#define CONFIG_INITIAL_DELAY(CONFIG) ((uint16_t)(((CONFIG) >> 16) & 0xFFFF))

struct accelerator_v1_reg
{
  volatile uint32_t STATUS; /* 0x00 */
  volatile uint32_t CONFIG; /* 0x04 */
  volatile uint32_t X; /* 0x08 */
  volatile uint32_t Z; /* 0x0C */
};