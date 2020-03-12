`timescale 1ns / 1ps

module Tote_sim(
        input   io_clk,
        input   io_reset,
        input  [7:0] io_gpioA_read,
        output [7:0] io_gpioA_write,
        output [7:0] io_gpioA_writeEnable);

Tote tote(
    .io_clk(io_clk),
    .io_reset(io_reset),
    .io_gpioA_read(io_gpioA_read),
    .io_gpioA_write(io_gpioA_write),
    .io_gpioA_writeEnable(io_gpioA_writeEnable)
);

endmodule