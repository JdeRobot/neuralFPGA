#pragma once

#include <stdint.h>
#include <stddef.h>

enum accelerator_v1_mode_t {
  ACCELERATOR_V1_CONV2D_3x3
};

struct accelerator_v1_instance_t {
  uint32_t base_address;
  enum accelerator_v1_mode_t mode;

  uint16_t window_buffer_row_width;
  uint16_t window_buffer_initial_delay;

  uint16_t output_width;
  uint8_t output_channel_count;
};

int accelerator_v1_init(struct accelerator_v1_instance_t *instance,
                         const uint32_t base_address,
                         const enum accelerator_v1_mode_t mode,
                         const uint16_t input_width,
                         const uint8_t output_channel_count);

size_t accelerator_v1_get_x_fifo_availability(const struct accelerator_v1_instance_t *instance);
size_t accelerator_v1_get_z_fifo_ocupancy(const struct accelerator_v1_instance_t *instance);

size_t accelerator_v1_set_input(
    const struct accelerator_v1_instance_t *instance, const int8_t data[],
    const size_t data_len);

size_t accelerator_v1_set_filter(
    const struct accelerator_v1_instance_t *instance,
    const uint8_t filter_index, const int8_t data[],
    const size_t data_len);

size_t accelerator_v1_get_output(
    const struct accelerator_v1_instance_t *instance, int32_t data[],
    const size_t data_len);