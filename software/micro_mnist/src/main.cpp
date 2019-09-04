#include <hal.h>
#include <cstdio>
#include <cinttypes>
#include "tensorflow/lite/experimental/micro/micro_mutable_op_resolver.h"
#include "tensorflow/lite/experimental/micro/micro_error_reporter.h"
#include "tensorflow/lite/experimental/micro/micro_interpreter.h"
#include "tensorflow/lite/kernels/kernel_util.h"
#include "tensorflow/lite/version.h"

#define assume(cond) do { if (!(cond)) __builtin_unreachable(); } while (0)

extern char model_data[];
extern unsigned model_data_size;

// Create an area of memory to use for input, output, and intermediate arrays.
  // The size of this will depend on the model you're using, and may need to be
  // determined by experimentation.
const int tensor_arena_size = 20 * 1024;
uint8_t tensor_arena[tensor_arena_size];

namespace tflite
{

namespace ops
{
namespace micro
{

TfLiteRegistration* Register_DEPTHWISE_CONV_2D();
TfLiteRegistration* Register_FULLY_CONNECTED();
TfLiteRegistration* Register_MAX_POOL_2D();
TfLiteRegistration* Register_CONV_2D();
TfLiteRegistration* Register_SOFTMAX();


} // namespace micro
} // namespace ops
} // namespace tflite

int main()
{
  // Set up logging.
  tflite::MicroErrorReporter micro_error_reporter;
  tflite::ErrorReporter *error_reporter = &micro_error_reporter;

  const tflite::Model *model = tflite::GetModel((const void *)model_data);
  if (model->version() != TFLITE_SCHEMA_VERSION)
  {
    error_reporter->Report(
        "Model provided is schema version %d not equal "
        "to supported version %d.\n",
        model->version(), TFLITE_SCHEMA_VERSION);
    return 1;
  }

  // This pulls in all the operation implementations we need.
  tflite::MicroMutableOpResolver resolver;
  resolver.AddBuiltin(tflite::BuiltinOperator_DEPTHWISE_CONV_2D, tflite::ops::micro::Register_DEPTHWISE_CONV_2D());
  resolver.AddBuiltin(tflite::BuiltinOperator_FULLY_CONNECTED, tflite::ops::micro::Register_FULLY_CONNECTED(),
                      /* min_version */ 1,
                      /* max_version */ 4);
  resolver.AddBuiltin(tflite::BuiltinOperator_MAX_POOL_2D, tflite::ops::micro::Register_MAX_POOL_2D());
  resolver.AddBuiltin(tflite::BuiltinOperator_CONV_2D, tflite::ops::micro::Register_CONV_2D());
  resolver.AddBuiltin(tflite::BuiltinOperator_SOFTMAX, tflite::ops::micro::Register_SOFTMAX());
  // resolver.AddBuiltin(tflite::BuiltinOperator_LOGISTIC,
  //                     tflite::ops::micro::Micro_Register_LOGISTIC());
  // resolver.AddBuiltin(tflite::BuiltinOperator_FULLY_CONNECTED,
  //                     tflite::ops::micro::Micro_Register_FULLY_CONNECTED(),
  //                     /* min_version */ 1,
  //                     /* max_version */ 2);

  // Build an interpreter to run the model with.
  tflite::MicroInterpreter interpreter(model, resolver, tensor_arena, tensor_arena_size, error_reporter);
  interpreter.AllocateTensors();

  // Get information about the memory area to use for the model's input.
  TfLiteTensor *model_input = interpreter.input(0); // dim: 1x28x28x1
  if ((model_input->dims->size != 4) || 
      (model_input->dims->data[0] != 1) ||
      (model_input->dims->data[1] != 28) ||
      (model_input->dims->data[2] != 28) ||
      (model_input->dims->data[3] != 1) ||
      (model_input->type != kTfLiteUInt8))
  {
    error_reporter->Report("Bad input tensor parameters in model\n");
    return 1;
  }

  //uint8_t input_data[28*28];

  uint64_t avg_iteration_cycles = 0;
  uint8_t  avg_iteration_cycles_nsamples = 0;

  while(1)
  {
    //set input
    //TODO

    // Run the model
    uint64_t t0 = TIMER->COUNTER;
    TfLiteStatus invoke_status = interpreter.Invoke();
    avg_iteration_cycles += (TIMER->COUNTER - t0);
    avg_iteration_cycles_nsamples++;
    if (invoke_status != kTfLiteOk)
    {
      error_reporter->Report("Invoke failed\n");
      return 1;
    }

    // get output
    error_reporter->Report("Iteration done\n");
    //if (avg_iteration_cycles_nsamples == 1) {
    error_reporter->Report("Micro MNIST: Avg iteration cycles: %d\n", static_cast<int>(avg_iteration_cycles));// avg_iteration_cycles/256
    avg_iteration_cycles = 0;
    //}
  }
}