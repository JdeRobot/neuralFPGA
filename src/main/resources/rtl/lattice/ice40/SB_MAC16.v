`timescale 1ps / 1ps

//mockup for simulation
//blackbox from yosys techlibs/ice40/cells_sim.v
module SB_MAC16 (
        input CLK,
        input CE,
        input [15:0] C,
        input [15:0] A,
        input [15:0] B,
        input [15:0] D,
        input AHOLD,
        input BHOLD,
        input CHOLD,
        input DHOLD,
        input IRSTTOP,
        input IRSTBOT,
        input ORSTTOP,
        input ORSTBOT,
        input OLOADTOP,
        input OLOADBOT,
        input ADDSUBTOP,
        input ADDSUBBOT,
        input OHOLDTOP,
        input OHOLDBOT,
        input CI,
        input ACCUMCI,
        input SIGNEXTIN,
        output [31:0] O,
        output CO,
        output ACCUMCO,
        output SIGNEXTOUT
);
parameter NEG_TRIGGER = 1'b0;
parameter C_REG = 1'b0;
parameter A_REG = 1'b0;
parameter B_REG = 1'b0;
parameter D_REG = 1'b0;
parameter TOP_8x8_MULT_REG = 1'b0;
parameter BOT_8x8_MULT_REG = 1'b0;
parameter PIPELINE_16x16_MULT_REG1 = 1'b0;
parameter PIPELINE_16x16_MULT_REG2 = 1'b0;
parameter TOPOUTPUT_SELECT =  2'b00;
parameter TOPADDSUB_LOWERINPUT = 2'b00;
parameter TOPADDSUB_UPPERINPUT = 1'b0;
parameter TOPADDSUB_CARRYSELECT = 2'b00;
parameter BOTOUTPUT_SELECT =  2'b00;
parameter BOTADDSUB_LOWERINPUT = 2'b00;
parameter BOTADDSUB_UPPERINPUT = 1'b0;
parameter BOTADDSUB_CARRYSELECT = 2'b00;
parameter MODE_8x8 = 1'b0;
parameter A_SIGNED = 1'b0;
parameter B_SIGNED = 1'b0;

    MAC16_SIM #(
        .NEG_TRIGGER(NEG_TRIGGER),
        .C_REG(C_REG),
        .A_REG(A_REG),
        .B_REG(B_REG),
        .D_REG(D_REG),
        .TOP_8x8_MULT_REG(TOP_8x8_MULT_REG),
        .BOT_8x8_MULT_REG(BOT_8x8_MULT_REG),
        .PIPELINE_16x16_MULT_REG1(PIPELINE_16x16_MULT_REG1),
        .PIPELINE_16x16_MULT_REG2(PIPELINE_16x16_MULT_REG2),
        .TOPOUTPUT_SELECT(TOPOUTPUT_SELECT),
        .TOPADDSUB_LOWERINPUT(TOPADDSUB_LOWERINPUT),
        .TOPADDSUB_UPPERINPUT(TOPADDSUB_UPPERINPUT),
        .TOPADDSUB_CARRYSELECT(TOPADDSUB_CARRYSELECT),
        .BOTOUTPUT_SELECT(BOTOUTPUT_SELECT),
        .BOTADDSUB_LOWERINPUT(BOTADDSUB_LOWERINPUT),
        .BOTADDSUB_UPPERINPUT(BOTADDSUB_UPPERINPUT),
        .BOTADDSUB_CARRYSELECT(BOTADDSUB_CARRYSELECT),
        .MODE_8x8(MODE_8x8),
        .A_SIGNED(A_SIGNED),
        .B_SIGNED(B_SIGNED)
    ) DSP_inst (
        .CLK(CLK),
        .CE(CE),
        .C(C),
        .A(A),
        .B(B),
        .D(D),
        .AHOLD(AHOLD),
        .BHOLD(BHOLD),
        .CHOLD(CHOLD),
        .DHOLD(DHOLD),
        .IRSTTOP(IRSTTOP),
        .IRSTBOT(IRSTBOT),
        .ORSTTOP(ORSTTOP),
        .ORSTBOT(ORSTBOT),
        .OLOADTOP(OLOADTOP),
        .OLOADBOT(OLOADBOT),
        .ADDSUBTOP(ADDSUBTOP),
        .ADDSUBBOT(ADDSUBBOT),
        .OHOLDTOP(OHOLDTOP),
        .OHOLDBOT(OHOLDBOT),
        .CI(CI),
        .ACCUMCI(ACCUMCI),
        .SIGNEXTIN(SIGNEXTIN),
        .O(O),
        .CO(CO),
        .ACCUMCO(ACCUMCO),
        .SIGNEXTOUT(SIGNEXTOUT)
    );
endmodule