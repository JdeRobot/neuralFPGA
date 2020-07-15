#include <assert.h>
#include <soc.h>
#include <accelerator_v1.h>
#include <accelerator_v1_regs.h>
#include <inttypes.h>
#include <stdio.h>
#include <time.h>
#include "conv2d.h"

#define OUTPUT_CHANNELS 4

#define FILTER_HEIGHT 3
#define FILTER_WIDTH 3
#define FILTER_DEPTH OUTPUT_CHANNELS
//filter shape depth x h x w
int8_t filter_data[FILTER_DEPTH * FILTER_HEIGHT * FILTER_WIDTH] = {
    1, 1, 1, 1, 1, 1, 1, 1, 1,
    2, 2, 2, 2, 2, 2, 2, 2, 2,
    1, 1, 1, 1, 1, 1, 1, 1, 1,
    -1, -1, -1, -1, -1, -1, -1, -1, -1,
};
int8_t filter_data_padded[16] = {0};

#define INPUT_HEIGHT 28
#define INPUT_WIDTH 28
// clang-format off
int8_t input_data[INPUT_HEIGHT*INPUT_WIDTH] = {
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
int32_t output_data_sw[OUTPUT_HEIGHT * OUTPUT_WIDTH * OUTPUT_CHANNELS] = {0};
int32_t output_data_hw[OUTPUT_HEIGHT * OUTPUT_WIDTH * OUTPUT_CHANNELS] = {0};

int main() {
  clock_t conv2d_sw_t0 = clock();
  conv2d(input_data, INPUT_HEIGHT, INPUT_WIDTH, filter_data, FILTER_HEIGHT,
         FILTER_WIDTH, FILTER_DEPTH, output_data_sw, OUTPUT_HEIGHT, OUTPUT_WIDTH, OUTPUT_CHANNELS);
  clock_t conv2d_sw_elapsed = clock() - conv2d_sw_t0;

  printf("conv2d_sw took: %lu\n", conv2d_sw_elapsed);

  struct accelerator_v1_instance_t accelerator;

  if (accelerator_v1_init(&accelerator, ACCELERATOR_V1_BASE_ADDR, ACCELERATOR_V1_CONV2D_3x3, INPUT_WIDTH, OUTPUT_CHANNELS) != 0) {
    printf("accelerator_v1_init failed");
    return -1;
  }

  clock_t conv2d_hw_t0 = clock();
  for (int f = 0; f < FILTER_DEPTH; f++) {
    const int8_t *filter_data_p = filter_data + offset(FILTER_DEPTH, FILTER_HEIGHT * FILTER_WIDTH, 1, f, 0, 0);
    if (accelerator_v1_set_filter(&accelerator, f, filter_data_p, FILTER_HEIGHT*FILTER_WIDTH) == -1) {
      printf("accelerator_v1_set_filter failed");
      return -1;
    }
  }

  //we are assuming input will go in one call, otherwise we need to get results and send more input
  size_t n = accelerator_v1_set_input(&accelerator, input_data, sizeof(input_data) / sizeof(input_data[0]));
  assert(n <= sizeof(input_data) / sizeof(input_data[0]));
  (void)n;

  //wait until data is available
  while (1) {
    if (accelerator_v1_get_z_fifo_ocupancy(&accelerator) != 0) break;
  }

  uint32_t z_count = accelerator_v1_get_output(&accelerator, output_data_hw, sizeof(output_data_hw) / sizeof(output_data_hw[0]));
  assert(z_count == OUTPUT_HEIGHT * OUTPUT_WIDTH * OUTPUT_CHANNELS);
  (void)z_count;
  clock_t conv2d_hw_elapsed = clock() - conv2d_hw_t0;

  printf("conv2d_hw took: %lu\n", conv2d_hw_elapsed);

  for (int y = 0; y < OUTPUT_HEIGHT; y++) {
    for (int x = 0; x < OUTPUT_WIDTH; x++) {
      for (int c = 0; x < OUTPUT_CHANNELS; x++) {
        int idx = offset(OUTPUT_HEIGHT, OUTPUT_WIDTH, OUTPUT_CHANNELS, y, x, c);
        //printf("output_data_hw[%d,%d,%d]: %lu\n", y, x, c, output_data_hw[idx]);
        //printf("output_data_sw[%d,%d,%d]: %lu\n", y, x, c, output_data_sw[idx]);
        assert(output_data_hw[idx] == output_data_sw[idx]);
        (void)idx;
      }
    }
  }

  return 0;
}
