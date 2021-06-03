package br.net.gits.dtos;

import java.math.BigDecimal;

import br.net.gits.annotation.Field;
import br.net.gits.annotation.Line;
import br.net.gits.transform.PadPosition;
import lombok.Data;

@Data
@Line(pattern = "1*")
public class CidadeDTO {

	@Field(min = 9, max = 55)
	private String nome;

	@Field(min = 56, max = 60, onlyNumbers = true, decimal = 2, padCharacter = '0', padPostion = PadPosition.LEFT)
	private BigDecimal saldo;

	@Field(min = 1, max = 1)
	private String prefix;

	@Field(min = 2, max = 8)
	private Integer id;
}
