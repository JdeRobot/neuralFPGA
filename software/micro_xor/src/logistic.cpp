
#include "tensorflow/lite/c/builtin_op_data.h"
#include "tensorflow/lite/c/c_api_internal.h"
#include "tensorflow/lite/kernels/internal/common.h"
#include "tensorflow/lite/kernels/internal/quantization_util.h"
#include "tensorflow/lite/kernels/internal/tensor.h"
#include "tensorflow/lite/kernels/kernel_util.h"
#include "tensorflow/lite/kernels/op_macros.h"

namespace tflite {
namespace reference_ops {
inline void Logistic(const RuntimeShape& input_shape, const float* input_data,
                     const RuntimeShape& output_shape, float* output_data) {
  const int flat_size = MatchingFlatSize(input_shape, output_shape);

  for (int i = 0; i < flat_size; i++) {
    float val = input_data[i];
    float result = 1.f / (1.f + std::exp(-val));
    output_data[i] = result;
  }
}

// Convenience version that allows, for example, generated-code calls to be
// uniform between data types.
inline void Logistic(const LogisticParams&, const RuntimeShape& input_shape,
                     const float* input_data, const RuntimeShape& output_shape,
                     float* output_data) {
  // Drop params: not needed.
  Logistic(input_shape, input_data, output_shape, output_data);
}

inline void Logistic(const LogisticParams& params,
                     const RuntimeShape& input_shape, const uint8* input_data,
                     const RuntimeShape& output_shape, uint8* output_data) {
  const int32 input_zero_point = params.input_zero_point;
  const int32 input_range_radius = params.input_range_radius;
  const int32 input_multiplier = params.input_multiplier;
  const int input_left_shift = params.input_left_shift;
  const int flat_size = MatchingFlatSize(input_shape, output_shape);

  for (int i = 0; i < flat_size; i++) {
    const uint8 input_val_u8 = input_data[i];
    const int32 input_val_centered =
        static_cast<int32>(input_val_u8) - input_zero_point;
    uint8 output_val;
    if (input_val_centered <= -input_range_radius) {
      output_val = 0;
    } else if (input_val_centered >= input_range_radius) {
      output_val = 255;
    } else {
      const int32 input_val_rescaled =
          MultiplyByQuantizedMultiplierGreaterThanOne(
              input_val_centered, input_multiplier, input_left_shift);
      using FixedPoint4 = gemmlowp::FixedPoint<int32, 4>;
      using FixedPoint0 = gemmlowp::FixedPoint<int32, 0>;
      const FixedPoint4 input_val_f4 = FixedPoint4::FromRaw(input_val_rescaled);
      const FixedPoint0 output_val_f0 = gemmlowp::logistic(input_val_f4);
      // Convert from Q0.31 to Q23.8.
      using gemmlowp::RoundingDivideByPOT;
      int32 output_val_s32 = RoundingDivideByPOT(output_val_f0.raw(), 23);
      if (output_val_s32 == 256) {
        output_val_s32 = 255;
      }
      // Reinterpret as U0.8.
      TFLITE_DCHECK_GE(output_val_s32, 0);
      TFLITE_DCHECK_LE(output_val_s32, 255);
      output_val = static_cast<uint8>(output_val_s32);
    }
    output_data[i] = output_val;
  }
}
}
}

namespace tflite {
namespace ops {
namespace micro {
namespace activations {

struct OpData {
  int32_t input_multiplier = 0;
  int input_left_shift = 0;
  int32_t input_range_radius = 0;
  int diff_min = 0;
};

TfLiteStatus SigmoidCalculateOpData(const TfLiteTensor* input, OpData* data) {
  TfLiteStatus status = kTfLiteOk;
  if (input->type == kTfLiteUInt8) {
    static constexpr int kInputIntegerBits = 4;

    const double input_real_multiplier =
        input->params.scale *
        static_cast<double>(1 << (31 - kInputIntegerBits));

    QuantizeMultiplierGreaterThanOne(input_real_multiplier,
                                     &data->input_multiplier,
                                     &data->input_left_shift);
    data->input_range_radius =
        CalculateInputRadius(kInputIntegerBits, data->input_left_shift);
  }
  return status;
}

void* SigmoidInit(TfLiteContext* context, const char* buffer, size_t length) {
  return nullptr;
}

void SigmoidFree(TfLiteContext* context, void* buffer) {}

TfLiteStatus SigmoidPrepare(TfLiteContext* context, TfLiteNode* node) {
  TF_LITE_ENSURE_EQ(context, NumInputs(node), 1);
  TF_LITE_ENSURE_EQ(context, NumOutputs(node), 1);
  const TfLiteTensor* input = GetInput(context, node, 0);
  TfLiteTensor* output = GetOutput(context, node, 0);
  TF_LITE_ENSURE_EQ(context, input->type, output->type);

  if (input->type == kTfLiteUInt8) {
    TF_LITE_ENSURE_EQ(context, output->params.zero_point,
                      std::numeric_limits<uint8_t>::min());
    TF_LITE_ENSURE(context, output->params.scale == 1. / 256);
  }
  return kTfLiteOk;
}

TfLiteStatus SigmoidEvalQuantized(TfLiteContext* context, TfLiteNode* node,
                           OpData* data, const TfLiteTensor* input,
                           TfLiteTensor* output) {
  LogisticParams params;
  params.input_zero_point = input->params.zero_point;
  params.input_range_radius = data->input_range_radius;
  params.input_multiplier = data->input_multiplier;
  params.input_left_shift = data->input_left_shift;
  reference_ops::Logistic(params, GetTensorShape(input),
                          GetTensorData<uint8_t>(input), GetTensorShape(output),
                          GetTensorData<uint8_t>(output));
  return kTfLiteOk;
}

TfLiteStatus SigmoidEvalFloat(TfLiteContext* context, TfLiteNode* node,
                       const TfLiteTensor* input, TfLiteTensor* output) {
  reference_ops::Logistic(GetTensorShape(input), GetTensorData<float>(input),
                          GetTensorShape(output), GetTensorData<float>(output));
  return kTfLiteOk;
}

TfLiteStatus SigmoidEval(TfLiteContext* context, TfLiteNode* node) {
  // OpData* data = reinterpret_cast<OpData*>(node->user_data);

  const TfLiteTensor* input = GetInput(context, node, 0);
  TfLiteTensor* output = GetOutput(context, node, 0);

  TfLiteType data_type = input->type;
  OpData local_data_object;
  OpData* data = &local_data_object;
  TF_LITE_ENSURE_STATUS(SigmoidCalculateOpData(input, data));

  switch (data_type) {
    // case kTfLiteFloat32: {
    //   return SigmoidEvalFloat(context, node, input, output);
    // }
    case kTfLiteUInt8: {
      return SigmoidEvalQuantized(context, node, data, input, output);
    }
    default:
      context->ReportError(context, "Type %d not currently supported.",
                           data_type);
      return kTfLiteError;
  }
  return kTfLiteOk;
}

}  // namespace activations

TfLiteRegistration* Register_LOGISTIC() {
  static TfLiteRegistration r = {activations::SigmoidInit, activations::SigmoidFree,
                                 activations::SigmoidPrepare, activations::SigmoidEval};
  return &r;
}

}  // namespace micro
}  // namespace ops
}  // namespace tflite