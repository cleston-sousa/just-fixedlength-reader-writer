package br.net.gits;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import br.net.gits.dtos.CidadeDTO;
import br.net.gits.dtos.EstadoDTO;
import br.net.gits.dtos.ExtratoDTO;

public class FixedLengthReaderTest {

	@Test
	void givenInputStream_withTwoLinesStringObject_whenIterateOver_thenReturnIterableObjects() {

		var reader = FixedLengthReader.init().input(ClassLoader.getSystemResourceAsStream("estado-cidades"))
				.mapper(EstadoDTO.class).mapper(CidadeDTO.class).open();

		assertThat(reader.hasNext()).isTrue();

		var o1 = reader.read();

		assertThat(o1).isInstanceOf(EstadoDTO.class);
		assertThat(o1).hasFieldOrPropertyWithValue("id", 35);
		assertThat(o1).hasFieldOrPropertyWithValue("saldo", new BigDecimal("999.89"));

		var o2 = reader.read();

		assertThat(o2).isInstanceOf(CidadeDTO.class);
		assertThat(o2).hasFieldOrPropertyWithValue("id", 3551405);
		assertThat(o2).hasFieldOrPropertyWithValue("saldo", new BigDecimal("0.01"));

	}

	@Test
	void givenStringLine_with_whenExtractObject_thenReturnJavaObject() {

		var o3 = FixedLengthReader.extractObject("01062021COMPRA AFF1520100000", ExtratoDTO.class, 1);

		assertThat(o3).isInstanceOf(ExtratoDTO.class);
		assertThat(o3).hasFieldOrPropertyWithValue("dataOcorrencia", LocalDate.of(2021, 6, 1));

		var o4 = FixedLengthReader.extractObject("02052021DEPOSITO  0000032145", ExtratoDTO.class, 1);

		assertThat(o4).isInstanceOf(ExtratoDTO.class);
		assertThat(o4).hasFieldOrPropertyWithValue("dataOcorrencia", LocalDate.of(2021, 5, 2));

	}

}
