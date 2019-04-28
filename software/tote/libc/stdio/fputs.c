#include <stdio.h>

int fputs(const char *__restrict s, FILE *__restrict stream) {
  const char *p = s;

  while (*p) {
    if (putc(*p++, stream) == EOF) goto err;
  }

  return 0;
err:
  return EOF;
}