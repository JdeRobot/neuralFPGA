PROJ_NAME := helloworld
include ../common.mk

LDSCRIPT := linker.ld

SRCS += isr.c main.c

OBJS := $(SRCS:.c=.o)
OBJS := $(ROOT_DIR)/rt/crt0.o \
 		$(ROOT_DIR)/rt/init.o \
 		uart.o \
 		$(OBJS)

all: $(PROJ_NAME).elf $(PROJ_NAME).asm $(PROJ_NAME).bin

$(PROJ_NAME).elf: $(OBJS)
	$(CC) $(CFLAGS) -o $@ $^ $(LDFLAGS) $(LIBS)

uart.o: $(LIBBASE_DIRECTORY)/uart.c
	$(CC) $(CFLAGS) -I$(SOC_DIRECTORY)/software/include/base $(INCLUDES) -c $< -o $@

-include $(OBJS:.o=.d)

.PHONY : clean
clean:
	-rm -f $(OBJS) $(OBJS:.o=.d) $(OBJS:.o=.su) $(PROJ_NAME).elf $(PROJ_NAME).asm $(PROJ_NAME).bin $(MAP_NAME)

.SECONDARY: $(OBJS)