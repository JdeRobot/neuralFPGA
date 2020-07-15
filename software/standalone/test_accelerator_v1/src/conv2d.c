#include "conv2d.h"

//input shape h x w
//filter shape depth x h x w
//output shape h x w x depth
//filter depth = output depth = output channel
void conv2d(const int8_t* input_data, const size_t input_height,
            const size_t input_width, const int8_t* filter_data,
            const size_t filter_height, const size_t filter_width,
            const size_t filter_depth, int32_t* output_data,
            const size_t output_height, const size_t output_width,
            const size_t output_depth) {

  assert(filter_depth == output_depth);
  
  for (int out_y = 0; out_y < output_height; ++out_y) {
    for (int out_x = 0; out_x < output_width; ++out_x) {
      for (int out_channel = 0; out_channel < output_depth; ++out_channel) {
        const int in_x_origin = out_x;
        const int in_y_origin = out_y;
        int32_t acc = 0;
        for (int filter_y = 0; filter_y < filter_height; ++filter_y) {
          for (int filter_x = 0; filter_x < filter_width; ++filter_x) {
            const int in_x = in_x_origin + filter_x;
            const int in_y = in_y_origin + filter_y;
            // If the location is outside the bounds of the input image,
            // use zero as a default value.
            if ((in_x >= 0) && (in_x < input_width) && (in_y >= 0) &&
                (in_y < input_height)) {
              int32_t input_val =
                  input_data[offset(input_height, input_width, 1, in_y, in_x, 0)];
              int32_t filter_val =
                  filter_data[offset(filter_depth, filter_height, filter_width, out_channel, filter_y, filter_x)];
              acc += filter_val * input_val;
            }
          }
        }

        output_data[offset(output_height, output_width, output_depth, out_y, out_x, out_channel)] = acc;
      }
    }
  }
}