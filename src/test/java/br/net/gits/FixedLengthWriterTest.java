package br.net.gits;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.net.gits.dtos.CidadeDTO;
import br.net.gits.dtos.EstadoDTO;
import br.net.gits.dtos.ExtratoDTO;

public class FixedLengthWriterTest {

	@Test
	void givenObject_with_whenExtractLine_thenReturnStringLine() throws JsonMappingException, JsonProcessingException {

		ObjectMapper mapper = new ObjectMapper();

		TypeReference<CidadeDTO> typeRef1 = new TypeReference<CidadeDTO>() {
		};
		var c1 = mapper.readValue("{ \"prefix\": \"1\", \"id\": 4567899, \"nome\": \"Nome teste\", \"saldo\": 51.44 }",
				typeRef1);

		var writer = FixedLengthWriter.init().add(c1);

		var content1 = writer.content();
		assertThat(content1).isEqualTo("14567899Nome teste                                     05144");

		TypeReference<EstadoDTO> typeRef2 = new TypeReference<EstadoDTO>() {
		};
		var e1 = mapper.readValue("{ \"id\": 99, \"nome\": \"Estadinho\", \"codigo\": \"ZZ\", \"saldo\": 99.99 }",
				typeRef2);

		var content2 = FixedLengthWriter.extractLine(e1);
		assertThat(content2).isEqualTo(" 99ZZEstadinho                                         09999");

		TypeReference<ExtratoDTO> typeRef3 = new TypeReference<ExtratoDTO>() {
		};
		var e2 = mapper.readValue(
				"{ \"descricao\": \"Muito mais longa que o permitido\", \"debito\": 989.88, \"credito\": 777.66 }",
				typeRef3);
		e2.setDataOcorrencia(LocalDate.of(2021, 9, 17));

		var content3 = FixedLengthWriter.extractLine(e2);
		assertThat(content3).isEqualTo("17092021Muito mais9898877766");

	}

}
