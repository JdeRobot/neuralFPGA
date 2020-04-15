TOPTARGETS := all test gen_expected_output clean

SUBDIRS := 	helloworld \
			load_tflite_model \
			conv_test

$(TOPTARGETS): $(SUBDIRS)
$(SUBDIRS):
	$(MAKE) -C $@ $(MAKECMDGOALS)

.PHONY: $(TOPTARGETS) $(SUBDIRS)