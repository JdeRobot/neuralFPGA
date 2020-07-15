#pragma once

#include <stddef.h>
#include <stdint.h>
#include <assert.h>

static inline int offset(const int d0, const int d1, const int d2, const int i0, const int i1, const int i2) {
  assert(i0 >= 0 && i0 < d0);
  assert(i1 >= 0 && i1 < d1);
  assert(i2 >= 0 && i2 < d2);
  return ((i0 * d1 + i1) * d2 + i2);
}

void conv2d(const int8_t* input_data, const size_t input_height,
            const size_t input_width, const int8_t* filter_data,
            const size_t filter_height, const size_t filter_width,
            const size_t filter_depth, int32_t* output_data,
            const size_t output_height, const size_t output_width,
            const size_t output_depth);