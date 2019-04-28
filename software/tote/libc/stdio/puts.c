#include <stdio.h>

int puts(const char *s) {
  int result = EOF;
  const char *p = s;

  while (*p) {
    if (putc(*p++, 0) == EOF) goto err;
  }
  if (putc('\n', 0) == EOF) goto err;

  result = '\n';
err:
  return result;
}