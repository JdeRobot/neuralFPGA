#include <assert.h>
#include <hal/accelerator_v1.h>
#include <hal/hal.h>
#include <inttypes.h>
#include <stdio.h>
#include <time.h>
#include "conv2d.h"

#define FILTER_HEIGHT 3
#define FILTER_WIDTH 3
uint8_t filter_data[FILTER_HEIGHT * FILTER_WIDTH] = {
    1, 1, 1, 1, 1, 1, 1, 1, 1,
};

#define INPUT_HEIGHT 28
#define INPUT_WIDTH 28
// clang-format off
uint8_t input_data[INPUT_HEIGHT*INPUT_WIDTH] = {
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
  1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28,
};
// clang-format on

// 0 padding, stride 1
#define OUTPUT_HEIGHT ((INPUT_HEIGHT - FILTER_HEIGHT) + 1)
#define OUTPUT_WIDTH ((INPUT_WIDTH - FILTER_WIDTH) + 1)
uint32_t output_data_sw[OUTPUT_HEIGHT * OUTPUT_WIDTH] = {0};
uint32_t output_data_hw[OUTPUT_HEIGHT * OUTPUT_WIDTH] = {0};

size_t copy_data_to_accelerator(struct accelerator_v1_reg *accel,
                                const uint32_t data[], const size_t data_len) {
  size_t copy_size = data_len;
  if (copy_size > X_FIFO_AVAILABILITY(accel->STATUS)) {
    copy_size = X_FIFO_AVAILABILITY(accel->STATUS);
  }

  for (size_t i = 0; i < copy_size; i++) {
    accel->X = data[i];
  }

  return copy_size;
}

int main() {
  clock_t conv2d_sw_t0 = clock();
  conv2d(input_data, INPUT_HEIGHT, INPUT_WIDTH, filter_data, FILTER_HEIGHT,
         FILTER_WIDTH, 0, output_data_sw, OUTPUT_HEIGHT, OUTPUT_WIDTH);
  clock_t conv2d_sw_elapsed = clock() - conv2d_sw_t0;

  printf("conv2d_sw took: %lu\n", conv2d_sw_elapsed);

  assert(X_FIFO_AVAILABILITY(ACCELERATOR_V1->STATUS) == 256);
  assert(Z_FIFO_OCUPANCY(ACCELERATOR_V1->STATUS) == 0);
  assert(CONFIG_ROW_WIDTH(ACCELERATOR_V1->CONFIG) == 0);
  assert(CONFIG_INITIAL_DELAY(ACCELERATOR_V1->CONFIG) == 0);

  ACCELERATOR_V1->CONFIG = ((INPUT_WIDTH * 2 + 2) << 16) | (INPUT_WIDTH);

  assert(CONFIG_ROW_WIDTH(ACCELERATOR_V1->CONFIG) == INPUT_WIDTH);
  assert(CONFIG_INITIAL_DELAY(ACCELERATOR_V1->CONFIG) == (INPUT_WIDTH * 2 + 2));

  clock_t conv2d_hw_t0 = clock();  
  size_t n = copy_data_to_accelerator(ACCELERATOR_V1, (uint32_t *)input_data,
                                      sizeof(input_data) / sizeof(uint32_t));
  assert(n <= sizeof(input_data) / sizeof(uint32_t));

  while (1) {
    if (Z_FIFO_OCUPANCY(ACCELERATOR_V1->STATUS) != 0) break;
  }


  uint32_t z = 0;
  uint32_t z_count = 0;
  for (int y = 0; y < OUTPUT_HEIGHT; y++) {
    for (int x = 0; x < OUTPUT_WIDTH + 2; x++) {
      z = ACCELERATOR_V1->Z;
      z_count++;

      if (x >= OUTPUT_WIDTH) continue;  // edge handling
      output_data_hw[(y * OUTPUT_WIDTH) + x] = z;

      if (Z_FIFO_OCUPANCY(ACCELERATOR_V1->STATUS) == 0) goto z_empty;
    }
  }
z_empty:
  // all rows but the last one get two extra pixels from edge wrap
  assert(z_count ==
         (((OUTPUT_HEIGHT - 1) * (OUTPUT_WIDTH + 2)) + OUTPUT_WIDTH));
  clock_t conv2d_hw_elapsed = clock() - conv2d_hw_t0;

  printf("conv2d_hw took: %lu\n", conv2d_hw_elapsed);

  for (int y = 0; y < OUTPUT_HEIGHT; y++) {
    for (int x = 0; x < OUTPUT_WIDTH; x++) {
      assert(output_data_hw[(y * OUTPUT_WIDTH) + x] == output_data_sw[(y * OUTPUT_WIDTH) + x]);
    }
  }

  return 0;
}
