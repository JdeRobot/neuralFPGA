#Overview

[gemmlowp](https://github.com/google/gemmlowp)


```
Inputs:
	LHS is a matrix mxn
	RHS is a matrix nxp
	LHS_width = m
	RHS_width = p
	depth = n
	LHS_zp = LHS quantization zero point
	RHS_zp = RHS quantization zero point
	RESULT_scale = C quantization scale. Implemented as integer multiplication (RESULT_mult) + arithmetic right shift (RESULT_shift)

Outputs:
	RESULT is a matrix mxp

fully_connected_quantized(inputs)
{
	for i: 0..LHS_width {
		for k: 0..RHS_width {
			accumulator := bias(k)
			sum_line_LHS := 0
			sum_line_RHS := 0
			for j: 0..depth {
				acummulator := accumulator + LHS[i,j] * RHS[j,k]
				sum_line_LHS := sum_line_LHS + LHS[i,j]
				sum_line_RHS := sum_line_RHS + RHS[j,k]
			}
		
			acummulator := accumulator + (-LHS_zp * sum_line_RHS) + (-RHS_zp * sum_line_LHS)
			acummulator := accumulator * RESULT_scale
			acummulator := accumulator + RESULT_zp
		}
		RESULT[i,k] := saturate_cast(accumulator)
	}
	return RESULT
}
```