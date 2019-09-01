#include "tensorflow/lite/experimental/micro/debug_log.h"

#include <cstdio>

extern "C" void DebugLog(const char *s)
{
    fprintf(stderr, "%s", s);
}
