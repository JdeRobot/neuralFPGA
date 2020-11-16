#include <ctime>
#include <cstdio>
#include <cinttypes>
#include "tensorflow/lite/micro/micro_mutable_op_resolver.h"
#include "tensorflow/lite/micro/micro_error_reporter.h"
#include "tensorflow/lite/micro/micro_interpreter.h"
#include "tensorflow/lite/micro/profiling/buffered_profiler.h"
#include "tensorflow/lite/kernels/kernel_util.h"
#include "tensorflow/lite/version.h"

extern char model_data[];
extern unsigned model_data_size;

// Create an area of memory to use for input, output, and intermediate arrays.
  // The size of this will depend on the model you're using, and may need to be
  // determined by experimentation.
const int tensor_arena_size = 20 * 1024;
uint8_t tensor_arena[tensor_arena_size];

int main()
{
  // Set up logging.
  tflite::MicroErrorReporter micro_error_reporter;
  tflite::ErrorReporter *error_reporter = &micro_error_reporter;

  // Set up profiling
  tflite::profiling::BufferedProfiler<16> buffered_profiler(error_reporter);
  tflite::Profiler *profiler = &buffered_profiler;

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
  tflite::MicroMutableOpResolver<8> resolver;
  resolver.AddDepthwiseConv2D();
  resolver.AddFullyConnected();
  resolver.AddMaxPool2D();
  resolver.AddConv2D();
  resolver.AddSoftmax();
  resolver.AddQuantize();
  resolver.AddDequantize();
  resolver.AddReshape();

  // Build an interpreter to run the model with.
  tflite::MicroInterpreter interpreter(model, resolver, tensor_arena, tensor_arena_size, error_reporter, profiler);
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

  // Get information about the memory area for the model's output.
  TfLiteTensor *model_output = interpreter.output(0); // dim: 1x10
  if ((model_output->dims->size != 2) || 
      (model_output->dims->data[0] != 1) ||
      (model_output->dims->data[1] != 10) ||
      (model_output->type != kTfLiteUInt8))
  {
    error_reporter->Report("Bad output tensor parameters in model\n");
    return 1;
  }

  uint8_t input_data[28*28] = {
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0, 84,185,159,151, 60, 36,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,222,254,254,254,254,241,198,198,198,198,198,198,198,198,170, 52,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0, 67,114, 72,114,163,227,254,225,254,254,254,250,229,254,254,140,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 17, 66, 14, 67, 67, 67, 59, 21,236,254,106,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 83,253,209, 18,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 22,233,255, 83,  0,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,129,254,238, 44,  0,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 59,249,254, 62,  0,  0,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,133,254,187,  5,  0,  0,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  9,205,248, 58,  0,  0,  0,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,126,254,182,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 75,251,240, 57,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 19,221,254,166,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  3,203,254,219, 35,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 38,254,254, 77,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 31,224,254,115,  1,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,133,254,254, 52,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0, 61,242,254,254, 52,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,121,254,254,219, 40,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,121,254,207, 18,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,
    0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0,  0
  };

  while(1)
  {
    buffered_profiler.StartProfiling();
    //set input
    model_input->data.uint8 = input_data;

    // Run the model
    TfLiteStatus invoke_status = interpreter.Invoke();
    if (invoke_status != kTfLiteOk)
    {
      error_reporter->Report("Invoke failed\n");
      return 1;
    }

    // get output argmax
    TfLiteTensor *output = interpreter.output(0);
    uint8_t argmax = 0;
    for (size_t i = 1; i < output->dims->data[1]; i++) {
      if (output->data.uint8[i] > output->data.uint8[argmax]) {
        argmax = i;
      }
    }
    error_reporter->Report("Micro MNIST: it's a %d", argmax);

    error_reporter->Report("Micro MNIST: profile report start");
    for(size_t i = 0; i < buffered_profiler.Size(); i++) {
      const tflite::profiling::ProfileEvent *event = buffered_profiler.At(i);
      int32_t op_cycles = static_cast<int32_t>(event->end_timestamp - event->begin_timestamp);
      error_reporter->Report("%d, %d", buffered_profiler.At(i)->event_metadata, op_cycles);
    }
    error_reporter->Report("Micro MNIST: profile report end");
    buffered_profiler.Reset();
  }
}