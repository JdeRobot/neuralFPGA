#include "conv2d.h"
#include <assert.h>

static inline int offset2d(const size_t width, const int i0, const int i1) {
  return (i0 * width) + i1;
}

void conv2d(const uint8_t* input_data, const size_t input_height,
            const size_t input_width, const uint8_t* filter_data,
            const size_t filter_height, const size_t filter_width,
            const uint8_t bias, uint32_t* output_data,
            const size_t output_height, const size_t output_width) {
  const int stride_width = 1;
  const int stride_height = 1;
  const int dilation_width_factor = 1;
  const int dilation_height_factor = 1;
  const int pad_width = 0;
  const int pad_height = 0;

  for (int out_y = 0; out_y < output_height; ++out_y) {
    for (int out_x = 0; out_x < output_width; ++out_x) {
      const int in_x_origin = (out_x * stride_width) - pad_width;
      const int in_y_origin = (out_y * stride_height) - pad_height;
      uint32_t acc = 0;
      for (int filter_y = 0; filter_y < filter_height; ++filter_y) {
        for (int filter_x = 0; filter_x < filter_width; ++filter_x) {
          const int in_x = in_x_origin + dilation_width_factor * filter_x;
          const int in_y = in_y_origin + dilation_height_factor * filter_y;
          // If the location is outside the bounds of the input image,
          // use zero as a default value.
          if ((in_x >= 0) && (in_x < input_width) && (in_y >= 0) &&
              (in_y < input_height)) {
            uint32_t input_val = input_data[offset2d(input_width, in_y, in_x)];
            uint32_t filter_val =
                filter_data[offset2d(filter_width, filter_y, filter_x)];
            acc += filter_val * input_val;
          }
        }
      }

      acc += bias;

      output_data[offset2d(output_width, out_y, out_x)] = acc;
    }
  }
}