#include <accelerator_v1.h>
#include <accelerator_v1_regs.h>
#include <assert.h>


int accelerator_v1_init(struct accelerator_v1_instance_t *instance,
                        const uint32_t base_address,
                        const enum accelerator_v1_mode_t mode,
                        void *mode_data) {
  assert(instance != 0);
  assert(mode_data != 0);

  int rc = -1;

  instance->base_address = base_address;
  instance->mode = mode;
  instance->mode_data = mode_data;

  uint32_t *config0_reg = (uint32_t *)(instance->base_address + ACCELERATOR_V1_CONFIG0_REG_OFFSET);
  uint32_t *config1_reg = (uint32_t *)(instance->base_address + ACCELERATOR_V1_CONFIG1_REG_OFFSET);
  uint32_t *status_reg = (uint32_t *)(instance->base_address + ACCELERATOR_V1_STATUS_REG_OFFSET);

  if (mode == ACCELERATOR_V1_CONV2D_3x3) {
    struct accelerator_v1_conv2d_3x3_data_t *conv2d_3x3_data = (struct accelerator_v1_conv2d_3x3_data_t *)instance->mode_data;

    *config0_reg = ((((conv2d_3x3_data->input_width * 2 + 2) // window buffer delay is twice input width + 2
                      << ACCELERATOR_V1_WINDOW_BUFFER_INITIAL_DELAY_SHIFT) &
                     ACCELERATOR_V1_WINDOW_BUFFER_INITIAL_DELAY_MASK) |
                    ((conv2d_3x3_data->input_width
                      << ACCELERATOR_V1_WINDOW_BUFFER_ROW_WIDTH_SHIFT) &
                     ACCELERATOR_V1_WINDOW_BUFFER_ROW_WIDTH_MASK));

    *config1_reg = ((conv2d_3x3_data->output_channel_count
                     << ACCELERATOR_V1_OUTPUT_CHANNEL_COUNT_SHIFT) &
                    ACCELERATOR_V1_OUTPUT_CHANNEL_COUNT_MASK);

    uint16_t output_width = (conv2d_3x3_data->input_width - 3) + 1;  //(INPUT_WIDTH - FILTER_WIDTH) + 1
    //init private data
    conv2d_3x3_data->output_idx = 0;
    conv2d_3x3_data->x = 0; //column counter
    conv2d_3x3_data->x_edge = output_width * conv2d_3x3_data->output_channel_count; //column edge including edge pixels
    conv2d_3x3_data->x_max = (output_width + 2) * conv2d_3x3_data->output_channel_count;//last column index with valid pixels

    rc = 0;
  }
  
  *status_reg = (1 << ACCELERATOR_V1_INIT_SHIFT) & ACCELERATOR_V1_INIT_MASK;

  return rc;
}

size_t accelerator_v1_get_x_fifo_availability(struct accelerator_v1_instance_t *instance) {
  assert(instance != 0);
  
  volatile uint32_t *status_reg = (volatile uint32_t *)(instance->base_address + ACCELERATOR_V1_X_FIFO_AVAILABILITY_OFFSET);
    
  return (size_t)((*status_reg & ACCELERATOR_V1_X_FIFO_AVAILABILITY_MASK) >> ACCELERATOR_V1_X_FIFO_AVAILABILITY_SHIFT);
}

size_t accelerator_v1_get_z_fifo_ocupancy(struct accelerator_v1_instance_t *instance) {
  assert(instance != 0);

  volatile uint32_t *status_reg = (volatile uint32_t *)(instance->base_address + ACCELERATOR_V1_Z_FIFO_OCUPANCY_OFFSET);
    
  return (size_t)((*status_reg & ACCELERATOR_V1_X_FIFO_OCUPANCY_MASK) >> ACCELERATOR_V1_X_FIFO_OCUPANCY_SHIFT);
}


size_t accelerator_v1_set_input(
    struct accelerator_v1_instance_t *instance, const int8_t data[],
    const size_t data_len) {
  assert(instance != 0);
  assert(data != 0);
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
    struct accelerator_v1_instance_t *instance,
    const uint8_t filter_index, const int8_t data[], const size_t data_len) {
  assert(instance != 0);
  assert(data != 0);
  assert(filter_index < 256);

  uint32_t *y_base = (uint32_t *)(instance->base_address + ACCELERATOR_V1_Y_BASE_ADDR);

  if (instance->mode == ACCELERATOR_V1_CONV2D_3x3) {
    int8_t filter_data_padded[16] = {0};  // y mem width is 128 bits/16 bytes
    assert(data_len == 9);                // 3 x 3

    for (int i = 0; i < 9; i++) {
      filter_data_padded[i] = data[i];
    }

    const uint32_t *filter_data_padded32 = (const uint32_t *)filter_data_padded;

    for (size_t i = 0; i < 4; i++) {
      y_base[i + (4 * filter_index)] = filter_data_padded32[i];
    }

    return data_len;
  }

  return 0;
}

size_t accelerator_v1_get_output(
    struct accelerator_v1_instance_t *instance, int32_t data[],
    const size_t data_len) {
  assert(instance != 0);
  assert(data != 0);

  if (instance->mode == ACCELERATOR_V1_CONV2D_3x3) {
    struct accelerator_v1_conv2d_3x3_data_t *conv2d_3x3_data = (struct accelerator_v1_conv2d_3x3_data_t *)instance->mode_data;

    size_t nready = 0;
    while ((nready = accelerator_v1_get_z_fifo_ocupancy(instance)) > 0) {
      for (size_t i = 0; i < nready; i++, conv2d_3x3_data->x++) {
        if (conv2d_3x3_data->output_idx >= data_len) return 0;
        if (conv2d_3x3_data->x == conv2d_3x3_data->x_max) conv2d_3x3_data->x = 0;

        int32_t z = *(volatile int32_t *)(instance->base_address +
                                          ACCELERATOR_V1_Z_REG_OFFSET);
        if (conv2d_3x3_data->x < conv2d_3x3_data->x_edge) data[conv2d_3x3_data->output_idx++] = z;
      }
    }

    return conv2d_3x3_data->output_idx;
  }

  return 0;
}

size_t accelerator_v1_accumulate_output(
    struct accelerator_v1_instance_t *instance, int32_t data[],
    const size_t data_len) {
  assert(instance != 0);
  assert(data != 0);

  if (instance->mode == ACCELERATOR_V1_CONV2D_3x3) {
    struct accelerator_v1_conv2d_3x3_data_t *conv2d_3x3_data = (struct accelerator_v1_conv2d_3x3_data_t *)instance->mode_data;

    size_t nready = 0;
    while ((nready = accelerator_v1_get_z_fifo_ocupancy(instance)) > 0) {
      for (size_t i = 0; i < nready; i++, conv2d_3x3_data->x++) {
        if (conv2d_3x3_data->output_idx >= data_len) return 0;
        if (conv2d_3x3_data->x == conv2d_3x3_data->x_max) conv2d_3x3_data->x = 0;

        int32_t z = *(volatile int32_t *)(instance->base_address +
                                          ACCELERATOR_V1_Z_REG_OFFSET);
        if (conv2d_3x3_data->x < conv2d_3x3_data->x_edge) data[conv2d_3x3_data->output_idx++] += z;
      }
    }

    return conv2d_3x3_data->output_idx;
  }

  return 0;
}