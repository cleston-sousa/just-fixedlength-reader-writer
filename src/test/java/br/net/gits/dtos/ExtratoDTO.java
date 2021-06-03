package br.net.gits.dtos;

import java.math.BigDecimal;
import java.time.LocalDate;

import br.net.gits.annotation.Field;
import br.net.gits.annotation.Line;
import br.net.gits.transform.PadPosition;
import lombok.Data;

@Data
@Line
public class ExtratoDTO {

	@Field(min = 1, max = 8, pattern = "ddMMyyyy")
	private LocalDate dataOcorrencia;

	@Field(min = 9, max = 18)
	private String descricao;

	@Field(min = 19, max = 23, onlyNumbers = true, decimal = 2, padCharacter = '0', padPostion = PadPosition.LEFT)
	private BigDecimal debito;

	@Field(min = 24, max = 28, onlyNumbers = true, decimal = 2, padCharacter = '0', padPostion = PadPosition.LEFT)
	private BigDecimal credito;

}
