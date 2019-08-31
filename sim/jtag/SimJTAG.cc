// from https://github.com/chipsalliance/rocket-chip/blob/master/src/main/resources/csrc/SimJTAG.cc
// See LICENSE.SiFive for license details.

// dlobato: use fixed port number

#include <cstdlib>
#include "remote_bitbang.h"

remote_bitbang_t* jtag;
extern "C" int jtag_tick
(
 unsigned char * jtag_TCK,
 unsigned char * jtag_TMS,
 unsigned char * jtag_TDI,
 unsigned char * jtag_TRSTn,
 unsigned char jtag_TDO
)
{
  if (!jtag) {
    jtag = new remote_bitbang_t(9090);
  }

  jtag->tick(jtag_TCK, jtag_TMS, jtag_TDI, jtag_TRSTn, jtag_TDO);

  return jtag->done() ? (jtag->exit_code() << 1 | 1) : 0;

}
