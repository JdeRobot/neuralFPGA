    .section .rodata
    .global model_data
    .type   model_data, @object
    .align  4
model_data:
    .incbin MODEL_DATA_FILE
model_data_end:
    .global model_data_size
    .type   model_data_size, @object
    .align  4
model_data_size:
    .int    model_data_end - model_data