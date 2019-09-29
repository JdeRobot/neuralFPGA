#include <cstdio>
#include <cinttypes>
#include <ctime>
#include "tensorflow/lite/experimental/micro/micro_mutable_op_resolver.h"
#include "tensorflow/lite/experimental/micro/micro_error_reporter.h"
#include "tensorflow/lite/experimental/micro/micro_interpreter.h"
#include "tensorflow/lite/experimental/micro/profiling/noop_profiler.h"
#include "tensorflow/lite/version.h"

extern char model_data[];
extern unsigned model_data_size;

namespace tflite
{

// class MicroErrorReporter : public ErrorReporter
// {
// public:
//   ~MicroErrorReporter() {}
//   int Report(const char *format, va_list args) override
//   {
//     return vfprintf(stderr, format, args);
//   }

// private:
//   TF_LITE_REMOVE_VIRTUAL_DELETE
// };

namespace ops
{
namespace micro
{

TfLiteRegistration *Register_LOGISTIC();
TfLiteRegistration *Micro_Register_LOGISTIC() { return Register_LOGISTIC(); }

TfLiteRegistration *Register_FULLY_CONNECTED();
TfLiteRegistration *Micro_Register_FULLY_CONNECTED()
{
  return Register_FULLY_CONNECTED();
}

} // namespace micro
} // namespace ops
} // namespace tflite

// Create an area of memory to use for input, output, and intermediate arrays.
// The size of this will depend on the model you're using, and may need to be
// determined by experimentation.
const int tensor_arena_size = 10 * 1024;
uint8_t tensor_arena[tensor_arena_size];

int main()
{
  // Set up logging.
  tflite::MicroErrorReporter micro_error_reporter;
  tflite::ErrorReporter *error_reporter = &micro_error_reporter;

  // Set up profiling
  tflite::profiling::NoopProfiler noop_profiler;
  tflite::Profiler *profiler = &noop_profiler;

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
  resolver.AddBuiltin(tflite::BuiltinOperator_LOGISTIC,
                      tflite::ops::micro::Micro_Register_LOGISTIC());
  resolver.AddBuiltin(tflite::BuiltinOperator_FULLY_CONNECTED,
                      tflite::ops::micro::Micro_Register_FULLY_CONNECTED(),
                      /* min_version */ 1,
                      /* max_version */ 2);

  // Build an interpreter to run the model with.
  tflite::MicroInterpreter interpreter(model, resolver, tensor_arena, tensor_arena_size, error_reporter, profiler);
  interpreter.AllocateTensors();

  // Get information about the memory area to use for the model's input.
  TfLiteTensor *model_input = interpreter.input(0); // dim: 1x2x0
  if ((model_input->dims->size != 2) || (model_input->dims->data[0] != 1) ||
      (model_input->dims->data[1] != 2) ||
      (model_input->type != kTfLiteUInt8))
  {
    error_reporter->Report("Bad input tensor parameters in model\n");
    return 1;
  }

  uint8_t test_data[4][3] = {// input1, input2, expected label
                             {0, 0, 0},
                             {0, 1, 1},
                             {1, 0, 1},
                             {1, 1, 0}};

  uint64_t avg_iteration_cycles = 0;

  error_reporter->Report("Micro XOR Start\n");
  for (int j = 0; j < 256; j++)
  {
    int i = j % 4;

    // set input
    model_input->data.uint8[0] = test_data[i][0];
    model_input->data.uint8[1] = test_data[i][1];

    // Run the model
    clock_t t0 = clock();
    TfLiteStatus invoke_status = interpreter.Invoke();
    avg_iteration_cycles += (clock() - t0);
    if (invoke_status != kTfLiteOk)
    {
      error_reporter->Report("Invoke failed\n");
      return 1;
    }

    // get output
    TfLiteTensor *output = interpreter.output(0);
    bool output_label = (output->data.uint8[0] > (255 / 2));
    if (test_data[i][2] != output_label)
    {
      error_reporter->Report("Expected ouput: %d, got: %d\n",
                             (int)test_data[i][2], (int)output_label);
    }
    //error_reporter->Report("Micro XOR iteration => %d xor %d = %d\n", test_data[i][0], test_data[i][1], test_data[i][2]);
  }
  
  error_reporter->Report("Micro XOR Done. Avg iteration cycles: %d\n", static_cast<int>(avg_iteration_cycles >> 8));// avg_iteration_cycles/256
}