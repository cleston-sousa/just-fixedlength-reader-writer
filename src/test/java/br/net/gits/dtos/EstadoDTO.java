package br.net.gits.dtos;

import java.math.BigDecimal;

import br.net.gits.annotation.Field;
import br.net.gits.annotation.Line;
import br.net.gits.transform.PadPosition;
import lombok.Data;

@Data
@Line(pattern = "0*")
public class EstadoDTO {

	@Field(min = 2, max = 3)
	private int id;

	@Field(min = 4, max = 5)
	private String codigo;

	@Field(min = 6, max = 55)
	private String nome;

	@Field(min = 56, max = 60, onlyNumbers = true, decimal = 2, padCharacter = '0', padPostion = PadPosition.LEFT)
	private BigDecimal saldo;
}
