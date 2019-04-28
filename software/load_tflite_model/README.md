Get schema:
$> wget https://raw.githubusercontent.com/tensorflow/tensorflow/r1.13/tensorflow/lite/schema/schema_v3.fbs

Generate cpp code:
$> flatc --cpp schema_v3.fbs
$> c++ -I/home/dlobato/src/flatbuffers/include -std=c++0x tflite_test.cpp -o tflite_test