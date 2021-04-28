!#/bin/bash

set -euo pipefail

TENSORFLOW_DIR=$1

make -f $(TENSORFLOW_DIR)/tensorflow/lite/micro/tools/make/Makefile TARGET=esp generate_hello_world_esp_project
cp -r $(TENSORFLOW_DIR)/tensorflow/lite/micro/tools/make/gen/esp_xtensa-esp32_default/prj/hello_world/esp-idf/components/tfmicro .
rm -r tfmicro/CMakeLists.txt

git -C $(TENSORFLOW_DIR)/src/tensorflow rev-parse --short HEAD > tfmicro/version