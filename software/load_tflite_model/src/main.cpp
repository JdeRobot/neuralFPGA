#include "flatbuffers/flatbuffers.h"
#include "schema_v3_generated.h"
#include <cstdio>

extern char model_data[];
extern unsigned model_data_size;

int main() {
    auto model = tflite::GetModel((const void *)model_data);

    while(1) {
        puts(tflite::ModelIdentifier());
        puts(model->description()->c_str());
    }
}
