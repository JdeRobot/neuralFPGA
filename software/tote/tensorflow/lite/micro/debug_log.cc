#include "tensorflow/lite/micro/debug_log.h"

#include <cstdio>

extern "C" void DebugLog(const char *s)
{
    fprintf(stderr, "%s", s);
}
