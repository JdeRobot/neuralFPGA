`timescale 1ps / 1ps

//mockup for simulation
//blackbox from yosys techlibs/ice40/cells_sim.v
module SB_SPRAM256KA(
    input [13:0] ADDRESS,
    input [15:0] DATAIN,
    input [3:0] MASKWREN,
    input WREN,
    input CHIPSELECT,
    input CLOCK,
    input STANDBY,
    input SLEEP,
    input POWEROFF,
    output [15:0] DATAOUT
);
    SPRAM256KA SPRAM256KA_inst(
        .ADDRESS(ADDRESS),
        .DATAIN(DATAIN),
        .MASKWREN(MASKWREN),
        .WREN(WREN),
        .CHIPSELECT(CHIPSELECT),
        .CLOCK(CLOCK),
        .STANDBY(STANDBY),
        .SLEEP(SLEEP),
        .POWEROFF(POWEROFF),
        .DATAOUT(DATAOUT)
    );
endmodule