TOTESIM := $(realpath ../../../totesim/obj_dir/VTote_sim)
TEST_PROGRAM := $(OBJDIR)/$(PROJ_NAME).elf
SHELL = /bin/bash

ifeq ($(TOTESIM_ARGS),)
	TOTESIM_ARGS := --program $(TEST_PROGRAM)
endif

test: $(TEST_PROGRAM)
	$(TOTESIM) $(TOTESIM_ARGS)

.PHONY: test gen_expected_output
