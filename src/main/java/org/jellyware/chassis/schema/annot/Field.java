/**
 * 
 */
package org.jellyware.chassis.schema.annot;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.enterprise.util.AnnotationLiteral;

/**
 * @author Jotter
 *
 */
@Retention(RUNTIME)
@Target(TYPE)
@UI
public @interface Field {
	public static final String PRIMARY = "is-primary";
	public static final String INFO = "is-info";
	public static final String SUCCESS = "is-success";
	public static final String WARNING = "is-warning";
	public static final String DANGER = "is-danger";

	String type() default PRIMARY;

	String label() default "";

	String[] messages() default {};

	@SuppressWarnings("all")
	class Literal extends AnnotationLiteral<Field> implements Field {
		private final String type, label;
		private final String[] messages;

		public Literal(String type, String label, String[] messages) {
			super();
			this.type = type;
			this.label = label;
			this.messages = messages;
		}

		public Literal(String label) {
			super();
			this.type = "";
			this.label = label;
			this.messages = new String[] {};
		}

		@Override
		public String type() {
			return type;
		}

		@Override
		public String label() {
			return label;
		}

		@Override
		public String[] messages() {
			return messages;
		}
	}
}
