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

  CXXFLAGS += $(PLATFORM_FLAGS)

  CCFLAGS += $(PLATFORM_FLAGS)

  LDFLAGS :=
  LDSCRIPT := riscv/link.ld
endif