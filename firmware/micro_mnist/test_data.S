    .section .rodata
    .global test_images
    .type   test_images, @object
    .align  4
test_images:
    .incbin TEST_IMAGES_DATA_FILE
test_images_end:
    .global test_images_size
    .type   test_images_size, @object
    .align  4
test_images_size:
    .int    test_images_end - test_images

    .global test_labels
    .type   test_labels, @object
    .align  4
test_labels:
    .incbin TEST_LABELS_DATA_FILE
test_labels_end:
    .global test_labels_size
    .type   test_labels_size, @object
    .align  4
test_labels_size:
    .int    test_labels_end - test_labels