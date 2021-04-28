ifeq (${PLATFORM},)
	PLATFORM := tote
endif

include ${DRIVER_PATH}/accelerator_v1/accelerator_v1.mk