INCLUDES += -I${STANDALONE}/include -I${STANDALONE}/hal

COMMON_CFLAGS := -ffunction-sections -fdata-sections -fstack-usage -Wall
CFLAGS += $(CFLAGS_ARGS) $(COMMON_CFLAGS)
CXXFLAGS += $(CXXFLAGS_ARGS) $(COMMON_CFLAGS) -fno-exceptions -fno-rtti -fno-unwind-tables -fcheck-new -fno-use-cxa-atexit
LDFLAGS += -ffreestanding -nostartfiles --specs=nano.specs
LDFLAGS += -Wl,--gc-sections,-Bstatic,-T,$(LDSCRIPT),-Map,$(OBJDIR)/$(PROJ_NAME).map,--print-memory-usage


RTSRCS += ${STANDALONE}/common/start.S \
			${STANDALONE}/common/init.c \
			${STANDALONE}/common/syscall.c \
        	${STANDALONE}/common/supc++.cpp
