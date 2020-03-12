`timescale 1ns / 1ps

module Tote_sim(
        input   io_clk,
        input   io_reset,
        input   io_enable_jtag,
        input  [7:0] io_gpioA_read,
        output [7:0] io_gpioA_write,
        output [7:0] io_gpioA_writeEnable);

wire jtag_tms_o;
wire jtag_tck_o;
//wire jtag_srst_o;
wire jtag_tdo_i;
wire jtag_tdi_o;

SimJTAG #(
    .TICK_DELAY(10)
) jtag (
    .clock(io_clk),
    .reset(io_reset),
    .enable(io_enable_jtag),
    .init_done(1),
    .jtag_TCK(jtag_tck_o),
    .jtag_TMS(jtag_tms_o),
    .jtag_TDI(jtag_tdi_o),
    .jtag_TRSTn(),
    .jtag_TDO_data(jtag_tdo_i),
    .jtag_TDO_driven(io_enable_jtag),
    .exit()
);

Tote tote(
    .io_clk(io_clk),
    .io_reset(io_reset),
    .io_jtag_tms(jtag_tms_o),
    .io_jtag_tdi(jtag_tdi_o),
    .io_jtag_tdo(jtag_tdo_i),
    .io_jtag_tck(jtag_tck_o),
    .io_gpioA_read(io_gpioA_read),
    .io_gpioA_write(io_gpioA_write),
    .io_gpioA_writeEnable(io_gpioA_writeEnable)
);

endmodule