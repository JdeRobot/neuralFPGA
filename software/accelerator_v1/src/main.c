#include <stdio.h>
#include <inttypes.h>
#include <time.h>
#include <hal/hal.h>

#define X_FIFO_AVAILABILITY (ACCELERATOR_V1->STATUS & 0xFFFF)
#define Z_FIFO_OCUPANCY ((ACCELERATOR_V1->STATUS >> 16) & 0xFFFF)

int main() {
  clock_t t0,t1;
  uint32_t z_count = 0;
  uint32_t z;

  printf("%lu: Hello world!\n", clock());
  printf("%lu: Status: x availability=%d, z ocupancy=%d\n", clock(), X_FIFO_AVAILABILITY, Z_FIFO_OCUPANCY);
  t0 = clock();
  ACCELERATOR_V1->X = 0x01020304UL;
  ACCELERATOR_V1->X = 0x01020304UL;
  ACCELERATOR_V1->X = 0x01020304UL;
  ACCELERATOR_V1->X = 0x01020304UL;
  while(1) {
    if (Z_FIFO_OCUPANCY != 0) break;
  }
  t1 = clock();
  printf("push t=%lu\n", t1-t0);

  t0 = clock();
  while(Z_FIFO_OCUPANCY != 0) {
    z = ACCELERATOR_V1->Z;
    z_count++;
  }
  t1 = clock();
  printf("pop t=%lu\n", t1-t0);
  printf("Z=%d, z_count=%d\n", z, z_count);
  printf("%lu: Status: x availability=%d, z ocupancy=%d\n", clock(), X_FIFO_AVAILABILITY, Z_FIFO_OCUPANCY);
  printf("%lu: Bye World!\n", clock());
  
  return 0;
}