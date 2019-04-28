// most of it from here https://www.avrfreaks.net/forum/avr-c-micro-how
// new/delete
// https://wiki.osdev.org/C++#The_Operators_.27new.27_and_.27delete.27
#include <cstdlib>

void *operator new(size_t size) { return malloc(size); }
void *operator new[](size_t size) { return malloc(size); }

void operator delete(void *p) noexcept { free(p); }
void operator delete(void* p, std::size_t size) noexcept { free(p); }
void operator delete[](void *p) { free(p); }

extern "C" void __cxa_pure_virtual() {
  //puts("__cxa_pure_virtual called!");
  while (1)
    ;
}

__extension__ typedef int __guard __attribute__((mode(__DI__)));

extern "C" int __cxa_guard_acquire(__guard *g) { return !*(char *)(g); };
extern "C" void __cxa_guard_release(__guard *g) { *(char *)g = 1; };
extern "C" void __cxa_guard_abort(__guard *){};