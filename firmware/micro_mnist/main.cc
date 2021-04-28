#include <cinttypes>
#include <irq.h>
#include <base/uart.h>
#include "tensorflow/lite/micro/all_ops_resolver.h"
#include "tensorflow/lite/micro/micro_error_reporter.h"
#include "tensorflow/lite/micro/micro_interpreter.h"
#include "tensorflow/lite/kernels/kernel_util.h"


extern char model_data[];
extern size_t model_data_size;

extern uint8_t test_images[];
extern size_t test_images_size;

extern uint8_t test_labels[];
extern size_t test_labels_size;

// Create an area of memory to use for input, output, and intermediate arrays.
  // The size of this will depend on the model you're using, and may need to be
  // determined by experimentation.
const int tensor_arena_size = 20 * 1024;
uint8_t tensor_arena[tensor_arena_size];

int main()
{
#ifdef CONFIG_CPU_HAS_INTERRUPT
  irq_setmask(0);
  irq_setie(1);
#endif
  uart_init();

  MicroPrintf("Micro MNIST: start");

  // Set up profiling
  tflite::MicroProfiler profiler;

  const tflite::Model *model = tflite::GetModel((const void *)model_data);
  if (model->version() != TFLITE_SCHEMA_VERSION)
  {
    TF_LITE_REPORT_ERROR(tflite::GetMicroErrorReporter(),
        "Model provided is schema version %d not equal "
        "to supported version %d.",
        model->version(), TFLITE_SCHEMA_VERSION);
    return 1;
  }

  // This pulls in all the operation implementations we need.
  static tflite::AllOpsResolver resolver;

  // Build an interpreter to run the model with.
  tflite::MicroInterpreter interpreter(model, resolver, tensor_arena, tensor_arena_size, tflite::GetMicroErrorReporter(), &profiler);
  interpreter.AllocateTensors();

  // Get information about the memory area to use for the model's input.
  TfLiteTensor *model_input = interpreter.input(0); // dim: 1x28x28x1
  if ((model_input->dims->size != 4) || 
      (model_input->dims->data[0] != 1) ||
      (model_input->dims->data[1] != 28) ||
      (model_input->dims->data[2] != 28) ||
      (model_input->dims->data[3] != 1) ||
      (model_input->type != kTfLiteInt8))
  {
    TF_LITE_REPORT_ERROR(tflite::GetMicroErrorReporter(), "Bad input tensor parameters in model");
    return 1;
  }

  // Get information about the memory area for the model's output.
  TfLiteTensor *model_output = interpreter.output(0); // dim: 1x10
  if ((model_output->dims->size != 2) || 
      (model_output->dims->data[0] != 1) ||
      (model_output->dims->data[1] != 10) ||
      (model_output->type != kTfLiteInt8))
  {
    TF_LITE_REPORT_ERROR(tflite::GetMicroErrorReporter(), "Bad output tensor parameters in model");
    return 1;
  }

  const size_t image_size = 28*28;
  const size_t n_images = test_images_size / image_size;
  const size_t n_labels = test_labels_size;
  if (n_images != n_labels) {
    TF_LITE_REPORT_ERROR(tflite::GetMicroErrorReporter(), "Number of test images and test labels don't match");
    return 1;
  }

  int matched = 0;
  const size_t test_n_images = 10;
  for (size_t n = 0; n < test_n_images; n++)
  {
    uint8_t* input_image = test_images + (n*image_size);
    uint8_t input_label = test_labels[n];

    profiler.ClearEvents();
    //set input
    for (size_t i = 0; i < 28*28; i++)
    {
      model_input->data.int8[i] = input_image[i] ^ 0x80;
    }

    // Run the model
    TfLiteStatus invoke_status = interpreter.Invoke();
    if (invoke_status != kTfLiteOk)
    {
      TF_LITE_REPORT_ERROR(tflite::GetMicroErrorReporter(), "Invoke failed");
      return 1;
    }

    // get output argmax
    uint8_t argmax = 0;
    for (size_t i = 1; i < model_output->dims->data[1]; i++) {
      if (model_output->data.int8[i] > model_output->data.int8[argmax]) {
        argmax = i;
      }
    }
    MicroPrintf("Micro MNIST: it's a %d, should be %d", argmax, input_label);
    if (argmax == input_label) matched++;
    profiler.Log();
  }

  MicroPrintf("Micro MNIST: matched=%d of %d", matched, test_n_images);

  return 0;
}