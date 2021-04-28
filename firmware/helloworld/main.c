#include <base/uart.h>
#include <irq.h>

int putchar(int c)
{
	uart_write(c);
	if (c == '\n')
		putchar('\r');
	return c;
}

void putsnonl(const char *s)
{
	while(*s) {
		putchar(*s);
		s++;
	}
}

int puts(const char *s)
{
	putsnonl(s);
	putchar('\n');
	return 1;
}

int main() {
#ifdef CONFIG_CPU_HAS_INTERRUPT
  irq_setmask(0);
  irq_setie(1);
#endif
  uart_init();

  puts("Hello World!");

  return 0;
}
