#include <hal.h>
#include <cstdio>
#include <cinttypes>
#include "tensorflow/lite/experimental/micro/micro_mutable_op_resolver.h"
//#include "tensorflow/lite/experimental/micro/micro_error_reporter.h"
#include "tensorflow/lite/experimental/micro/micro_interpreter.h"
#include "tensorflow/lite/version.h"

extern char model_data[];
extern unsigned model_data_size;

namespace tflite {

class MicroErrorReporter : public ErrorReporter {
 public:
  ~MicroErrorReporter() {}
  int Report(const char* format, va_list args) override {
    return vfprintf(stderr, format, args);
  }

 private:
  TF_LITE_REMOVE_VIRTUAL_DELETE
};

namespace ops {
namespace micro {

TfLiteRegistration* Register_LOGISTIC();
TfLiteRegistration* Micro_Register_LOGISTIC() { return Register_LOGISTIC(); }

TfLiteRegistration* Register_FULLY_CONNECTED();
TfLiteRegistration* Micro_Register_FULLY_CONNECTED() {
  return Register_FULLY_CONNECTED();
}

}  // namespace micro
}  // namespace ops
}  // namespace tflite

int main() {
  // Set up logging.
  tflite::MicroErrorReporter micro_error_reporter;
  tflite::ErrorReporter* error_reporter = &micro_error_reporter;

  const tflite::Model* model = tflite::GetModel((const void*)model_data);
  if (model->version() != TFLITE_SCHEMA_VERSION) {
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

  // Create an area of memory to use for input, output, and intermediate arrays.
  // The size of this will depend on the model you're using, and may need to be
  // determined by experimentation.
  const int tensor_arena_size = 10 * 1024;
  uint8_t tensor_arena[tensor_arena_size];
  tflite::SimpleTensorAllocator tensor_allocator(tensor_arena,
                                                 tensor_arena_size);

  // Build an interpreter to run the model with.
  tflite::MicroInterpreter interpreter(model, resolver, &tensor_allocator,
                                       error_reporter);

  // Get information about the memory area to use for the model's input.
  TfLiteTensor* model_input = interpreter.input(0);  // dim: 1x2x0
  if ((model_input->dims->size != 2) || (model_input->dims->data[0] != 1) ||
      (model_input->dims->data[1] != 2) ||
      (model_input->type != kTfLiteUInt8)) {
    error_reporter->Report("Bad input tensor parameters in model\n");
    return 1;
  }

  bool test_data[4][3] = {// input1, input2, expected label
                          {false, false, false},
                          {false, true, true},
                          {true, false, true},
                          {true, true, false}};

  //printf("%llu: Micro XOR start\n", TIMER->COUNTER);
  error_reporter->Report("%" PRIu64 ": Start\n", TIMER->COUNTER);
  for (int i = 0; i < 4; i++) {
    // set input
    model_input->data.uint8[0] = test_data[i][0];
    model_input->data.uint8[1] = test_data[i][1];

    // Run the model
    TfLiteStatus invoke_status = interpreter.Invoke();
    if (invoke_status != kTfLiteOk) {
      error_reporter->Report("Invoke failed\n");
      return 1;
    }

    // get output
    TfLiteTensor* output = interpreter.output(0);
    bool output_label = (output->data.uint8[0] > (255 / 2));
    if (test_data[i][2] != output_label) {
      error_reporter->Report("Expected ouput: %d, got: %d\n",
                             (int)test_data[i][2], (int)output_label);
    }

    error_reporter->Report("%" PRIu64 ": Micro XOR iteration => %d xor %d = %d\n", TIMER->COUNTER, test_data[i][0], test_data[i][1], test_data[i][2]);
  }
  error_reporter->Report("%" PRIu64 ":Done\n", TIMER->COUNTER);
  //printf("%llu: Micro XOR done\n", TIMER->COUNTER);
}