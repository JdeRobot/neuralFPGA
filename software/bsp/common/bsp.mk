ifeq (${BSP},)
	BSP := totesim
endif

INCLUDES += -I${BSP_PATH}/${BSP}/include
LDSCRIPT ?= ${BSP_PATH}/${BSP}/linker/default.ld

include ${BSP_PATH}/${BSP}/include/soc.mk