#include "flatbuffers/flatbuffers.h"
#include "tensorflow/lite/schema/schema_generated.h"
#include <cstdio>

extern char model_data[];
extern unsigned model_data_size;

int main() {
    auto model = tflite::GetModel((const void *)model_data);

    puts(tflite::ModelIdentifier());
    puts(model->description()->c_str());
}
