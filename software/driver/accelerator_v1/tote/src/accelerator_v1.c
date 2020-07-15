#include <accelerator_v1.h>
#include <accelerator_v1_regs.h>
#include <assert.h>

struct accelerator_v1_priv_t {
  
};

int accelerator_v1_init(struct accelerator_v1_instance_t *instance,
                         const uint32_t base_address,
                         const enum accelerator_v1_mode_t mode,
                         const uint16_t input_width,
                         const uint8_t output_channel_count) {
  instance->base_address = base_address;
  instance->mode = mode;

  if (mode == ACCELERATOR_V1_CONV2D_3x3) {
    instance->window_buffer_row_width = input_width;
    instance->window_buffer_initial_delay = input_width * 2 + 2;//window buffer delay is twice input width + 2
    instance->output_width = (input_width - 3) + 1;//(INPUT_WIDTH - FILTER_WIDTH) + 1
    instance->output_channel_count = output_channel_count;
  } else { //mode not implemented
    return -1;
  }
  
  volatile uint32_t *config0_reg =
      (volatile uint32_t *)(instance->base_address + ACCELERATOR_V1_CONFIG0_REG_OFFSET);
  volatile uint32_t *config1_reg =
      (volatile uint32_t *)(instance->base_address + ACCELERATOR_V1_CONFIG1_REG_OFFSET);

  *config0_reg = (((instance->window_buffer_initial_delay
                    << ACCELERATOR_V1_WINDOW_BUFFER_INITIAL_DELAY_SHIFT) &
                   ACCELERATOR_V1_WINDOW_BUFFER_INITIAL_DELAY_MASK) |
                  ((instance->window_buffer_row_width
                    << ACCELERATOR_V1_WINDOW_BUFFER_ROW_WIDTH_SHIFT) &
                   ACCELERATOR_V1_WINDOW_BUFFER_ROW_WIDTH_MASK));

  *config1_reg = ((instance->output_channel_count
                   << ACCELERATOR_V1_OUTPUT_CHANNEL_COUNT_SHIFT) &
                  ACCELERATOR_V1_OUTPUT_CHANNEL_COUNT_MASK);

  uint32_t *status_reg =
      (uint32_t *)(instance->base_address + ACCELERATOR_V1_STATUS_REG_OFFSET);
  *status_reg = (1 << ACCELERATOR_V1_INIT_SHIFT) & ACCELERATOR_V1_INIT_MASK;

  //verify hw queues are empty
  assert(accelerator_v1_get_x_fifo_availability(instance) == 512);
  assert(accelerator_v1_get_z_fifo_ocupancy(instance) == 0);

  //verify hw config match initialized values
  assert(((*config0_reg & ACCELERATOR_V1_WINDOW_BUFFER_ROW_WIDTH_MASK) >> ACCELERATOR_V1_WINDOW_BUFFER_ROW_WIDTH_SHIFT) == instance->window_buffer_row_width);
  assert(((*config0_reg & ACCELERATOR_V1_WINDOW_BUFFER_INITIAL_DELAY_MASK) >> ACCELERATOR_V1_WINDOW_BUFFER_INITIAL_DELAY_SHIFT) == instance->window_buffer_initial_delay);
  assert(((*config1_reg & ACCELERATOR_V1_OUTPUT_CHANNEL_COUNT_MASK) >> ACCELERATOR_V1_OUTPUT_CHANNEL_COUNT_SHIFT) == instance->output_channel_count);

  return 0;
}

size_t accelerator_v1_get_x_fifo_availability(const struct accelerator_v1_instance_t *instance) {
    volatile uint32_t *status_reg = (volatile uint32_t *)(instance->base_address + ACCELERATOR_V1_X_FIFO_AVAILABILITY_OFFSET);
    
    return (size_t)((*status_reg & ACCELERATOR_V1_X_FIFO_AVAILABILITY_MASK) >> ACCELERATOR_V1_X_FIFO_AVAILABILITY_SHIFT);
}

size_t accelerator_v1_get_z_fifo_ocupancy(const struct accelerator_v1_instance_t *instance) {
    volatile uint32_t *status_reg = (volatile uint32_t *)(instance->base_address + ACCELERATOR_V1_Z_FIFO_OCUPANCY_OFFSET);
    
    return (size_t)((*status_reg & ACCELERATOR_V1_X_FIFO_OCUPANCY_MASK) >> ACCELERATOR_V1_X_FIFO_OCUPANCY_SHIFT);
}


size_t accelerator_v1_set_input(
    const struct accelerator_v1_instance_t *instance, const int8_t data[],
    const size_t data_len) {
  assert((data_len & 0x3) == 0);//data len must be multiple of 4

  size_t copy_size = data_len >> 2;
  size_t fifo_availability = accelerator_v1_get_x_fifo_availability(instance);
  if (copy_size > fifo_availability) {
    copy_size = fifo_availability;
  }

  const uint32_t *data_32 = (const uint32_t *)data;
  uint32_t *x_fifo = (uint32_t *)(instance->base_address + ACCELERATOR_V1_X_REG_OFFSET);
  

  for (size_t i = 0; i < copy_size; i++) {
     *x_fifo = data_32[i];
  }

  return copy_size << 2;
}

size_t accelerator_v1_set_filter(
    const struct accelerator_v1_instance_t *instance,
    const uint8_t filter_index, const int8_t data[],
    const size_t data_len) {
  assert(filter_index < 256);

  int8_t filter_data_padded[16] = {0};//y mem width is 128 bits/16 bytes
  
  if (instance->mode == ACCELERATOR_V1_CONV2D_3x3) {
    assert(data_len == 9);//3 x 3

    for (int i = 0; i < 9; i++) {
      filter_data_padded[i] = data[i];
    }
  } else {// not implemented
    return 0;
  }

  const uint32_t *filter_data_padded32 = (const uint32_t *)filter_data_padded;
  uint32_t *y_base = (uint32_t *)(instance->base_address + ACCELERATOR_V1_Y_BASE_ADDR);

  for (size_t i = 0; i < 4; i++) {
    y_base[i + (4 * filter_index)] = filter_data_padded32[i];
  }

  return data_len;
}

size_t accelerator_v1_get_output(
    const struct accelerator_v1_instance_t *instance, int32_t data[],
    const size_t data_len) {
  size_t idx = 0, x = 0;
  const uint32_t x_edge = instance->output_width * instance->output_channel_count;
  const uint32_t x_max = (instance->output_width + 2) * instance->output_channel_count;

  size_t nready = 0;
  while ((nready = accelerator_v1_get_z_fifo_ocupancy(instance)) > 0) {
    for (size_t i = 0; i < nready; i++, x++) {
      if (idx >= data_len) return 0;
      if (x == x_max) x = 0;

      int32_t z = *(volatile int32_t *)(instance->base_address + ACCELERATOR_V1_Z_REG_OFFSET);
      if (x < x_edge) data[idx++] = z;
    }
  }

  return idx;
}