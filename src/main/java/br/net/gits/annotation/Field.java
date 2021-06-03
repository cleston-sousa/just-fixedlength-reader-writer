package br.net.gits.annotation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import br.net.gits.transform.PadPosition;

@Target({ FIELD })
@Retention(RUNTIME)
public @interface Field {

	int min();

	int max();

	boolean trim() default true;

	int decimal() default 0;

	boolean onlyNumbers() default false;

	char padCharacter() default ' ';

	PadPosition padPostion() default PadPosition.RIGHT;
	
	String pattern() default "yyyy-MM-dd";
}
