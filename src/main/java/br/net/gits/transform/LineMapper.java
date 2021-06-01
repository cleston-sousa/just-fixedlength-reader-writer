package br.net.gits.transform;

import java.util.HashMap;
import java.util.Map;

import lombok.ToString;

@ToString
public class LineMapper {

	public Class<?> type;

	public int length;

	public String pattern;

	public Map<String, Field> fields = new HashMap<>();

}
