# 1 "tensorflow/lite/experimental/micro/micro_error_reporter.cc"
# 1 "<built-in>"
# 1 "<command-line>"
# 1 "tensorflow/lite/experimental/micro/micro_error_reporter.cc"
# 16 "tensorflow/lite/experimental/micro/micro_error_reporter.cc"
# 1 "./tensorflow/lite/experimental/micro/micro_error_reporter.h" 1
# 18 "./tensorflow/lite/experimental/micro/micro_error_reporter.h"
# 1 "./tensorflow/lite/core/api/error_reporter.h" 1
# 18 "./tensorflow/lite/core/api/error_reporter.h"
# 1 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/c++/8.2.0/cstdarg" 1 3
# 39 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/c++/8.2.0/cstdarg" 3
       
# 40 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/c++/8.2.0/cstdarg" 3


# 1 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/c++/8.2.0/riscv64-unknown-elf/rv32im/ilp32/bits/c++config.h" 1 3
# 236 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/c++/8.2.0/riscv64-unknown-elf/rv32im/ilp32/bits/c++config.h" 3

# 236 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/c++/8.2.0/riscv64-unknown-elf/rv32im/ilp32/bits/c++config.h" 3
namespace std
{
  typedef unsigned int size_t;
  typedef int ptrdiff_t;


  typedef decltype(nullptr) nullptr_t;

}
# 258 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/c++/8.2.0/riscv64-unknown-elf/rv32im/ilp32/bits/c++config.h" 3
namespace std
{
  inline namespace __cxx11 __attribute__((__abi_tag__ ("cxx11"))) { }
}
namespace __gnu_cxx
{
  inline namespace __cxx11 __attribute__((__abi_tag__ ("cxx11"))) { }
}
# 508 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/c++/8.2.0/riscv64-unknown-elf/rv32im/ilp32/bits/c++config.h" 3
# 1 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/c++/8.2.0/riscv64-unknown-elf/rv32im/ilp32/bits/os_defines.h" 1 3
# 509 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/c++/8.2.0/riscv64-unknown-elf/rv32im/ilp32/bits/c++config.h" 2 3


# 1 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/c++/8.2.0/riscv64-unknown-elf/rv32im/ilp32/bits/cpu_defines.h" 1 3
# 512 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/c++/8.2.0/riscv64-unknown-elf/rv32im/ilp32/bits/c++config.h" 2 3
# 43 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/c++/8.2.0/cstdarg" 2 3
# 1 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/lib/gcc/riscv64-unknown-elf/8.2.0/include/stdarg.h" 1 3 4
# 40 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/lib/gcc/riscv64-unknown-elf/8.2.0/include/stdarg.h" 3 4
typedef __builtin_va_list __gnuc_va_list;
# 99 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/lib/gcc/riscv64-unknown-elf/8.2.0/include/stdarg.h" 3 4
typedef __gnuc_va_list va_list;
# 44 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/c++/8.2.0/cstdarg" 2 3
# 53 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/c++/8.2.0/cstdarg" 3
namespace std
{
  using ::va_list;
}
# 19 "./tensorflow/lite/core/api/error_reporter.h" 2


# 20 "./tensorflow/lite/core/api/error_reporter.h"
namespace tflite {
# 35 "./tensorflow/lite/core/api/error_reporter.h"
class ErrorReporter {
 public:
  virtual ~ErrorReporter() {}
  virtual int Report(const char* format, va_list args) = 0;
  int Report(const char* format, ...);
  int ReportError(void*, const char* format, ...);
};

}
# 19 "./tensorflow/lite/experimental/micro/micro_error_reporter.h" 2
# 1 "./tensorflow/lite/experimental/micro/compatibility.h" 1
# 20 "./tensorflow/lite/experimental/micro/micro_error_reporter.h" 2
# 1 "./tensorflow/lite/experimental/micro/debug_log.h" 1
# 21 "./tensorflow/lite/experimental/micro/debug_log.h"
extern "C" void DebugLog(const char* s);
# 21 "./tensorflow/lite/experimental/micro/micro_error_reporter.h" 2
# 1 "./tensorflow/lite/experimental/micro/debug_log_numbers.h" 1
# 18 "./tensorflow/lite/experimental/micro/debug_log_numbers.h"
# 1 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/c++/8.2.0/cstdint" 1 3
# 32 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/c++/8.2.0/cstdint" 3
       
# 33 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/c++/8.2.0/cstdint" 3
# 41 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/c++/8.2.0/cstdint" 3
# 1 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/lib/gcc/riscv64-unknown-elf/8.2.0/include/stdint.h" 1 3 4
# 9 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/lib/gcc/riscv64-unknown-elf/8.2.0/include/stdint.h" 3 4
# 1 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/stdint.h" 1 3 4
# 12 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/stdint.h" 3 4
# 1 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/machine/_default_types.h" 1 3 4







# 1 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/sys/features.h" 1 3 4
# 25 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/sys/features.h" 3 4

# 25 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/sys/features.h" 3 4
extern "C" {


# 1 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/_newlib_version.h" 1 3 4
# 29 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/sys/features.h" 2 3 4
# 531 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/sys/features.h" 3 4
}
# 9 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/machine/_default_types.h" 2 3 4
# 37 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/machine/_default_types.h" 3 4
extern "C" {



typedef signed char __int8_t;

typedef unsigned char __uint8_t;
# 55 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/machine/_default_types.h" 3 4
typedef short int __int16_t;

typedef short unsigned int __uint16_t;
# 77 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/machine/_default_types.h" 3 4
typedef long int __int32_t;

typedef long unsigned int __uint32_t;
# 103 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/machine/_default_types.h" 3 4
typedef long long int __int64_t;

typedef long long unsigned int __uint64_t;
# 134 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/machine/_default_types.h" 3 4
typedef signed char __int_least8_t;

typedef unsigned char __uint_least8_t;
# 160 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/machine/_default_types.h" 3 4
typedef short int __int_least16_t;

typedef short unsigned int __uint_least16_t;
# 182 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/machine/_default_types.h" 3 4
typedef long int __int_least32_t;

typedef long unsigned int __uint_least32_t;
# 200 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/machine/_default_types.h" 3 4
typedef long long int __int_least64_t;

typedef long long unsigned int __uint_least64_t;
# 214 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/machine/_default_types.h" 3 4
typedef long long int __intmax_t;







typedef long long unsigned int __uintmax_t;







typedef int __intptr_t;

typedef unsigned int __uintptr_t;
# 247 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/machine/_default_types.h" 3 4
}
# 13 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/stdint.h" 2 3 4
# 1 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/sys/_intsup.h" 1 3 4
# 35 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/sys/_intsup.h" 3 4
       
       
       
       
       
       
       
# 187 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/sys/_intsup.h" 3 4
       
       
       
       
       
       
       
# 14 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/stdint.h" 2 3 4
# 1 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/sys/_stdint.h" 1 3 4
# 15 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/sys/_stdint.h" 3 4
extern "C" {




typedef __int8_t int8_t ;



typedef __uint8_t uint8_t ;







typedef __int16_t int16_t ;



typedef __uint16_t uint16_t ;







typedef __int32_t int32_t ;



typedef __uint32_t uint32_t ;







typedef __int64_t int64_t ;



typedef __uint64_t uint64_t ;






typedef __intmax_t intmax_t;




typedef __uintmax_t uintmax_t;




typedef __intptr_t intptr_t;




typedef __uintptr_t uintptr_t;




}
# 15 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/stdint.h" 2 3 4


extern "C" {



typedef __int_least8_t int_least8_t;
typedef __uint_least8_t uint_least8_t;




typedef __int_least16_t int_least16_t;
typedef __uint_least16_t uint_least16_t;




typedef __int_least32_t int_least32_t;
typedef __uint_least32_t uint_least32_t;




typedef __int_least64_t int_least64_t;
typedef __uint_least64_t uint_least64_t;
# 51 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/stdint.h" 3 4
  typedef int int_fast8_t;
  typedef unsigned int uint_fast8_t;
# 61 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/stdint.h" 3 4
  typedef int int_fast16_t;
  typedef unsigned int uint_fast16_t;
# 71 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/stdint.h" 3 4
  typedef int int_fast32_t;
  typedef unsigned int uint_fast32_t;
# 81 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/stdint.h" 3 4
  typedef long long int int_fast64_t;
  typedef long long unsigned int uint_fast64_t;
# 463 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/stdint.h" 3 4
}
# 10 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/lib/gcc/riscv64-unknown-elf/8.2.0/include/stdint.h" 2 3 4
# 42 "/home/dlobato/opt/riscv64-unknown-elf-gcc-8.2.0-2019.02.0-x86_64-linux-ubuntu14/riscv64-unknown-elf/include/c++/8.2.0/cstdint" 2 3




namespace std
{
  using ::int8_t;
  using ::int16_t;
  using ::int32_t;
  using ::int64_t;

  using ::int_fast8_t;
  using ::int_fast16_t;
  using ::int_fast32_t;
  using ::int_fast64_t;

  using ::int_least8_t;
  using ::int_least16_t;
  using ::int_least32_t;
  using ::int_least64_t;

  using ::intmax_t;
  using ::intptr_t;

  using ::uint8_t;
  using ::uint16_t;
  using ::uint32_t;
  using ::uint64_t;

  using ::uint_fast8_t;
  using ::uint_fast16_t;
  using ::uint_fast32_t;
  using ::uint_fast64_t;

  using ::uint_least8_t;
  using ::uint_least16_t;
  using ::uint_least32_t;
  using ::uint_least64_t;

  using ::uintmax_t;
  using ::uintptr_t;
}
# 19 "./tensorflow/lite/experimental/micro/debug_log_numbers.h" 2



# 21 "./tensorflow/lite/experimental/micro/debug_log_numbers.h"
extern "C" {
void DebugLogInt32(int32_t i);
void DebugLogUInt32(uint32_t i);
void DebugLogHex(uint32_t i);
void DebugLogFloat(float i);
}
# 22 "./tensorflow/lite/experimental/micro/micro_error_reporter.h" 2

namespace tflite {

class MicroErrorReporter : public ErrorReporter {
 public:
  ~MicroErrorReporter() {}
  int Report(const char* format, va_list args) override;

 private:
  void operator delete(void* p) {}
};

}
# 17 "tensorflow/lite/experimental/micro/micro_error_reporter.cc" 2

namespace tflite {
namespace {
void DebugLogPrintf(const char* format, va_list args) {
  const int output_cache_size = 64;
  char output_cache[output_cache_size + 1];
  int output_cache_index = 0;
  const char* current = format;
  while (*current != 0) {
    if (*current == '%') {
      const char next = *(current + 1);
      if ((next == 'd') || (next == 's')) {
        current += 1;
        if (output_cache_index > 0) {
          output_cache[output_cache_index] = 0;
          DebugLog(output_cache);
          output_cache_index = 0;
        }
        if (next == 'd') {
          DebugLogInt32(
# 36 "tensorflow/lite/experimental/micro/micro_error_reporter.cc" 3 4
                       __builtin_va_arg(
# 36 "tensorflow/lite/experimental/micro/micro_error_reporter.cc"
                       args
# 36 "tensorflow/lite/experimental/micro/micro_error_reporter.cc" 3 4
                       ,
# 36 "tensorflow/lite/experimental/micro/micro_error_reporter.cc"
                       int
# 36 "tensorflow/lite/experimental/micro/micro_error_reporter.cc" 3 4
                       )
# 36 "tensorflow/lite/experimental/micro/micro_error_reporter.cc"
                                        );
        } else if (next == 's') {
          DebugLog(
# 38 "tensorflow/lite/experimental/micro/micro_error_reporter.cc" 3 4
                  __builtin_va_arg(
# 38 "tensorflow/lite/experimental/micro/micro_error_reporter.cc"
                  args
# 38 "tensorflow/lite/experimental/micro/micro_error_reporter.cc" 3 4
                  ,
# 38 "tensorflow/lite/experimental/micro/micro_error_reporter.cc"
                  char*
# 38 "tensorflow/lite/experimental/micro/micro_error_reporter.cc" 3 4
                  )
# 38 "tensorflow/lite/experimental/micro/micro_error_reporter.cc"
                                     );
        }
      }
    } else {
      output_cache[output_cache_index] = *current;
      output_cache_index += 1;
    }
    if (output_cache_index >= output_cache_size) {
      output_cache[output_cache_index] = 0;
      DebugLog(output_cache);
      output_cache_index = 0;
    }
    current += 1;
  }
  if (output_cache_index > 0) {
    output_cache[output_cache_index] = 0;
    DebugLog(output_cache);
    output_cache_index = 0;
  }
  DebugLog("\n");
}
}

int MicroErrorReporter::Report(const char* format, va_list args) {
  DebugLogPrintf(format, args);
  return 0;
}

}
