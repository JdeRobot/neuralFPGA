package neuralfpga.core

import spinal.core._

object Riscvv {
  def vmBit = 25
  def vdRange = 11 downto 7
  def vs3Range = 11 downto 7

  /**
    * https://riscv.github.io/documents/riscv-v-spec/#_vector_loads_and_stores
    *
    * 31 29 28 26  25  24      20 19       15 14   12 11      7 6     0
    * nf  | mop | vm |  lumop   |    rs1    | width |    vd   |0000111| VL*  unit-stride
    * nf  | mop | vm |  sumop   |    rs1    | width |   vs3   |0100111| VS*  unit-stride
    *
    * nf[2:0] specifies the number of fields in each segment, for segment load/stores
    * mop[2:0] specifies memory addressing mode
    * vm specifies vector mask
    * lumop[4:0]/sumop[4:0] are additional fields encoding variants of unit-stride instructions
    * rs1[4:0] specifies x register holding base address
    * width[2:0] specifies size of memory elements, and distinguishes from FP scalar
    * vd[4:0]  specifies v register destination of load
    * vs3[4:0] specifies v register holding store data
    *
    */
  def VLB  = M"000000-00000-----000-----0000111"
  def VLH  = M"000000-00000-----101-----0000111"
  def VLW  = M"000000-00000-----110-----0000111"
  def VLE  = M"000000-00000-----111-----0000111"
  def VLBU = M"000100-00000-----000-----0000111"
  def VLHU = M"000100-00000-----101-----0000111"
  def VLWU = M"000100-00000-----110-----0000111"
  def VLEU = M"000100-00000-----111-----0000111"

  def VSB  = M"000000-00000-----000-----0100111"
  def VSH  = M"000000-00000-----101-----0100111"
  def VSW  = M"000000-00000-----110-----0100111"
  def VSE  = M"000000-00000-----111-----0100111"

  object CSR {
    def VSTART = 0x008
    def VL     = 0xC20
  }
}
