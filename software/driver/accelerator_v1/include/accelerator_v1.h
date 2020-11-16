#pragma once

#include <stdint.h>
#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif  // __cplusplus

enum accelerator_v1_mode_t {
  ACCELERATOR_V1_CONV2D_3x3
};

struct accelerator_v1_conv2d_3x3_data_t {
  //params
  int input_width;
  int output_channel_count;

  //private data
  size_t output_idx;
  size_t x;
  uint32_t x_edge;
  uint32_t x_max;
};

struct accelerator_v1_instance_t {
  uint32_t base_address;
  enum accelerator_v1_mode_t mode;

  void *mode_data;
};

int accelerator_v1_init(struct accelerator_v1_instance_t *instance,
                         const uint32_t base_address,
                         const enum accelerator_v1_mode_t mode,
                         void *mode_data);

size_t accelerator_v1_get_x_fifo_availability(struct accelerator_v1_instance_t *instance);
size_t accelerator_v1_get_z_fifo_ocupancy(struct accelerator_v1_instance_t *instance);

size_t accelerator_v1_set_input(
    struct accelerator_v1_instance_t *instance, const int8_t data[],
    const size_t data_len);

size_t accelerator_v1_set_filter(
    struct accelerator_v1_instance_t *instance,
    const uint8_t filter_index, const int8_t data[],
    const size_t data_len);

size_t accelerator_v1_get_output(
    struct accelerator_v1_instance_t *instance, int32_t data[],
    const size_t data_len);

size_t accelerator_v1_accumulate_output(
    struct accelerator_v1_instance_t *instance, int32_t data[],
    const size_t data_len);

#ifdef __cplusplus
}  // extern "C"
#endif  // __cplusplus