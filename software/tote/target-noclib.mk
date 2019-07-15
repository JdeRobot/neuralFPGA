# Settings for RISCV 32-bit Tote toolchain.
ifeq ($(TARGET), tote)
	TARGET_ARCH := rv32im

  TARGET_TOOLCHAIN_PREFIX := riscv64-unknown-elf-
  CXX := ${TARGET_TOOLCHAIN_PREFIX}g++
  CC := ${TARGET_TOOLCHAIN_PREFIX}gcc
  AR := ${TARGET_TOOLCHAIN_PREFIX}ar
  OBJCOPY := $(TARGET_TOOLCHAIN_PREFIX)objcopy
  OBJDUMP := $(TARGET_TOOLCHAIN_PREFIX)objdump

  PLATFORM_FLAGS += \
    -march=$(TARGET_ARCH) \
    -mabi=ilp32 \
    -mcmodel=medany

  INCLUDES := -I $(TARGET_DIR)/hal
  COMMON_CFLAGS := $(PLATFORM_FLAGS) -ffunction-sections -fdata-sections -fstack-usage -Wall
  CXXFLAGS += $(COMMON_CFLAGS) -fno-exceptions -fno-rtti -fno-unwind-tables -fcheck-new
  CCFLAGS += $(COMMON_CFLAGS)

  LDSCRIPT := riscv/link.ld

  LDFLAGS += -ffreestanding -nostdlib -lgcc
  LDFLAGS += -Wl,--gc-sections,-Bstatic,-T,$(TARGET_DIR)/$(LDSCRIPT),-Map,$(OBJDIR)/$(PROJ_NAME).map,--print-memory-usage

  TARGET_SRCS := $(TARGET_DIR)/riscv/start.S $(TARGET_DIR)/riscv/init.c $(TARGET_DIR)/riscv/exit.c $(TARGET_DIR)/riscv/putchar.S $(TARGET_DIR)/riscv/printf.c $(TARGET_DIR)/riscv/supc++.cpp
  TARGET_OBJS := $(addprefix $(OBJDIR)/, $(patsubst %.S,%.o,$(patsubst %.c,%.o,$(patsubst %.cpp,%.o,$(TARGET_SRCS)))))
endif