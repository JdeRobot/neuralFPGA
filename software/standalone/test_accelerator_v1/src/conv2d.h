#pragma once

#include <stdint.h>
#include <stddef.h>

void conv2d(const uint8_t* input_data, const size_t input_height,
            const size_t input_width, const uint8_t* filter_data,
            const size_t filter_height, const size_t filter_width,
            const uint8_t bias, uint32_t* output_data,
            const size_t output_height, const size_t output_width);