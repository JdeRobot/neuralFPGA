#include <stdio.h>

size_t fwrite(const void *__restrict buf, size_t size, size_t count, FILE *fp) {
  size_t n;
  size_t i = 0;
  const char *p = buf;
  n = count * size;

  while (i < n) {
    if (putc(p[i], fp) == EOF) break;

    i++;
  }
  
  return i / size;
}