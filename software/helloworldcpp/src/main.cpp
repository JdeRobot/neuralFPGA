#include <hal.h>
#include <cstdio>


int main() 
{
    while (1) {
        printf("%llu: Hello World!\n", TIMER->COUNTER);
    }
}